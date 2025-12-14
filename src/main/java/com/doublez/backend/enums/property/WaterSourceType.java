package com.doublez.backend.enums.property;

public enum WaterSourceType {
    CITY_NETWORK("Gradski vodovod"),
    WELL("Bunar"),
    SPRING("Izvor"),
    RAINWATER("Kišnica"),
    TANK("Rezervoar"),
    OTHER("Ostalo");

    private final String displayName;

    WaterSourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


