package com.doublez.backend.entity.user;

public enum UserRole {
    
	USER("Korisnik"),                    // Browsing only
    OWNER("Vlasnik nekretnina"),         // Individual property owners
    BUSINESS("Pravno lice"),             // Corporations, banks
    AGENCY("Agencija za nekretnine"),    // Real estate agencies
    INVESTOR("Investitor"),              // Property investors
    CONTRACTOR("Izvođač radova");        // Service providers (renamed from SERVICE_PROVIDER)
    
    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
