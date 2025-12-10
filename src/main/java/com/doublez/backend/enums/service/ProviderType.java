package com.doublez.backend.enums.service;

public enum ProviderType {
    
	INDIVIDUAL("Pojedinačni majstor"),          // Single person
    FREELANCE_TEAM("Tim pojedinaca"),           // Small team without business registration
    REGISTERED_COMPANY("Registrovana firma"),   // Legal business entity
    ENTERPRISE("Korporacija");                  // Large company
    
    private final String displayName;

    ProviderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
