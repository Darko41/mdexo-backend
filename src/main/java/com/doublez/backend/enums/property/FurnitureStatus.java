package com.doublez.backend.enums.property;

public enum FurnitureStatus {
    FURNISHED("Potpuno namešteno"),
    SEMI_FURNISHED("Polunamešteno"), 
    UNFURNISHED("Prazno"),
    PARTIALLY_FURNISHED("Delimično namješteno");
    
    private final String displayName;
    private FurnitureStatus furnitureStatus; // User selects from dropdown
    
    FurnitureStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}