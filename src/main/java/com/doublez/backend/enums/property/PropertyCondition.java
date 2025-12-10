package com.doublez.backend.enums.property;

public enum PropertyCondition {
    
	NEW_CONSTRUCTION("Novogradnja"),
    RENOVATED("Renovirano"),
    MODERNIZED("Modernizovano"),
    GOOD("Dobro stanje, bez ulaganja"),
    NEEDS_RENOVATION("Potrebno renoviranje"),
    ORIGINAL("Izvorno stanje"),
    LUXURY("Luksuzna izrada"),
    SHELL("Siva faza, konstrukcija");

    private final String displayName;

    PropertyCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
