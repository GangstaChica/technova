package com.technova.blockentity;

import com.technova.chemical.ChemicalTank;
import com.technova.recipe.MachineRecipes;
import com.technova.recipe.MachineRecipes.OxidizeRecipe;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Transforme des objets (soufre, sel) en gaz : dioxyde de soufre, chlore. */
public class ChemicalOxidizerBE extends AbstractMachineBE {
    private static final int ENERGY_PER_TICK = 30;
    private static final int TICKS = 100;

    private final ChemicalTank out;

    public ChemicalOxidizerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_OXIDIZER.get(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 35, false)}, 20000, 400, 0, true);
        this.out = addTank(10000, c -> true, false, true);
        this.maxProgress = TICKS;
    }

    @Override
    public boolean showsProgress() {
        return true;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return MachineRecipes.findOxidize(stack) != null;
    }

    @Override
    protected void tickServer() {
        ItemStack input = items.get(0);
        OxidizeRecipe recipe = MachineRecipes.findOxidize(input);
        int cost = scaledEnergy(ENERGY_PER_TICK);
        boolean canRun = recipe != null
                && out.accepts(recipe.out())
                && out.space() >= recipe.amount()
                && energy.getAmountAsInt() >= cost;
        if (canRun) {
            useEnergy(cost);
            progress += speedStep();
            if (progress >= TICKS) {
                input.shrink(1);
                if (input.isEmpty()) {
                    items.set(0, ItemStack.EMPTY);
                }
                out.insert(recipe.out(), recipe.amount(), false);
                progress = 0;
            }
        } else if (recipe == null) {
            progress = 0;
        }
        pushChemicals(500);
    }
}
