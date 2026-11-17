package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalTank;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Consomme de l'hydrogene (1 mB par tick) pour produire beaucoup d'energie. */
public class HydrogenGeneratorBE extends AbstractMachineBE {
    public static final int GENERATION = 100; // FE par tick

    private final ChemicalTank fuel;

    public HydrogenGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDROGEN_GENERATOR.get(), pos, state, new SlotSpec[0], 100000, 0, 800);
        this.fuel = addTank(10000, c -> c == Chemical.HYDROGEN, true, false);
    }

    @Override
    protected void tickServer() {
        if (fuel.getAmount() >= 1 && energyRoom() >= GENERATION) {
            fuel.extract(1, false);
            addEnergy(GENERATION);
            aux = 1;
        } else {
            aux = 0;
        }
        pushEnergy(800);
    }
}
