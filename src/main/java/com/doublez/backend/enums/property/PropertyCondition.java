package com.doublez.backend.enums.property;

public enum PropertyCondition {
    NEW_CONSTRUCTION("Novogradnja"),
    RENOVATED("Renovirano"),
    MODERNIZED("Modernizovano"),
    GOOD("Dobro stanje"),
    NEEDS_RENOVATION("Potrebno renoviranje"),
    ORIGINAL("Izvorno stanje"),
    LUXURY("Luksuz"),
    SHELL("Siva faza");

    private final String displayName;

    PropertyCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


