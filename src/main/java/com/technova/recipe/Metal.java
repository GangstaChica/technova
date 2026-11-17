package com.technova.recipe;

import com.technova.registry.ModBlocks;
import com.technova.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Les metaux traites par la chaine de multiplication des minerais.
 * Pour chacun on cree : poussiere, agglomerat (clump), eclat (shard) et cristal.
 */
public enum Metal {
    IRON("iron", () -> Items.IRON_INGOT, () -> Items.RAW_IRON,
            () -> List.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE)),
    GOLD("gold", () -> Items.GOLD_INGOT, () -> Items.RAW_GOLD,
            () -> List.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE)),
    COPPER("copper", () -> Items.COPPER_INGOT, () -> Items.RAW_COPPER,
            () -> List.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE)),
    TIN("tin", () -> ModItems.TIN_INGOT.get(), () -> ModItems.RAW_TIN.get(),
            () -> List.of(ModBlocks.TIN_ORE.asItem(), ModBlocks.DEEPSLATE_TIN_ORE.asItem())),
    LEAD("lead", () -> ModItems.LEAD_INGOT.get(), () -> ModItems.RAW_LEAD.get(),
            () -> List.of(ModBlocks.LEAD_ORE.asItem(), ModBlocks.DEEPSLATE_LEAD_ORE.asItem())),
    URANIUM("uranium", () -> ModItems.URANIUM_INGOT.get(), () -> ModItems.RAW_URANIUM.get(),
            () -> List.of(ModBlocks.URANIUM_ORE.asItem(), ModBlocks.DEEPSLATE_URANIUM_ORE.asItem()));

    public final String id;
    private final Supplier<Item> ingot;
    private final Supplier<Item> raw;
    private final Supplier<List<Item>> ores;

    Metal(String id, Supplier<Item> ingot, Supplier<Item> raw, Supplier<List<Item>> ores) {
        this.id = id;
        this.ingot = ingot;
        this.raw = raw;
        this.ores = ores;
    }

    public Item ingot() {
        return ingot.get();
    }

    public Item raw() {
        return raw.get();
    }

    public List<Item> ores() {
        return ores.get();
    }

    public Item dust() {
        return ModItems.DUST.get(this).get();
    }

    public Item clump() {
        return ModItems.CLUMP.get(this).get();
    }

    public Item shard() {
        return ModItems.SHARD.get(this).get();
    }

    public Item crystal() {
        return ModItems.CRYSTAL.get(this).get();
    }
}
