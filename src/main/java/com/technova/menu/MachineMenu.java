package com.technova.menu;

import com.technova.blockentity.AbstractMachineBE;
import com.technova.chemical.Chemical;
import com.technova.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Menu commun a toutes les machines : la disposition des slots vient du bloc entite. */
public class MachineMenu extends AbstractContainerMenu {
    public final AbstractMachineBE be;
    public final ContainerData data;
    private final Container container;
    private final int machineSlots;

    public MachineMenu(int id, Inventory inv, AbstractMachineBE be, Container container, ContainerData data) {
        super(ModMenus.MACHINE.get(), id);
        this.be = be;
        this.container = container;
        this.data = data;

        AbstractMachineBE.SlotSpec[] specs = be.slotSpecs();
        this.machineSlots = specs.length;
        for (int i = 0; i < specs.length; i++) {
            this.addSlot(new MachineSlot(container, i, specs[i].x(), specs[i].y(), specs[i].output()));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        this.addDataSlots(data);
    }

    /** Constructeur cote client : le contenu et les donnees arrivent par paquets reseau. */
    public static MachineMenu create(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof AbstractMachineBE machine) {
            return new MachineMenu(id, inv, machine, new SimpleContainer(machine.getContainerSize()),
                    new SimpleContainerData(AbstractMachineBE.DATA_COUNT));
        }
        throw new IllegalStateException("Aucune machine en " + pos);
    }

    // --- Accesseurs pour l'ecran ---

    public int progress() {
        return data.get(0);
    }

    public int maxProgress() {
        return data.get(1);
    }

    public int energy() {
        return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16);
    }

    public int energyCapacity() {
        return (data.get(4) & 0xFFFF) | ((data.get(5) & 0xFFFF) << 16);
    }

    public int aux() {
        return data.get(6);
    }

    public int auxMax() {
        return data.get(7);
    }

    public Chemical tankChemical(int tank) {
        return Chemical.byOrdinal(data.get(8 + tank * 3));
    }

    public int tankAmount(int tank) {
        return data.get(9 + tank * 3) & 0xFFFF;
    }

    public int tankCapacity(int tank) {
        return data.get(10 + tank * 3) & 0xFFFF;
    }

    // --- Comportement du menu ---

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int end = machineSlots;
            if (index < end) {
                // machine -> inventaire du joueur
                if (!this.moveItemStackTo(stack, end, end + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, end, false)) {
                // inventaire <-> barre d'acces rapide
                if (index < end + 27) {
                    if (!this.moveItemStackTo(stack, end + 27, end + 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stack, end, end + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /** Slot de machine : les slots de sortie refusent tout depot. */
    private static final class MachineSlot extends Slot {
        private final boolean output;

        MachineSlot(Container container, int index, int x, int y, boolean output) {
            super(container, index, x, y);
            this.output = output;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !output && this.container.canPlaceItem(this.getContainerSlot(), stack);
        }
    }
}
