package com.doublez.backend.enums;

public enum CreditPackageType {
    TIER_SUBSCRIPTION("Pretplata na tier"),
    FEATURE("Funkcija/Paket"),
    ONE_TIME("Jednokratna kupovina"),
    PROMOTIONAL("Promotivni"),
    AGENCY_TIER_SUBSCRIPTION("Agencijska pretplata"),
    AGENCY_CREDIT_PACKAGE("Agencijski kreditni paket"),
    SUPER_AGENT_SEAT("Super agent mesto"),
    TEAM_FEATURE_PACKAGE("Timski funkcionalni paket");

    private final String description;

    // Constructor
    CreditPackageType(String description) {
        this.description = description;
    }

    // Constructor for PROMOTIONAL without parameters (if needed)
    CreditPackageType() {
        this.description = this.name(); // or default description
    }

    // Getter
    public String getDescription() {
        return description;
    }

    // Optional: Override toString() to return description
    @Override
    public String toString() {
        return description;
    }
}
