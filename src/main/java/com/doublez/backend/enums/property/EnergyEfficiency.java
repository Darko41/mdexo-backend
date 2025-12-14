package com.doublez.backend.enums.property;

public enum EnergyEfficiency {
    A_PLUS("A+"),
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    G("G"),
    NOT_RATED("Nije ocenjeno");

    private final String displayName;

    EnergyEfficiency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


