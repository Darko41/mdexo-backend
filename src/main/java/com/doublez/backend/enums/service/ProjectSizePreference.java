package com.doublez.backend.enums.service;

public enum ProjectSizePreference {
    
	SMALL_JOBS("Mali poslovi (do 500€)"),
    MEDIUM_PROJECTS("Srednji projekti (500€ - 5.000€)"),
    LARGE_PROJECTS("Veliki projekti (5.000€ - 50.000€)"),
    ENTERPRISE_CONTRACTS("Korporativni ugovori (50.000€+)"),
    ANY_SIZE("Sve veličine projekata");
    
    private final String displayName;

    ProjectSizePreference(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
