package com.technova.blockentity;

import com.technova.recipe.MachineRecipes;
import com.technova.recipe.MachineRecipes.ItemRecipe;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Machine simple "objet -> objet" : broyeur, chambre d'enrichissement, four electrique. */
public class ItemProcessorBE extends AbstractMachineBE {

    public enum Kind {
        CRUSHER(MachineRecipes.CRUSHER, 20, 150),
        ENRICHMENT(MachineRecipes.ENRICHMENT, 30, 200),
        SMELTER(MachineRecipes.SMELTER, 25, 120);

        final String recipeKey;
        final int energyPerTick;
        final int ticks;

        Kind(String recipeKey, int energyPerTick, int ticks) {
            this.recipeKey = recipeKey;
            this.energyPerTick = energyPerTick;
            this.ticks = ticks;
        }

        BlockEntityType<ItemProcessorBE> type() {
            switch (this) {
                case CRUSHER: return ModBlockEntities.CRUSHER.get();
                case ENRICHMENT: return ModBlockEntities.ENRICHMENT_CHAMBER.get();
                default: return ModBlockEntities.ELECTRIC_FURNACE.get();
            }
        }
    }

    private final Kind kind;

    public ItemProcessorBE(Kind kind, BlockPos pos, BlockState state) {
        super(kind.type(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 35, false), new SlotSpec(130, 35, true)},
                20000, 400, 0, true);
        this.kind = kind;
        this.maxProgress = kind.ticks;
    }

    @Override
    public boolean showsProgress() {
        return true;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return MachineRecipes.find(kind.recipeKey, stack) != null;
    }

    @Override
    protected void tickServer() {
        ItemStack input = items.get(0);
        ItemRecipe recipe = MachineRecipes.find(kind.recipeKey, input);
        int cost = scaledEnergy(kind.energyPerTick);
        boolean canRun = recipe != null
                && input.getCount() >= recipe.inCount()
                && outputFits(1, recipe.output(), recipe.outCount())
                && energy.getAmountAsInt() >= cost;

        if (canRun) {
            useEnergy(cost);
            progress += speedStep();
            if (progress >= kind.ticks) {
                input.shrink(recipe.inCount());
                if (input.isEmpty()) {
                    items.set(0, ItemStack.EMPTY);
                }
                insertOutput(1, recipe.output(), recipe.outCount());
                progress = 0;
            }
        } else if (recipe == null) {
            progress = 0;
        }
    }
}
