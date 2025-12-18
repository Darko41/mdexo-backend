package com.doublez.backend.enums.property;

public enum OwnershipType {
    FREEHOLD("Svojina"),
    LEASEHOLD("Zakup"),
    STATE_OWNED("Državno"),
    COOPERATIVE("Zadružno"),
    OTHER("Ostalo");

    private final String displayName;

    OwnershipType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
