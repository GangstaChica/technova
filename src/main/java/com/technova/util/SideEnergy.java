package com.technova.util;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Vue d'un stockage d'energie qui n'autorise que l'entree ou que la sortie (selon la face). */
public final class SideEnergy implements EnergyHandler {
    private final EnergyHandler delegate;
    private final boolean canInsert;
    private final boolean canExtract;

    public SideEnergy(EnergyHandler delegate, boolean canInsert, boolean canExtract) {
        this.delegate = delegate;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public long getAmountAsLong() {
        return delegate.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return delegate.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return canInsert ? delegate.insert(amount, transaction) : 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return canExtract ? delegate.extract(amount, transaction) : 0;
    }
}
