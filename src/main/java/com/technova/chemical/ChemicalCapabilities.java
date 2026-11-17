package com.technova.chemical;

import com.technova.TechNova;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;

/** Capacite NeoForge "chimie" : permet aux tubes et aux machines de se parler. */
public final class ChemicalCapabilities {
    public static final BlockCapability<ChemicalHandler, Direction> BLOCK = BlockCapability.createSided(
            Identifier.fromNamespaceAndPath(TechNova.MODID, "chemical_handler"), ChemicalHandler.class);

    private ChemicalCapabilities() {}
}
