package com.doublez.backend.enums.service;

public enum ServiceScope {
    
	RESIDENTIAL("Stambeni objekti"),
    COMMERCIAL("Komercijalni objekti"), 
    INDUSTRIAL("Industrijski objekti"),
    ALL("Svi tipovi objekata");
    
    private final String displayName;

    ServiceScope(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
