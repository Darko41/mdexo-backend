package com.doublez.backend.enums.property;

public enum FurnitureStatus {
    FULLY_FURNISHED("Potpuno namešteno"),
    SEMI_FURNISHED("Polunamešteno"),
    PARTIALLY_FURNISHED("Delimično namešteno"),
    UNFURNISHED("Prazno");

    private final String displayName;

    FurnitureStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


