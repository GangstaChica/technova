package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalTank;
import com.technova.registry.ModBlockEntities;
import com.technova.registry.ModBlocks;
import com.technova.registry.ModItems;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Controleur du reacteur a fission : multibloc 3x3x3 (26 blocs de carter, un noyau au centre,
 * et le controleur remplace le centre d'une face). Consomme de l'uranium et de l'eau.
 */
public class ReactorControllerBE extends AbstractMachineBE {
    public static final int GENERATION = 300;
    public static final int BURN_TICKS = 3000;
    public static final int WATER_PER_TICK = 1;
    public static final int RADIATION_STRENGTH = 4;

    /** Reacteurs actifs, lus par le gestionnaire de radiation. */
    public static final Set<GlobalPos> ACTIVE = ConcurrentHashMap.newKeySet();

    private final ChemicalTank water;
    private boolean formed;

    public ReactorControllerBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROLLER.get(), pos, state,
                new SlotSpec[] {new SlotSpec(78, 35, false)}, 500_000, 0, 2000);
        this.water = addTank(8000, c -> c == Chemical.WATER, true, false);
    }

    @Override
    public boolean showsAux() {
        return true;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return stack.is(ModItems.URANIUM_INGOT.get());
    }

    public boolean isFormed() {
        return formed;
    }

    /** Verifie la structure : noyau adjacent au controleur, entoure de 25 carters + le controleur. */
    private boolean checkStructure() {
        if (level == null) {
            return false;
        }
        for (Direction d : Direction.values()) {
            BlockPos core = worldPosition.relative(d);
            if (!level.getBlockState(core).is(ModBlocks.REACTOR_CORE.get())) {
                continue;
            }
            boolean ok = true;
            for (int x = -1; x <= 1 && ok; x++) {
                for (int y = -1; y <= 1 && ok; y++) {
                    for (int z = -1; z <= 1 && ok; z++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        BlockPos p = core.offset(x, y, z);
                        if (p.equals(worldPosition)) {
                            continue;
                        }
                        ok = level.getBlockState(p).is(ModBlocks.REACTOR_CASING.get());
                    }
                }
            }
            // le controleur doit etre au centre d'une face : une seule coordonnee differe du noyau
            if (ok) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void tickServer() {
        if (level.getGameTime() % 20L == 0L) {
            formed = checkStructure();
        }
        GlobalPos gp = GlobalPos.of(level.dimension(), worldPosition);
        boolean running = false;

        if (formed) {
            if (aux <= 0) {
                ItemStack fuel = items.get(0);
                if (fuel.is(ModItems.URANIUM_INGOT.get()) && water.getAmount() >= WATER_PER_TICK
                        && energyRoom() >= GENERATION) {
                    fuel.shrink(1);
                    if (fuel.isEmpty()) {
                        items.set(0, ItemStack.EMPTY);
                    }
                    aux = BURN_TICKS;
                    auxMax = BURN_TICKS;
                }
            }
            if (aux > 0 && water.getAmount() >= WATER_PER_TICK && energyRoom() >= GENERATION) {
                water.extract(WATER_PER_TICK, false);
                addEnergy(GENERATION);
                aux--;
                running = true;
            }
        }

        if (running) {
            ACTIVE.add(gp);
        } else {
            ACTIVE.remove(gp);
        }
        pushEnergy(2000);
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            ACTIVE.remove(GlobalPos.of(level.dimension(), worldPosition));
        }
        super.setRemoved();
    }
}
