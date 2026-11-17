package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalTank;
import com.technova.recipe.MachineRecipes;
import com.technova.recipe.MachineRecipes.ItemRecipe;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Machines "objet + gaz -> objets multiplies" : purification (oxygene, x3), injection
 * (chlorure d'hydrogene, x4) et dissolution (acide sulfurique, x5).
 */
public class ChemicalProcessorBE extends AbstractMachineBE {

    public enum Kind {
        PURIFICATION(MachineRecipes.PURIFICATION, Chemical.OXYGEN, 40, 200),
        INJECTION(MachineRecipes.INJECTION, Chemical.HYDROGEN_CHLORIDE, 50, 250),
        DISSOLUTION(MachineRecipes.DISSOLUTION, Chemical.SULFURIC_ACID, 60, 300);

        final String recipeKey;
        final Chemical chemical;
        final int energyPerTick;
        final int ticks;

        Kind(String recipeKey, Chemical chemical, int energyPerTick, int ticks) {
            this.recipeKey = recipeKey;
            this.chemical = chemical;
            this.energyPerTick = energyPerTick;
            this.ticks = ticks;
        }

        BlockEntityType<ChemicalProcessorBE> type() {
            switch (this) {
                case PURIFICATION: return ModBlockEntities.PURIFICATION_CHAMBER.get();
                case INJECTION: return ModBlockEntities.INJECTION_CHAMBER.get();
                default: return ModBlockEntities.DISSOLUTION_CHAMBER.get();
            }
        }
    }

    private final Kind kind;
    private final ChemicalTank gas;

    public ChemicalProcessorBE(Kind kind, BlockPos pos, BlockState state) {
        super(kind.type(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 35, false), new SlotSpec(130, 35, true)},
                30000, 600, 0, true);
        this.kind = kind;
        this.maxProgress = kind.ticks;
        this.gas = addTank(10000, c -> c == kind.chemical, true, false);
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
                && gas.getAmount() >= recipe.chemAmount()
                && energy.getAmountAsInt() >= cost;

        if (canRun) {
            useEnergy(cost);
            progress += speedStep();
            if (progress >= kind.ticks) {
                input.shrink(recipe.inCount());
                if (input.isEmpty()) {
                    items.set(0, ItemStack.EMPTY);
                }
                gas.extract(recipe.chemAmount(), false);
                insertOutput(1, recipe.output(), recipe.outCount());
                progress = 0;
            }
        } else if (recipe == null) {
            progress = 0;
        }
    }
}
