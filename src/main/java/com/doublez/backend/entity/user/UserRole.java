package com.doublez.backend.entity.user;

/**
 * Account-level role. Determines the user's primary identity on the platform
 * and maps to the Spring Security authority "ROLE_<name>" via getRolesAsList().
 *
 * NOT to be confused with AgentRole, which describes a user's role *within* an
 * agency (OWNER/SUPER_AGENT/AGENT) and is stored on the Agent entity.
 *
 * Users can have multiple UserRoles (e.g., ROLE_USER is always added as base),
 * but only one AgentRole per agency.
 */

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
