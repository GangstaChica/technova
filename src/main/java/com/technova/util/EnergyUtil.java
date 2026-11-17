package com.technova.util;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class EnergyUtil {
    private EnergyUtil() {}

    /**
     * Deplace de l'energie d'un handler vers un autre, de facon atomique (API de transactions
     * de NeoForge 21.9+). @return la quantite reellement deplacee.
     */
    public static int transfer(EnergyHandler from, EnergyHandler to, int max) {
        if (max <= 0) {
            return 0;
        }
        // 1) simulation de l'extraction (la transaction n'est pas validee -> annulee)
        int available;
        try (Transaction tx = Transaction.openRoot()) {
            available = from.extract(max, tx);
        }
        if (available <= 0) {
            return 0;
        }
        // 2) simulation de l'insertion
        int accepted;
        try (Transaction tx = Transaction.openRoot()) {
            accepted = to.insert(available, tx);
        }
        if (accepted <= 0) {
            return 0;
        }
        // 3) transfert reel
        try (Transaction tx = Transaction.openRoot()) {
            int out = from.extract(accepted, tx);
            int in = to.insert(out, tx);
            if (out == accepted && in == accepted) {
                tx.commit();
                return accepted;
            }
        }
        return 0;
    }
}
