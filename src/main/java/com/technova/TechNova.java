package com.technova;

import com.technova.radiation.RadiationHandler;
import com.technova.recipe.MachineRecipes;
import com.technova.registry.ModBlockEntities;
import com.technova.registry.ModBlocks;
import com.technova.registry.ModItems;
import com.technova.registry.ModMenus;
import com.technova.registry.ModTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(TechNova.MODID)
public class TechNova {
    public static final String MODID = "technova";

    public TechNova(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModTabs.TABS.register(modBus);
        modBus.addListener(ModBlockEntities::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(MachineRecipes::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(RadiationHandler::onPlayerTick);
    }
}
