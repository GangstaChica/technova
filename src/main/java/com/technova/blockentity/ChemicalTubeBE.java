package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalCapabilities;
import com.technova.chemical.ChemicalHandler;
import com.technova.chemical.ChemicalTank;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Tube chimique : transporte un seul produit a la fois. Les produits differents ne se
 * melangent jamais : il faut un reseau de tubes par produit.
 */
public class ChemicalTubeBE extends PipeBE {
    public static final int CAPACITY = 4000;
    public static final int TRANSFER = 400;

    public final ChemicalTank tank = new ChemicalTank(CAPACITY, c -> true);

    public ChemicalTubeBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_TUBE.get(), pos, state);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbor, Direction dir) {
        return level.getBlockEntity(neighbor) instanceof ChemicalTubeBE
                || level.getCapability(ChemicalCapabilities.BLOCK, neighbor, dir.getOpposite()) != null;
    }

    @Override
    protected void transfer(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (tank.isEmpty()) {
                return;
            }
            BlockPos np = pos.relative(d);
            if (!level.isLoaded(np)) {
                continue;
            }
            ChemicalHandler other = level.getCapability(ChemicalCapabilities.BLOCK, np, d.getOpposite());
            if (other == null) {
                continue;
            }
            Chemical chemical = tank.getChemical();
            int move;
            if (level.getBlockEntity(np) instanceof ChemicalTubeBE) {
                // Entre tubes : equilibrage des niveaux.
                int diff = tank.getAmount() - other.getAmount();
                if (diff <= 1) {
                    continue;
                }
                move = Math.min(diff / 2, TRANSFER);
            } else {
                move = Math.min(tank.getAmount(), TRANSFER);
            }
            int accepted = other.insert(chemical, move, true);
            if (accepted > 0) {
                int taken = tank.extract(accepted, false);
                other.insert(chemical, taken, false);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.save(output.child("tank"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.load(input.childOrEmpty("tank"));
    }
}
