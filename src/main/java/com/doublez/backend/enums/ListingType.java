package com.doublez.backend.enums;

public enum ListingType {
    
	FOR_SALE("Na prodaju"),
    FOR_RENT("Za izdavanje");

    private final String displayName;

    ListingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}