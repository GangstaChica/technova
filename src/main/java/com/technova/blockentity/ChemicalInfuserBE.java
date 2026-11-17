package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalTank;
import com.technova.recipe.MachineRecipes;
import com.technova.recipe.MachineRecipes.InfuseRecipe;
import com.technova.registry.ModBlockEntities;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Combine deux produits chimiques en un troisieme (ex. H2 + Cl2 -> HCl). */
public class ChemicalInfuserBE extends AbstractMachineBE {
    private static final int ENERGY_PER_TICK = 40;
    private static final Set<Chemical> INPUTS = Set.of(Chemical.HYDROGEN, Chemical.CHLORINE, Chemical.SULFUR_DIOXIDE,
            Chemical.SULFUR_TRIOXIDE, Chemical.OXYGEN, Chemical.WATER);

    private final ChemicalTank left;
    private final ChemicalTank right;
    private final ChemicalTank out;

    public ChemicalInfuserBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_INFUSER.get(), pos, state, new SlotSpec[0], 30000, 600, 0, true);
        this.left = addTank(8000, INPUTS::contains, true, false);
        this.right = addTank(8000, INPUTS::contains, true, false);
        this.out = addTank(8000, c -> true, false, true);
    }

    @Override
    protected void tickServer() {
        int cost = scaledEnergy(ENERGY_PER_TICK);
        boolean active = false;
        // Chaque upgrade de vitesse ajoute une reaction par tick.
        for (int n = 0; n < speedStep(); n++) {
            if (!react(cost)) {
                break;
            }
            active = true;
        }
        aux = active ? 1 : 0;
        auxMax = 1;
        pushChemicals(500);
    }

    private boolean react(int cost) {
        if (energy.getAmountAsInt() < cost) {
            return false;
        }
        for (InfuseRecipe r : MachineRecipes.infuseRecipes()) {
            ChemicalTank x = null;
            ChemicalTank y = null;
            if (matches(left, right, r)) {
                x = left;
                y = right;
            } else if (matches(right, left, r)) {
                x = right;
                y = left;
            }
            if (x == null || !out.accepts(r.out()) || out.space() < r.outAmount()) {
                continue;
            }
            x.extract(r.aAmount(), false);
            y.extract(r.bAmount(), false);
            out.insert(r.out(), r.outAmount(), false);
            useEnergy(cost);
            return true;
        }
        return false;
    }

    private static boolean matches(ChemicalTank x, ChemicalTank y, InfuseRecipe r) {
        return x.getChemical() == r.a() && y.getChemical() == r.b()
                && x.getAmount() >= r.aAmount() && y.getAmount() >= r.bAmount();
    }
}
