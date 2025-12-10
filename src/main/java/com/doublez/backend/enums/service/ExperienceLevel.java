package com.doublez.backend.enums.service;

public enum ExperienceLevel {
    
	BEGINNER("Početnik (0-2 godine)"),
    EXPERIENCED("Iskusan majstor (2-5 godina)"),
    SEASONED("Veteran zanata (5-10 godina)"),
    EXPERT("Ekspert (10+ godina)"),
    LICENSED("Licencirani majstor sa certifikatima");
    
    private final String displayName;

    ExperienceLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
