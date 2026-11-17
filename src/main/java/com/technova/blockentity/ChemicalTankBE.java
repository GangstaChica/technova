package com.technova.blockentity;

import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reservoir de stockage. Par defaut : entree par toutes les faces sauf le dessous, sortie
 * par le dessous. Les faces se reconfigurent avec le configurateur.
 */
public class ChemicalTankBE extends AbstractMachineBE {
    private static final int RATE = 500;

    public ChemicalTankBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_TANK.get(), pos, state, new SlotSpec[0], 0, 0, 0);
        addTank(32000, c -> true, true, true);
    }

    @Override
    public boolean usesEnergy() {
        return false;
    }

    @Override
    protected FaceMode defaultFaceMode(Direction side) {
        return side == Direction.DOWN ? FaceMode.OUTPUT : FaceMode.INPUT;
    }

    @Override
    protected void tickServer() {
        pushChemicals(RATE);
    }
}
