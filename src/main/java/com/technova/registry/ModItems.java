package com.technova.registry;

import com.technova.TechNova;
import com.technova.item.ConfiguratorItem;
import com.technova.recipe.Metal;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TechNova.MODID);

    // Materiaux
    public static final DeferredItem<Item> TIN_INGOT = ITEMS.registerSimpleItem("tin_ingot");
    public static final DeferredItem<Item> LEAD_INGOT = ITEMS.registerSimpleItem("lead_ingot");
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot");
    public static final DeferredItem<Item> RAW_TIN = ITEMS.registerSimpleItem("raw_tin");
    public static final DeferredItem<Item> RAW_LEAD = ITEMS.registerSimpleItem("raw_lead");
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium");
    public static final DeferredItem<Item> SULFUR_DUST = ITEMS.registerSimpleItem("sulfur_dust");
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> BASIC_CIRCUIT = ITEMS.registerSimpleItem("basic_circuit");
    public static final DeferredItem<Item> MACHINE_CASING = ITEMS.registerSimpleItem("machine_casing");
    public static final DeferredItem<Item> ENERGY_TABLET = ITEMS.registerSimpleItem("energy_tablet");

    // Chaine de multiplication des minerais
    public static final Map<Metal, DeferredItem<Item>> DUST = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredItem<Item>> CLUMP = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredItem<Item>> SHARD = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredItem<Item>> CRYSTAL = new EnumMap<>(Metal.class);

    static {
        for (Metal m : Metal.values()) {
            DUST.put(m, ITEMS.registerSimpleItem(m.id + "_dust"));
            CLUMP.put(m, ITEMS.registerSimpleItem(m.id + "_clump"));
            SHARD.put(m, ITEMS.registerSimpleItem(m.id + "_shard"));
            CRYSTAL.put(m, ITEMS.registerSimpleItem(m.id + "_crystal"));
        }
    }

    // Outils et upgrades
    public static final DeferredItem<Item> CONFIGURATOR =
            ITEMS.registerItem("configurator", ConfiguratorItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SPEED_UPGRADE =
            ITEMS.registerItem("speed_upgrade", Item::new, new Item.Properties().stacksTo(4));
    public static final DeferredItem<Item> ENERGY_UPGRADE =
            ITEMS.registerItem("energy_upgrade", Item::new, new Item.Properties().stacksTo(4));

    // Outils en etain
    public static final DeferredItem<Item> TIN_PICKAXE = ITEMS.registerItem("tin_pickaxe",
            p -> new Item(p.pickaxe(ModMaterials.TIN_TOOLS, 1.0F, -2.8F)));
    public static final DeferredItem<Item> TIN_AXE = ITEMS.registerItem("tin_axe",
            p -> new AxeItem(ModMaterials.TIN_TOOLS, 6.0F, -3.2F, p));
    public static final DeferredItem<Item> TIN_SHOVEL = ITEMS.registerItem("tin_shovel",
            p -> new ShovelItem(ModMaterials.TIN_TOOLS, 1.5F, -3.0F, p));
    public static final DeferredItem<Item> TIN_HOE = ITEMS.registerItem("tin_hoe",
            p -> new HoeItem(ModMaterials.TIN_TOOLS, 0.0F, -3.0F, p));
    public static final DeferredItem<Item> TIN_SWORD = ITEMS.registerItem("tin_sword",
            p -> new Item(p.sword(ModMaterials.TIN_TOOLS, 3.0F, -2.4F)));

    // Combinaison de protection
    public static final DeferredItem<Item> HAZMAT_HELMET = ITEMS.registerItem("hazmat_helmet",
            p -> new Item(p.humanoidArmor(ModMaterials.HAZMAT, ArmorType.HELMET)));
    public static final DeferredItem<Item> HAZMAT_CHESTPLATE = ITEMS.registerItem("hazmat_chestplate",
            p -> new Item(p.humanoidArmor(ModMaterials.HAZMAT, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> HAZMAT_LEGGINGS = ITEMS.registerItem("hazmat_leggings",
            p -> new Item(p.humanoidArmor(ModMaterials.HAZMAT, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> HAZMAT_BOOTS = ITEMS.registerItem("hazmat_boots",
            p -> new Item(p.humanoidArmor(ModMaterials.HAZMAT, ArmorType.BOOTS)));

    // Un objet-bloc pour chaque bloc du mod
    static {
        ModBlocks.BLOCKS.getEntries().forEach(e -> ITEMS.registerSimpleBlockItem(e));
    }

    private ModItems() {}
}
