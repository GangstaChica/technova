package com.technova.blockentity;

/**
 * Mode d'une face de machine, applique a tous les types de transfert (objets, energie,
 * produits chimiques). On le change avec le configurateur (Maj + clic droit).
 */
public enum FaceMode {
    ALL("all"),
    INPUT("input"),
    OUTPUT("output"),
    OFF("off");

    private static final FaceMode[] VALUES = values();

    public final String id;

    FaceMode(String id) {
        this.id = id;
    }

    public boolean allowsInput() {
        return this == ALL || this == INPUT;
    }

    public boolean allowsOutput() {
        return this == ALL || this == OUTPUT;
    }

    public FaceMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public String translationKey() {
        return "face_mode.technova." + id;
    }

    public static FaceMode byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : ALL;
    }
}
