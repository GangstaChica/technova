package com.technova.chemical;

/**
 * Interface de transfert de produits chimiques entre blocs (equivalent du ResourceHandler de
 * NeoForge, en version simplifiee : un seul reservoir visible par face).
 */
public interface ChemicalHandler {
    Chemical getChemical();

    int getAmount();

    int getCapacity();

    /** @return la quantite reellement acceptee (0 si refuse). */
    int insert(Chemical chemical, int amount, boolean simulate);

    /** Extrait jusqu'a {@code maxAmount} du produit contenu. @return la quantite extraite. */
    int extract(int maxAmount, boolean simulate);
}
