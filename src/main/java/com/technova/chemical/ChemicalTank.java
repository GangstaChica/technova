package com.technova.chemical;

import java.util.function.Predicate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Reservoir contenant un seul type de produit chimique (en millibuckets). */
public class ChemicalTank implements ChemicalHandler {
    private final int capacity;
    private final Predicate<Chemical> validator;
    private Chemical chemical = Chemical.EMPTY;
    private int amount;

    public ChemicalTank(int capacity, Predicate<Chemical> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public boolean isEmpty() {
        return amount <= 0 || chemical == Chemical.EMPTY;
    }

    /** Le reservoir peut-il recevoir ce produit (type valide et pas de melange) ? */
    public boolean accepts(Chemical c) {
        return c != Chemical.EMPTY && validator.test(c) && (isEmpty() || chemical == c);
    }

    @Override
    public Chemical getChemical() {
        return isEmpty() ? Chemical.EMPTY : chemical;
    }

    @Override
    public int getAmount() {
        return isEmpty() ? 0 : amount;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public int space() {
        return capacity - getAmount();
    }

    @Override
    public int insert(Chemical c, int toInsert, boolean simulate) {
        if (toInsert <= 0 || !accepts(c)) {
            return 0;
        }
        int accepted = Math.min(toInsert, space());
        if (accepted > 0 && !simulate) {
            chemical = c;
            amount = getAmount() + accepted;
        }
        return accepted;
    }

    @Override
    public int extract(int maxAmount, boolean simulate) {
        if (maxAmount <= 0 || isEmpty()) {
            return 0;
        }
        int taken = Math.min(maxAmount, amount);
        if (!simulate) {
            amount -= taken;
            if (amount <= 0) {
                amount = 0;
                chemical = Chemical.EMPTY;
            }
        }
        return taken;
    }

    public void save(ValueOutput out) {
        out.putString("chemical", getChemical().id);
        out.putInt("amount", getAmount());
    }

    public void load(ValueInput in) {
        chemical = Chemical.byId(in.getStringOr("chemical", "empty"));
        amount = Math.min(capacity, in.getIntOr("amount", 0));
        if (chemical == Chemical.EMPTY) {
            amount = 0;
        }
    }
}
