package com.technova.blockentity;

import com.technova.registry.ModBlockEntities;
import com.technova.util.EnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/**
 * Cable d'energie : un petit tampon qui s'equilibre avec les cables voisins et alimente
 * directement les machines adjacentes.
 */
public class EnergyCableBE extends PipeBE {
    public static final int CAPACITY = 4000;
    public static final int TRANSFER = 800;

    public final SimpleEnergyHandler energy = new SimpleEnergyHandler(CAPACITY, TRANSFER);

    public EnergyCableBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE.get(), pos, state);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbor, Direction dir) {
        return level.getBlockEntity(neighbor) instanceof EnergyCableBE
                || level.getCapability(Capabilities.Energy.BLOCK, neighbor, dir.getOpposite()) != null;
    }

    @Override
    protected void transfer(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            int mine = energy.getAmountAsInt();
            if (mine <= 0) {
                return;
            }
            BlockPos np = pos.relative(d);
            if (!level.isLoaded(np)) {
                continue;
            }
            EnergyHandler other = level.getCapability(Capabilities.Energy.BLOCK, np, d.getOpposite());
            if (other == null) {
                continue;
            }
            if (level.getBlockEntity(np) instanceof EnergyCableBE) {
                // Entre cables : on equilibre les niveaux, sans aller-retour.
                int diff = mine - other.getAmountAsInt();
                if (diff > 1) {
                    EnergyUtil.transfer(energy, other, Math.min(diff / 2, TRANSFER));
                }
            } else {
                EnergyUtil.transfer(energy, other, TRANSFER);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output.child("energy"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input.childOrEmpty("energy"));
    }
}
