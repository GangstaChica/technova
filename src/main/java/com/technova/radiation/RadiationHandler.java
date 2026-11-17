package com.technova.radiation;

import com.technova.TechNova;
import com.technova.blockentity.ReactorControllerBE;
import com.technova.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Radiation : les items et blocs d'uranium ainsi que les reacteurs en marche irradient le joueur
 * proche. La combinaison de protection complete rend immunise.
 */
public final class RadiationHandler {
    private static final int INTERVAL = 40;
    private static final int ORE_RADIUS = 3;
    private static final int REACTOR_RADIUS = 10;

    private RadiationHandler() {}

    public static boolean hasFullHazmat(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HAZMAT_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HAZMAT_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HAZMAT_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HAZMAT_BOOTS.get());
    }

    private static boolean isRadioactive(net.minecraft.resources.Identifier id) {
        return TechNova.MODID.equals(id.getNamespace()) && id.getPath().contains("uranium");
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % INTERVAL != 0 || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (hasFullHazmat(player)) {
            return;
        }

        int dose = 0;
        // Objets dans l'inventaire
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isRadioactive(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                dose += Math.min(2, 1 + stack.getCount() / 32);
            }
        }
        // Blocs d'uranium a proximite
        BlockPos origin = player.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-ORE_RADIUS, -ORE_RADIUS, -ORE_RADIUS),
                origin.offset(ORE_RADIUS, ORE_RADIUS, ORE_RADIUS))) {
            BlockState state = player.level().getBlockState(p);
            if (!state.isAir() && isRadioactive(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) {
                dose += 1;
            }
        }
        // Reacteurs en marche
        for (GlobalPos gp : ReactorControllerBE.ACTIVE) {
            if (gp.dimension().equals(player.level().dimension())
                    && gp.pos().distSqr(origin) <= REACTOR_RADIUS * REACTOR_RADIUS) {
                dose += ReactorControllerBE.RADIATION_STRENGTH;
            }
        }

        if (dose <= 0) {
            return;
        }
        int dur = INTERVAL * 3;
        if (dose >= 1) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, dur, 0));
        }
        if (dose >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur, 0));
        }
        if (dose >= 5) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, dur, 0));
        }
        if (dose >= 8) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, dur, 0));
        }
    }
}
