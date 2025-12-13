package com.doublez.backend.enums;

public enum CreditTransactionType {
    PURCHASE("Purchase"),
    USAGE("Usage"),
    REFUND("Refund"),
    BONUS("Bonus"),
    TRANSFER("Transfer"),
    EXPIRATION("Expiration"),
    AGENCY_PURCHASE("Agency Purchase"),
    TEAM_DISTRIBUTION("Team Distribution"),
    AGENCY_DISTRIBUTION("Agency Distribution");
    
    private final String description;
    
    CreditTransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}