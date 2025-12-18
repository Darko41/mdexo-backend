package com.doublez.backend.enums.property;

public enum FeatureCategory {

    ADDITIONAL("Dodatno"),
    AMENITIES("Ostalo"),
    LEGAL("Pravno"),
    ENERGY("Energetika"),
    COMFORT("Komfor"),
    SECURITY("Bezbednost"),
    STRUCTURE("Struktura");

    private final String displayName;

    FeatureCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}