package com.technova.chemical;

/**
 * Les produits chimiques du mod (gaz et liquides). Comme dans Mekanism, ils ont leur propre
 * systeme de transport, distinct des fluides vanilla.
 */
public enum Chemical {
    EMPTY("empty", 0xFF000000, false),
    WATER("water", 0xFF3F76E4, false),
    HYDROGEN("hydrogen", 0xFFE8F4FF, true),
    OXYGEN("oxygen", 0xFF8FD3FF, true),
    CHLORINE("chlorine", 0xFFB6E34A, true),
    HYDROGEN_CHLORIDE("hydrogen_chloride", 0xFFE8D86A, true),
    SULFUR_DIOXIDE("sulfur_dioxide", 0xFFD9A441, true),
    SULFUR_TRIOXIDE("sulfur_trioxide", 0xFFE07B39, true),
    SULFURIC_ACID("sulfuric_acid", 0xFFC7B21F, false);

    private static final Chemical[] VALUES = values();

    public final String id;
    public final int color;
    public final boolean gas;

    Chemical(String id, int color, boolean gas) {
        this.id = id;
        this.color = color;
        this.gas = gas;
    }

    public String translationKey() {
        return "chemical.technova." + id;
    }

    public static Chemical byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : EMPTY;
    }

    public static Chemical byId(String id) {
        for (Chemical c : VALUES) {
            if (c.id.equals(id)) {
                return c;
            }
        }
        return EMPTY;
    }
}
