package com.technova.blockentity;

import com.technova.block.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Base commune des cables et des tubes : mise a jour des connexions + transfert a chaque tick. */
public abstract class PipeBE extends BlockEntity {
    private int refreshCooldown = 1;

    protected PipeBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final void serverTick(Level level, BlockPos pos, BlockState state) {
        if (--refreshCooldown <= 0) {
            refreshCooldown = 10;
            refreshConnections(level, pos);
        }
        transfer(level, pos);
        if (level.getGameTime() % 40L == 0L) {
            setChanged();
        }
    }

    private void refreshConnections(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!(current.getBlock() instanceof PipeBlock)) {
            return;
        }
        BlockState next = current;
        for (Direction d : Direction.values()) {
            BlockPos np = pos.relative(d);
            boolean connected = level.isLoaded(np) && connectsTo(level, np, d);
            next = next.setValue(PipeBlock.PROPS.get(d), connected);
        }
        if (next != current) {
            level.setBlock(pos, next, Block.UPDATE_ALL);
        }
    }

    /** Le voisin dans la direction {@code dir} doit-il etre relie visuellement a ce tuyau ? */
    protected abstract boolean connectsTo(Level level, BlockPos neighbor, Direction dir);

    protected abstract void transfer(Level level, BlockPos pos);
}
