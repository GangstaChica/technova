package com.technova.item;

import com.technova.blockentity.AbstractMachineBE;
import com.technova.blockentity.FaceMode;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Configurateur : Maj + clic droit sur la face d'une machine pour changer son mode
 * (tout, entree, sortie, desactivee). Sans Maj, la GUI de la machine s'ouvre normalement.
 */
public class ConfiguratorItem extends Item {
    public ConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof AbstractMachineBE machine) {
            Direction face = context.getClickedFace();
            FaceMode mode = machine.cycleFace(face);
            player.displayClientMessage(Component.translatable("message.technova.face_mode",
                    Component.translatable("direction.technova." + face.getName()),
                    Component.translatable(mode.translationKey())), true);
        }
        return InteractionResult.SUCCESS;
    }
}
