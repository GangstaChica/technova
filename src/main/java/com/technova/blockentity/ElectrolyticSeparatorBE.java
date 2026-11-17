package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalTank;
import com.technova.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electrolyse de l'eau : 2 mB d'eau -> 2 mB d'hydrogene + 1 mB d'oxygene par tick.
 * L'eau arrive par seau (slot du haut) ou par tube ; les seaux vides sortent en bas.
 */
public class ElectrolyticSeparatorBE extends AbstractMachineBE {
    private static final int ENERGY_PER_TICK = 50;
    private static final int BUCKET = 1000;

    private final ChemicalTank water;
    private final ChemicalTank hydrogen;
    private final ChemicalTank oxygen;

    public ElectrolyticSeparatorBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYTIC_SEPARATOR.get(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 17, false), new SlotSpec(78, 53, true)}, 30000, 600, 0, true);
        this.water = addTank(10000, c -> c == Chemical.WATER, true, false);
        this.hydrogen = addTank(10000, c -> true, false, true);
        this.oxygen = addTank(10000, c -> true, false, true);
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return stack.is(Items.WATER_BUCKET);
    }

    @Override
    protected void tickServer() {
        // Vidage d'un seau d'eau dans le reservoir.
        ItemStack in = items.get(0);
        if (in.is(Items.WATER_BUCKET) && water.space() >= BUCKET) {
            ItemStack out = items.get(1);
            if (out.isEmpty() || (out.is(Items.BUCKET) && out.getCount() < out.getMaxStackSize())) {
                in.shrink(1);
                if (in.isEmpty()) {
                    items.set(0, ItemStack.EMPTY);
                }
                water.insert(Chemical.WATER, BUCKET, false);
                if (out.isEmpty()) {
                    items.set(1, new ItemStack(Items.BUCKET));
                } else {
                    out.grow(1);
                }
            }
        }

        int cost = scaledEnergy(ENERGY_PER_TICK);
        boolean active = false;
        for (int n = 0; n < speedStep(); n++) {
            if (water.getAmount() >= 2 && hydrogen.space() >= 2 && oxygen.space() >= 1
                    && energy.getAmountAsInt() >= cost) {
                water.extract(2, false);
                hydrogen.insert(Chemical.HYDROGEN, 2, false);
                oxygen.insert(Chemical.OXYGEN, 1, false);
                useEnergy(cost);
                active = true;
            } else {
                break;
            }
        }
        aux = active ? 1 : 0;
        auxMax = 1;
        pushChemicals(500);
    }
}
