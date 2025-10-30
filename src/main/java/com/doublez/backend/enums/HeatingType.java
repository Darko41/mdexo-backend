package com.doublez.backend.enums;

public enum HeatingType {
    CENTRAL("Central Heating"),
    DISTRICT("District Heating"),
    ELECTRIC("Electric Heating"),
    GAS("Gas Heating"),
    HEAT_PUMP("Heat Pump"),
    SOLAR("Solar Heating"),
    WOOD_PELLET("Wood Pellet"),
    OIL("Oil Heating"),
    NONE("No Heating"),
    OTHER("Other");

    private final String displayName;

    HeatingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
