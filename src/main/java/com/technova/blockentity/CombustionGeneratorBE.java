package com.technova.blockentity;

import com.technova.recipe.MachineRecipes;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Brule du combustible (charbon, bois...) pour produire de l'energie. */
public class CombustionGeneratorBE extends AbstractMachineBE {
    public static final int GENERATION = 40; // FE par tick

    public CombustionGeneratorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBUSTION_GENERATOR.get(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 35, false)}, 40000, 0, 400);
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return MachineRecipes.burnTime(stack) > 0;
    }

    @Override
    public boolean showsAux() {
        return true;
    }

    @Override
    protected void tickServer() {
        if (aux > 0) {
            // La combustion se met en pause quand le stockage est plein.
            if (energyRoom() >= GENERATION) {
                aux--;
                addEnergy(GENERATION);
            }
        } else if (energyRoom() >= GENERATION * 20) {
            ItemStack fuel = items.get(0);
            int burn = MachineRecipes.burnTime(fuel);
            if (burn > 0) {
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    items.set(0, ItemStack.EMPTY);
                }
                aux = burn;
                auxMax = burn;
            }
        }
        pushEnergy(400);
    }
}
