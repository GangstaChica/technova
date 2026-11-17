package com.technova.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.technova.TechNova;
import com.technova.chemical.Chemical;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/**
 * Recettes des machines, chargees depuis les datapacks (donc modifiables et extensibles) :
 *
 * <ul>
 *   <li>{@code data/<ns>/machine_recipes/**.json} : machines "objet -> objet" ;</li>
 *   <li>{@code data/<ns>/machine_infusing/**.json} : infuseur chimique ;</li>
 *   <li>{@code data/<ns>/machine_oxidizing/**.json} : oxydeur chimique.</li>
 * </ul>
 *
 * <p>Les recettes par defaut du mod se trouvent dans {@code data/technova/}.
 */
public final class MachineRecipes {
    public static final String CRUSHER = "crusher";
    public static final String ENRICHMENT = "enrichment";
    public static final String SMELTER = "smelter";
    public static final String PURIFICATION = "purification";
    public static final String INJECTION = "injection";
    public static final String DISSOLUTION = "dissolution";

    public record ItemRecipe(Item input, int inCount, Item output, int outCount, int chemAmount) {}

    public record InfuseRecipe(Chemical a, Chemical b, int aAmount, int bAmount, Chemical out, int outAmount) {}

    public record OxidizeRecipe(Item input, Chemical out, int amount) {}

    private static volatile Map<String, Map<Item, ItemRecipe>> tables = Map.of();
    private static volatile List<InfuseRecipe> infuse = List.of();
    private static volatile List<OxidizeRecipe> oxidize = List.of();

    private MachineRecipes() {}

    // ------------------------------------------------------------------ acces

    public static ItemRecipe find(String machine, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Map<Item, ItemRecipe> table = tables.get(machine);
        return table == null ? null : table.get(stack.getItem());
    }

    public static List<InfuseRecipe> infuseRecipes() {
        return infuse;
    }

    public static List<OxidizeRecipe> oxidizeRecipes() {
        return oxidize;
    }

    public static OxidizeRecipe findOxidize(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (OxidizeRecipe r : oxidize) {
            if (stack.is(r.input())) {
                return r;
            }
        }
        return null;
    }

    /** Duree de combustion (en ticks) d'un objet dans le generateur a combustion. */
    public static int burnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 1600;
        }
        if (stack.is(Items.COAL_BLOCK)) {
            return 16000;
        }
        if (stack.is(Items.BLAZE_ROD)) {
            return 2400;
        }
        if (stack.is(Items.DRIED_KELP_BLOCK)) {
            return 4000;
        }
        if (stack.is(Items.STICK)) {
            return 100;
        }
        if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS_THAT_BURN)) {
            return 300;
        }
        return 0;
    }

    // ------------------------------------------------------------------ chargement

    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(TechNova.MODID, "machine_recipes"), new ItemListener());
        event.addListener(Identifier.fromNamespaceAndPath(TechNova.MODID, "machine_infusing"), new InfuseListener());
        event.addListener(Identifier.fromNamespaceAndPath(TechNova.MODID, "machine_oxidizing"), new OxidizeListener());
    }

    private static Item item(Identifier id) {
        return BuiltInRegistries.ITEM.containsKey(id) ? BuiltInRegistries.ITEM.getValue(id) : null;
    }

    public record ItemRecipeData(String machine, Identifier input, int inputCount, Identifier result,
                                 int resultCount, int chemicalAmount) {
        static final Codec<ItemRecipeData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("machine").forGetter(ItemRecipeData::machine),
                Identifier.CODEC.fieldOf("input").forGetter(ItemRecipeData::input),
                Codec.INT.optionalFieldOf("input_count", 1).forGetter(ItemRecipeData::inputCount),
                Identifier.CODEC.fieldOf("result").forGetter(ItemRecipeData::result),
                Codec.INT.optionalFieldOf("result_count", 1).forGetter(ItemRecipeData::resultCount),
                Codec.INT.optionalFieldOf("chemical_amount", 0).forGetter(ItemRecipeData::chemicalAmount)
        ).apply(i, ItemRecipeData::new));
    }

    public record InfuseData(String a, String b, int aAmount, int bAmount, String out, int outAmount) {
        static final Codec<InfuseData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("a").forGetter(InfuseData::a),
                Codec.STRING.fieldOf("b").forGetter(InfuseData::b),
                Codec.INT.fieldOf("a_amount").forGetter(InfuseData::aAmount),
                Codec.INT.fieldOf("b_amount").forGetter(InfuseData::bAmount),
                Codec.STRING.fieldOf("out").forGetter(InfuseData::out),
                Codec.INT.fieldOf("out_amount").forGetter(InfuseData::outAmount)
        ).apply(i, InfuseData::new));
    }

    public record OxidizeData(Identifier input, String chemical, int amount) {
        static final Codec<OxidizeData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("input").forGetter(OxidizeData::input),
                Codec.STRING.fieldOf("chemical").forGetter(OxidizeData::chemical),
                Codec.INT.fieldOf("amount").forGetter(OxidizeData::amount)
        ).apply(i, OxidizeData::new));
    }

    private static final class ItemListener extends SimpleJsonResourceReloadListener<ItemRecipeData> {
        ItemListener() {
            super(ItemRecipeData.CODEC, FileToIdConverter.json("machine_recipes"));
        }

        @Override
        protected void apply(Map<Identifier, ItemRecipeData> prepared, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, Map<Item, ItemRecipe>> result = new HashMap<>();
            for (ItemRecipeData d : prepared.values()) {
                Item in = item(d.input());
                Item out = item(d.result());
                if (in == null || out == null || d.inputCount() < 1 || d.resultCount() < 1) {
                    continue; // objet inconnu (mod optionnel absent) : on ignore la recette
                }
                result.computeIfAbsent(d.machine(), k -> new HashMap<>())
                        .put(in, new ItemRecipe(in, d.inputCount(), out, d.resultCount(), d.chemicalAmount()));
            }
            tables = result;
        }
    }

    private static final class InfuseListener extends SimpleJsonResourceReloadListener<InfuseData> {
        InfuseListener() {
            super(InfuseData.CODEC, FileToIdConverter.json("machine_infusing"));
        }

        @Override
        protected void apply(Map<Identifier, InfuseData> prepared, ResourceManager manager, ProfilerFiller profiler) {
            List<InfuseRecipe> result = new ArrayList<>();
            for (InfuseData d : prepared.values()) {
                Chemical a = Chemical.byId(d.a());
                Chemical b = Chemical.byId(d.b());
                Chemical out = Chemical.byId(d.out());
                if (a == Chemical.EMPTY || b == Chemical.EMPTY || out == Chemical.EMPTY) {
                    continue;
                }
                result.add(new InfuseRecipe(a, b, d.aAmount(), d.bAmount(), out, d.outAmount()));
            }
            infuse = List.copyOf(result);
        }
    }

    private static final class OxidizeListener extends SimpleJsonResourceReloadListener<OxidizeData> {
        OxidizeListener() {
            super(OxidizeData.CODEC, FileToIdConverter.json("machine_oxidizing"));
        }

        @Override
        protected void apply(Map<Identifier, OxidizeData> prepared, ResourceManager manager, ProfilerFiller profiler) {
            List<OxidizeRecipe> result = new ArrayList<>();
            for (OxidizeData d : prepared.values()) {
                Item in = item(d.input());
                Chemical chemical = Chemical.byId(d.chemical());
                if (in == null || chemical == Chemical.EMPTY) {
                    continue;
                }
                result.add(new OxidizeRecipe(in, chemical, d.amount()));
            }
            oxidize = List.copyOf(result);
        }
    }
}
