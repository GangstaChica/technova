package com.technova.blockentity;

import com.technova.chemical.Chemical;
import com.technova.chemical.ChemicalCapabilities;
import com.technova.chemical.ChemicalHandler;
import com.technova.chemical.ChemicalTank;
import com.technova.menu.MachineMenu;
import com.technova.registry.ModItems;
import com.technova.util.EnergyUtil;
import com.technova.util.SideEnergy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

/**
 * Base de toutes les machines : inventaire (entonnoirs + tuyaux d'autres mods), stockage
 * d'energie (FE), reservoirs chimiques, upgrades, configuration des faces, donnees
 * synchronisees avec la GUI et sauvegarde.
 */
public abstract class AbstractMachineBE extends BlockEntity implements WorldlyContainer, MenuProvider {

    public enum SlotKind { NORMAL, SPEED_UPGRADE, ENERGY_UPGRADE }

    /** Position d'un slot dans la GUI, s'il s'agit d'un slot de sortie, et son role. */
    public record SlotSpec(int x, int y, boolean output, SlotKind kind) {
        public SlotSpec(int x, int y, boolean output) {
            this(x, y, output, SlotKind.NORMAL);
        }
    }

    /** Nombre d'entiers synchronises vers la GUI (voir {@link #dataAccess}). */
    public static final int DATA_COUNT = 17;
    public static final int MAX_UPGRADES = 4;

    protected final SlotSpec[] slotSpecs;
    protected final NonNullList<ItemStack> items;
    protected final SimpleEnergyHandler energy;
    private final EnergyHandler energyIn;
    private final EnergyHandler energyOut;
    protected final List<ChemicalTank> tanks = new ArrayList<>();
    private final List<Boolean> tankInput = new ArrayList<>();
    private final List<Boolean> tankOutput = new ArrayList<>();
    private final FaceMode[] faces = new FaceMode[6];

    protected int progress;
    protected int maxProgress;
    /** Valeur auxiliaire affichee en barre : temps de combustion, activite... */
    protected int aux;
    protected int auxMax;

    private final Port portBoth = new Port(true, true);
    private final Port portInOnly = new Port(true, false);
    private final Port portOutOnly = new Port(false, true);

    /**
     * Donnees envoyees a la GUI. Les valeurs sont transmises sur 16 bits : l'energie est donc
     * decoupee en deux moities, et les reservoirs sont limites a 32 000 mB.
     */
    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return progress;
                case 1: return maxProgress;
                case 2: return energy.getAmountAsInt() & 0xFFFF;
                case 3: return energy.getAmountAsInt() >>> 16;
                case 4: return energy.getCapacityAsInt() & 0xFFFF;
                case 5: return energy.getCapacityAsInt() >>> 16;
                case 6: return aux;
                case 7: return auxMax;
                default:
                    int tank = (index - 8) / 3;
                    if (tank >= tanks.size()) {
                        return 0;
                    }
                    ChemicalTank t = tanks.get(tank);
                    switch ((index - 8) % 3) {
                        case 0: return t.getChemical().ordinal();
                        case 1: return t.getAmount();
                        default: return t.getCapacity();
                    }
            }
        }

        @Override
        public void set(int index, int value) {
            // Lecture seule : le serveur est la source de verite.
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractMachineBE(BlockEntityType<?> type, BlockPos pos, BlockState state, SlotSpec[] slots,
                                int energyCapacity, int maxInsert, int maxExtract) {
        this(type, pos, state, slots, energyCapacity, maxInsert, maxExtract, false);
    }

    /** @param upgradable ajoute deux slots d'upgrade (vitesse et efficacite energetique). */
    protected AbstractMachineBE(BlockEntityType<?> type, BlockPos pos, BlockState state, SlotSpec[] slots,
                                int energyCapacity, int maxInsert, int maxExtract, boolean upgradable) {
        super(type, pos, state);
        this.slotSpecs = upgradable ? withUpgradeSlots(slots) : slots;
        this.items = NonNullList.withSize(slotSpecs.length, ItemStack.EMPTY);
        this.energy = new SimpleEnergyHandler(Math.max(1, energyCapacity), maxInsert, maxExtract);
        this.energyIn = new SideEnergy(energy, true, false);
        this.energyOut = new SideEnergy(energy, false, true);
        for (Direction d : Direction.values()) {
            faces[d.ordinal()] = defaultFaceMode(d);
        }
    }

    private static SlotSpec[] withUpgradeSlots(SlotSpec[] base) {
        SlotSpec[] all = new SlotSpec[base.length + 2];
        System.arraycopy(base, 0, all, 0, base.length);
        all[base.length] = new SlotSpec(152, 17, false, SlotKind.SPEED_UPGRADE);
        all[base.length + 1] = new SlotSpec(152, 35, false, SlotKind.ENERGY_UPGRADE);
        return all;
    }

    // ------------------------------------------------------------------ logique

    /** Appele a chaque tick serveur par le bloc. */
    public final void serverTick(Level level, BlockPos pos, BlockState state) {
        tickServer();
        if (level.getGameTime() % 20L == 0L) {
            setChanged();
        }
    }

    protected abstract void tickServer();

    // Informations d'affichage pour la GUI.
    public boolean usesEnergy() {
        return true;
    }

    public boolean showsProgress() {
        return false;
    }

    public boolean showsAux() {
        return false;
    }

    public int tankCount() {
        return tanks.size();
    }

    public SlotSpec[] slotSpecs() {
        return slotSpecs;
    }

    protected boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    // ------------------------------------------------------------------ upgrades

    protected int upgrades(SlotKind kind) {
        int n = 0;
        for (int i = 0; i < slotSpecs.length; i++) {
            if (slotSpecs[i].kind() == kind) {
                n += items.get(i).getCount();
            }
        }
        return Math.min(MAX_UPGRADES, n);
    }

    /** Nombre d'unites de progression par tick (1 sans upgrade, jusqu'a 5). */
    protected int speedStep() {
        return 1 + upgrades(SlotKind.SPEED_UPGRADE);
    }

    /** Cout energetique par tick apres upgrades : la vitesse coute cher, l'efficacite fait economiser. */
    protected int scaledEnergy(int base) {
        double factor = (1.0 + 0.5 * upgrades(SlotKind.SPEED_UPGRADE)) / (1.0 + 0.5 * upgrades(SlotKind.ENERGY_UPGRADE));
        return Math.max(1, (int) Math.round(base * factor));
    }

    // ------------------------------------------------------------------ faces

    protected FaceMode defaultFaceMode(Direction side) {
        return FaceMode.ALL;
    }

    public FaceMode faceMode(Direction side) {
        return side == null ? FaceMode.ALL : faces[side.ordinal()];
    }

    /** Passe la face au mode suivant (tout -> entree -> sortie -> off) et previent les voisins. */
    public FaceMode cycleFace(Direction side) {
        FaceMode next = faces[side.ordinal()].next();
        faces[side.ordinal()] = next;
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        return next;
    }

    // ------------------------------------------------------------------ capacites

    public EnergyHandler getEnergyHandler(Direction side) {
        if (!usesEnergy()) {
            return null;
        }
        FaceMode mode = faceMode(side);
        switch (mode) {
            case OFF: return null;
            case INPUT: return energyIn;
            case OUTPUT: return energyOut;
            default: return energy;
        }
    }

    public ChemicalHandler getChemicalHandler(Direction side) {
        if (tanks.isEmpty()) {
            return null;
        }
        switch (faceMode(side)) {
            case OFF: return null;
            case INPUT: return portInOnly;
            case OUTPUT: return portOutOnly;
            default: return portBoth;
        }
    }

    /** Inventaire expose aux tuyaux d'objets d'autres mods (API de transfert de NeoForge). */
    public ResourceHandler<ItemResource> getItemHandler(Direction side) {
        if (slotSpecs.length == 0 || faceMode(side) == FaceMode.OFF) {
            return null;
        }
        return new WorldlyContainerWrapper(this, side);
    }

    protected ChemicalTank addTank(int capacity, Predicate<Chemical> validator, boolean input, boolean output) {
        ChemicalTank tank = new ChemicalTank(capacity, validator);
        tanks.add(tank);
        tankInput.add(input);
        tankOutput.add(output);
        return tank;
    }

    /** Pousse l'energie vers les faces autorisees en sortie. */
    protected void pushEnergy(int rate) {
        for (Direction d : Direction.values()) {
            pushEnergyTo(d, rate);
        }
    }

    protected void pushEnergyTo(Direction d, int rate) {
        if (!faceMode(d).allowsOutput()) {
            return;
        }
        BlockPos np = worldPosition.relative(d);
        if (level == null || !level.isLoaded(np)) {
            return;
        }
        EnergyHandler other = level.getCapability(Capabilities.Energy.BLOCK, np, d.getOpposite());
        if (other != null) {
            EnergyUtil.transfer(energy, other, rate);
        }
    }

    /** Pousse le contenu des reservoirs de sortie vers les voisins (tubes, machines...). */
    protected void pushChemicals(int rate) {
        for (Direction d : Direction.values()) {
            pushChemicalsTo(d, rate);
        }
    }

    protected void pushChemicalsTo(Direction d, int rate) {
        if (!faceMode(d).allowsOutput()) {
            return;
        }
        BlockPos np = worldPosition.relative(d);
        if (level == null || !level.isLoaded(np)) {
            return;
        }
        ChemicalHandler other = null;
        for (int i = 0; i < tanks.size(); i++) {
            ChemicalTank tank = tanks.get(i);
            if (!tankOutput.get(i) || tank.isEmpty()) {
                continue;
            }
            if (other == null) {
                other = level.getCapability(ChemicalCapabilities.BLOCK, np, d.getOpposite());
                if (other == null) {
                    return;
                }
            }
            Chemical chemical = tank.getChemical();
            int sim = other.insert(chemical, Math.min(rate, tank.getAmount()), true);
            if (sim > 0) {
                int taken = tank.extract(sim, false);
                other.insert(chemical, taken, false);
            }
        }
    }

    protected void addEnergy(int amount) {
        energy.set(Math.min(energy.getCapacityAsInt(), energy.getAmountAsInt() + amount));
    }

    protected void useEnergy(int amount) {
        energy.set(Math.max(0, energy.getAmountAsInt() - amount));
    }

    protected int energyRoom() {
        return energy.getCapacityAsInt() - energy.getAmountAsInt();
    }

    protected boolean outputFits(int slot, Item item, int count) {
        ItemStack out = items.get(slot);
        return out.isEmpty() || (out.is(item) && out.getCount() + count <= out.getMaxStackSize());
    }

    protected void insertOutput(int slot, Item item, int count) {
        ItemStack out = items.get(slot);
        if (out.isEmpty()) {
            items.set(slot, new ItemStack(item, count));
        } else {
            out.grow(count);
        }
    }

    public void dropAll() {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, worldPosition, this);
            clearContent();
        }
    }

    // ------------------------------------------------------------------ sauvegarde

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output.child("energy"));
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).save(output.child("tank" + i));
        }
        for (int i = 0; i < faces.length; i++) {
            output.putInt("face" + i, faces[i].ordinal());
        }
        output.putInt("progress", progress);
        output.putInt("aux", aux);
        output.putInt("auxMax", auxMax);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, items);
        energy.deserialize(input.childOrEmpty("energy"));
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).load(input.childOrEmpty("tank" + i));
        }
        for (int i = 0; i < faces.length; i++) {
            faces[i] = FaceMode.byOrdinal(input.getIntOr("face" + i, defaultFaceMode(Direction.values()[i]).ordinal()));
        }
        progress = input.getIntOr("progress", 0);
        aux = input.getIntOr("aux", 0);
        auxMax = input.getIntOr("auxMax", 0);
    }

    // ------------------------------------------------------------------ menu

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MachineMenu(containerId, inventory, this, this, dataAccess);
    }

    // ------------------------------------------------------------------ inventaire

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        SlotSpec spec = slotSpecs[slot];
        switch (spec.kind()) {
            case SPEED_UPGRADE: return stack.is(ModItems.SPEED_UPGRADE.get());
            case ENERGY_UPGRADE: return stack.is(ModItems.ENERGY_UPGRADE.get());
            default: return !spec.output() && isItemValid(slot, stack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (faceMode(side) == FaceMode.OFF) {
            return new int[0];
        }
        int[] all = new int[items.size()];
        for (int i = 0; i < all.length; i++) {
            all[i] = i;
        }
        return all;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slotSpecs[slot].kind() == SlotKind.NORMAL && faceMode(side).allowsInput() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slotSpecs[slot].kind() == SlotKind.NORMAL && slotSpecs[slot].output() && faceMode(side).allowsOutput();
    }

    // ------------------------------------------------------------------ port chimique

    /** Vue "reseau" des reservoirs : entree dans les reservoirs d'entree, sortie des sorties. */
    protected final class Port implements ChemicalHandler {
        private final boolean canInsert;
        private final boolean canExtract;

        Port(boolean canInsert, boolean canExtract) {
            this.canInsert = canInsert;
            this.canExtract = canExtract;
        }

        private ChemicalTank display() {
            for (int i = 0; i < tanks.size(); i++) {
                if (tankOutput.get(i)) {
                    return tanks.get(i);
                }
            }
            return tanks.get(0);
        }

        @Override
        public Chemical getChemical() {
            return display().getChemical();
        }

        @Override
        public int getAmount() {
            return display().getAmount();
        }

        @Override
        public int getCapacity() {
            return display().getCapacity();
        }

        @Override
        public int insert(Chemical chemical, int amount, boolean simulate) {
            if (!canInsert) {
                return 0;
            }
            int target = -1;
            // 1) un reservoir d'entree contient deja ce produit
            for (int i = 0; i < tanks.size(); i++) {
                if (tankInput.get(i) && tanks.get(i).getChemical() == chemical && !tanks.get(i).isEmpty()) {
                    target = i;
                    break;
                }
            }
            // 2) sinon un reservoir d'entree vide qui l'accepte
            if (target < 0) {
                for (int i = 0; i < tanks.size(); i++) {
                    if (tankInput.get(i) && tanks.get(i).isEmpty() && tanks.get(i).accepts(chemical)) {
                        target = i;
                        break;
                    }
                }
            }
            return target < 0 ? 0 : tanks.get(target).insert(chemical, amount, simulate);
        }

        @Override
        public int extract(int maxAmount, boolean simulate) {
            if (!canExtract) {
                return 0;
            }
            for (int i = 0; i < tanks.size(); i++) {
                if (tankOutput.get(i) && !tanks.get(i).isEmpty()) {
                    return tanks.get(i).extract(maxAmount, simulate);
                }
            }
            return 0;
        }
    }
}
