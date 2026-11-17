package com.technova.registry;

import com.technova.TechNova;
import com.technova.menu.MachineMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TechNova.MODID);

    /** Un seul type de menu pour toutes les machines : la disposition vient du bloc entite. */
    public static final Supplier<MenuType<MachineMenu>> MACHINE =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::create));

    private ModMenus() {}
}
