package com.technova.registry;

import com.technova.TechNova;
import com.technova.blockentity.*;
import com.technova.chemical.ChemicalCapabilities;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TechNova.MODID);

    private static <T extends BlockEntity> Supplier<BlockEntityType<T>> reg(String name,
            BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(factory, block.get()));
    }

    public static final Supplier<BlockEntityType<CombustionGeneratorBE>> COMBUSTION_GENERATOR =
            reg("combustion_generator", CombustionGeneratorBE::new, ModBlocks.COMBUSTION_GENERATOR);
    public static final Supplier<BlockEntityType<SolarGeneratorBE>> SOLAR_GENERATOR =
            reg("solar_generator", SolarGeneratorBE::new, ModBlocks.SOLAR_GENERATOR);
    public static final Supplier<BlockEntityType<HydrogenGeneratorBE>> HYDROGEN_GENERATOR =
            reg("hydrogen_generator", HydrogenGeneratorBE::new, ModBlocks.HYDROGEN_GENERATOR);

    public static final Supplier<BlockEntityType<ItemProcessorBE>> CRUSHER =
            reg("crusher", (p, s) -> new ItemProcessorBE(ItemProcessorBE.Kind.CRUSHER, p, s), ModBlocks.CRUSHER);
    public static final Supplier<BlockEntityType<ItemProcessorBE>> ENRICHMENT_CHAMBER =
            reg("enrichment_chamber", (p, s) -> new ItemProcessorBE(ItemProcessorBE.Kind.ENRICHMENT, p, s), ModBlocks.ENRICHMENT_CHAMBER);
    public static final Supplier<BlockEntityType<ItemProcessorBE>> ELECTRIC_FURNACE =
            reg("electric_furnace", (p, s) -> new ItemProcessorBE(ItemProcessorBE.Kind.SMELTER, p, s), ModBlocks.ELECTRIC_FURNACE);

    public static final Supplier<BlockEntityType<ChemicalProcessorBE>> PURIFICATION_CHAMBER =
            reg("purification_chamber", (p, s) -> new ChemicalProcessorBE(ChemicalProcessorBE.Kind.PURIFICATION, p, s), ModBlocks.PURIFICATION_CHAMBER);
    public static final Supplier<BlockEntityType<ChemicalProcessorBE>> INJECTION_CHAMBER =
            reg("injection_chamber", (p, s) -> new ChemicalProcessorBE(ChemicalProcessorBE.Kind.INJECTION, p, s), ModBlocks.INJECTION_CHAMBER);
    public static final Supplier<BlockEntityType<ChemicalProcessorBE>> DISSOLUTION_CHAMBER =
            reg("dissolution_chamber", (p, s) -> new ChemicalProcessorBE(ChemicalProcessorBE.Kind.DISSOLUTION, p, s), ModBlocks.DISSOLUTION_CHAMBER);

    public static final Supplier<BlockEntityType<ChemicalInfuserBE>> CHEMICAL_INFUSER =
            reg("chemical_infuser", ChemicalInfuserBE::new, ModBlocks.CHEMICAL_INFUSER);
    public static final Supplier<BlockEntityType<ElectrolyticSeparatorBE>> ELECTROLYTIC_SEPARATOR =
            reg("electrolytic_separator", ElectrolyticSeparatorBE::new, ModBlocks.ELECTROLYTIC_SEPARATOR);
    public static final Supplier<BlockEntityType<ChemicalOxidizerBE>> CHEMICAL_OXIDIZER =
            reg("chemical_oxidizer", ChemicalOxidizerBE::new, ModBlocks.CHEMICAL_OXIDIZER);

    public static final Supplier<BlockEntityType<ChemicalTankBE>> CHEMICAL_TANK =
            reg("chemical_tank", ChemicalTankBE::new, ModBlocks.CHEMICAL_TANK);
    public static final Supplier<BlockEntityType<EnergyCubeBE>> ENERGY_CUBE =
            reg("energy_cube", EnergyCubeBE::new, ModBlocks.ENERGY_CUBE);
    public static final Supplier<BlockEntityType<EnergyCableBE>> ENERGY_CABLE =
            reg("energy_cable", EnergyCableBE::new, ModBlocks.ENERGY_CABLE);
    public static final Supplier<BlockEntityType<ChemicalTubeBE>> CHEMICAL_TUBE =
            reg("chemical_tube", ChemicalTubeBE::new, ModBlocks.CHEMICAL_TUBE);

    public static final Supplier<BlockEntityType<ReactorControllerBE>> REACTOR_CONTROLLER =
            reg("reactor_controller", ReactorControllerBE::new, ModBlocks.REACTOR_CONTROLLER);

    private static void machine(RegisterCapabilitiesEvent event, BlockEntityType<? extends AbstractMachineBE> type) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, type, (be, side) -> be.getEnergyHandler(side));
        event.registerBlockEntity(ChemicalCapabilities.BLOCK, type, (be, side) -> be.getChemicalHandler(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK, type, (be, side) -> be.getItemHandler(side));
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        machine(event, COMBUSTION_GENERATOR.get());
        machine(event, SOLAR_GENERATOR.get());
        machine(event, HYDROGEN_GENERATOR.get());
        machine(event, CRUSHER.get());
        machine(event, ENRICHMENT_CHAMBER.get());
        machine(event, ELECTRIC_FURNACE.get());
        machine(event, PURIFICATION_CHAMBER.get());
        machine(event, INJECTION_CHAMBER.get());
        machine(event, DISSOLUTION_CHAMBER.get());
        machine(event, CHEMICAL_INFUSER.get());
        machine(event, ELECTROLYTIC_SEPARATOR.get());
        machine(event, CHEMICAL_OXIDIZER.get());
        machine(event, CHEMICAL_TANK.get());
        machine(event, ENERGY_CUBE.get());
        machine(event, REACTOR_CONTROLLER.get());

        event.registerBlockEntity(Capabilities.Energy.BLOCK, ENERGY_CABLE.get(), (be, side) -> be.energy);
        event.registerBlockEntity(ChemicalCapabilities.BLOCK, CHEMICAL_TUBE.get(), (be, side) -> be.tank);
    }

    private ModBlockEntities() {}
}
