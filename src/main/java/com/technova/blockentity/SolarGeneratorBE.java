package com.technova.blockentity;

import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Produit de l'energie en plein jour, a ciel ouvert et sans pluie. */
public class SolarGeneratorBE extends AbstractMachineBE {
    public static final int GENERATION = 15; // FE par tick

    public SolarGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_GENERATOR.get(), pos, state, new SlotSpec[0], 20000, 0, 200);
    }

    @Override
    protected void tickServer() {
        boolean sun = level.canSeeSky(worldPosition.above()) && level.isBrightOutside() && !level.isRaining();
        aux = sun ? 1 : 0;
        auxMax = 1;
        if (sun) {
            addEnergy(GENERATION);
        }
        pushEnergy(200);
    }
}
