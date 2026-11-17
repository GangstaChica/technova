package com.technova.registry;

import com.technova.TechNova;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechNova.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.technova"))
                    .icon(() -> new ItemStack(ModBlocks.ENERGY_CUBE.get()))
                    .displayItems((params, output) -> ModItems.ITEMS.getEntries().forEach(e -> output.accept(e.get())))
                    .build());

    private ModTabs() {}
}
