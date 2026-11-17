package com.technova;

import com.technova.client.MachineScreen;
import com.technova.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Point d'entree cote client uniquement (ecrans de GUI). */
@Mod(value = TechNova.MODID, dist = Dist.CLIENT)
public class TechNovaClient {
    public TechNovaClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(TechNovaClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MACHINE.get(), MachineScreen::new);
    }
}
