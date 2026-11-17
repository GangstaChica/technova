package com.technova.block;

import com.technova.blockentity.AbstractMachineBE;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Bloc generique de machine : ouvre la GUI au clic droit et fait tourner le bloc entite. */
public class MachineBlock extends Block implements EntityBlock {
    private final Supplier<? extends BlockEntityType<? extends AbstractMachineBE>> typeSupplier;

    public MachineBlock(Properties properties, Supplier<? extends BlockEntityType<? extends AbstractMachineBE>> type) {
        super(properties);
        this.typeSupplier = type;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return typeSupplier.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        BlockEntityType<?> mine = typeSupplier.get();
        return type == mine ? (lvl, pos, st, be) -> ((AbstractMachineBE) be).serverTick(lvl, pos, st) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Les items contenus dans la machine tombent au sol quand un joueur la casse.
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractMachineBE machine) {
            machine.dropAll();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
