package com.doublez.backend.enums;

public enum InvestorType {
    
	DEVELOPER("Investitor građevinar"), 
    WHOLESALER("Veletrgovac nekretninama"),
    FLIPPER("Spekulant"),
    PRIVATE("Privatni investitor"),
    INTERNATIONAL("Inostrani investitor");

    private final String displayName;

    InvestorType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
