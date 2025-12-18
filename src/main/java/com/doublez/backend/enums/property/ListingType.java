package com.doublez.backend.enums.property;

public enum ListingType {
    
	FOR_SALE("Na prodaju"),
    FOR_RENT("Za izdavanje"),
	FOR_SALE_OR_RENT("Na prodaju ili izdavanje");

    private final String displayName;

    ListingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}