package com.technova.registry;

import com.technova.TechNova;
import com.technova.block.MachineBlock;
import com.technova.block.PipeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TechNova.MODID);

    private static Properties stone() {
        return Properties.of().strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private static Properties deepslate() {
        return Properties.of().strength(4.5F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE);
    }

    private static Properties metal() {
        return Properties.of().strength(3.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
    }

    private static Properties pipe() {
        return Properties.of().strength(1.0F, 3.0F).sound(SoundType.METAL).noOcclusion();
    }

    // Minerais
    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.registerBlock("tin_ore", Block::new, stone());
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = BLOCKS.registerBlock("deepslate_tin_ore", Block::new, deepslate());
    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.registerBlock("lead_ore", Block::new, stone());
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = BLOCKS.registerBlock("deepslate_lead_ore", Block::new, deepslate());
    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.registerBlock("uranium_ore", Block::new, stone());
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.registerBlock("deepslate_uranium_ore", Block::new, deepslate());
    public static final DeferredBlock<Block> SULFUR_ORE = BLOCKS.registerBlock("sulfur_ore", Block::new, stone());
    public static final DeferredBlock<Block> SALT_ORE = BLOCKS.registerBlock("salt_ore", Block::new, stone());

    // Blocs de metal
    public static final DeferredBlock<Block> TIN_BLOCK = BLOCKS.registerBlock("tin_block", Block::new, metal());
    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.registerBlock("lead_block", Block::new, metal());
    public static final DeferredBlock<Block> URANIUM_BLOCK = BLOCKS.registerBlock("uranium_block", Block::new, metal());

    // Generateurs
    public static final DeferredBlock<Block> COMBUSTION_GENERATOR = BLOCKS.registerBlock("combustion_generator",
            p -> new MachineBlock(p, ModBlockEntities.COMBUSTION_GENERATOR), metal());
    public static final DeferredBlock<Block> SOLAR_GENERATOR = BLOCKS.registerBlock("solar_generator",
            p -> new MachineBlock(p, ModBlockEntities.SOLAR_GENERATOR), metal());
    public static final DeferredBlock<Block> HYDROGEN_GENERATOR = BLOCKS.registerBlock("hydrogen_generator",
            p -> new MachineBlock(p, ModBlockEntities.HYDROGEN_GENERATOR), metal());

    // Machines d'objets
    public static final DeferredBlock<Block> CRUSHER = BLOCKS.registerBlock("crusher",
            p -> new MachineBlock(p, ModBlockEntities.CRUSHER), metal());
    public static final DeferredBlock<Block> ENRICHMENT_CHAMBER = BLOCKS.registerBlock("enrichment_chamber",
            p -> new MachineBlock(p, ModBlockEntities.ENRICHMENT_CHAMBER), metal());
    public static final DeferredBlock<Block> ELECTRIC_FURNACE = BLOCKS.registerBlock("electric_furnace",
            p -> new MachineBlock(p, ModBlockEntities.ELECTRIC_FURNACE), metal());

    // Chimie
    public static final DeferredBlock<Block> PURIFICATION_CHAMBER = BLOCKS.registerBlock("purification_chamber",
            p -> new MachineBlock(p, ModBlockEntities.PURIFICATION_CHAMBER), metal());
    public static final DeferredBlock<Block> INJECTION_CHAMBER = BLOCKS.registerBlock("injection_chamber",
            p -> new MachineBlock(p, ModBlockEntities.INJECTION_CHAMBER), metal());
    public static final DeferredBlock<Block> DISSOLUTION_CHAMBER = BLOCKS.registerBlock("dissolution_chamber",
            p -> new MachineBlock(p, ModBlockEntities.DISSOLUTION_CHAMBER), metal());
    public static final DeferredBlock<Block> CHEMICAL_INFUSER = BLOCKS.registerBlock("chemical_infuser",
            p -> new MachineBlock(p, ModBlockEntities.CHEMICAL_INFUSER), metal());
    public static final DeferredBlock<Block> ELECTROLYTIC_SEPARATOR = BLOCKS.registerBlock("electrolytic_separator",
            p -> new MachineBlock(p, ModBlockEntities.ELECTROLYTIC_SEPARATOR), metal());
    public static final DeferredBlock<Block> CHEMICAL_OXIDIZER = BLOCKS.registerBlock("chemical_oxidizer",
            p -> new MachineBlock(p, ModBlockEntities.CHEMICAL_OXIDIZER), metal());

    // Stockage et transport
    public static final DeferredBlock<Block> CHEMICAL_TANK = BLOCKS.registerBlock("chemical_tank",
            p -> new MachineBlock(p, ModBlockEntities.CHEMICAL_TANK), metal());
    public static final DeferredBlock<Block> ENERGY_CUBE = BLOCKS.registerBlock("energy_cube",
            p -> new MachineBlock(p, ModBlockEntities.ENERGY_CUBE), metal());
    public static final DeferredBlock<Block> ENERGY_CABLE = BLOCKS.registerBlock("energy_cable", p -> new PipeBlock(p, ModBlockEntities.ENERGY_CABLE), pipe());
    public static final DeferredBlock<Block> CHEMICAL_TUBE = BLOCKS.registerBlock("chemical_tube", p -> new PipeBlock(p, ModBlockEntities.CHEMICAL_TUBE), pipe());

    // Reacteur a fission
    public static final DeferredBlock<Block> REACTOR_CASING = BLOCKS.registerBlock("reactor_casing", Block::new, metal());
    public static final DeferredBlock<Block> REACTOR_CORE = BLOCKS.registerBlock("reactor_core", Block::new, metal());
    public static final DeferredBlock<Block> REACTOR_CONTROLLER = BLOCKS.registerBlock("reactor_controller",
            p -> new MachineBlock(p, ModBlockEntities.REACTOR_CONTROLLER), metal());

    private ModBlocks() {}
}
