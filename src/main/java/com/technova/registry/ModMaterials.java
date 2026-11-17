package com.technova.registry;

import com.technova.TechNova;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class ModMaterials {
    public static final TagKey<Item> TIN_INGOTS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/tin"));
    public static final TagKey<Item> LEAD_INGOTS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/lead"));

    /** Outils en etain : un peu meilleurs que la pierre, plus rapides, tres enchantables. */
    public static final ToolMaterial TIN_TOOLS = new ToolMaterial(
            BlockTags.INCORRECT_FOR_IRON_TOOL, 220, 6.0F, 2.0F, 14, TIN_INGOTS);

    public static final ResourceKey<EquipmentAsset> HAZMAT_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(TechNova.MODID, "hazmat"));

    /** Combinaison de protection contre la radiation (reparable avec du plomb). */
    public static final ArmorMaterial HAZMAT = new ArmorMaterial(
            15,
            Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 3, ArmorType.CHESTPLATE, 4, ArmorType.HELMET, 2,
                    ArmorType.BODY, 4),
            12, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, LEAD_INGOTS, HAZMAT_ASSET);

    private ModMaterials() {}
}
