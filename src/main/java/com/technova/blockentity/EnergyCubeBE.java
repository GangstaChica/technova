package com.technova.blockentity;

import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Batterie. Par defaut : entree par toutes les faces sauf le dessous, sortie par le dessous.
 * Les faces se reconfigurent avec le configurateur.
 */
public class EnergyCubeBE extends AbstractMachineBE {
    private static final int RATE = 4000;

    public EnergyCubeBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CUBE.get(), pos, state, new SlotSpec[0], 2_000_000, RATE, RATE);
    }

    @Override
    protected FaceMode defaultFaceMode(Direction side) {
        return side == Direction.DOWN ? FaceMode.OUTPUT : FaceMode.INPUT;
    }

    @Override
    protected void tickServer() {
        pushEnergy(RATE);
    }
}
