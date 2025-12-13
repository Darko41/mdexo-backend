package com.doublez.backend.enums.subscription;

import java.math.BigDecimal;

public enum SubscriptionPeriod {
    MONTHLY("Mesečno", 1),
    QUARTERLY("Kvartalno", 3),
    SEMI_ANNUAL("Polugodišnje", 6),
    ANNUAL("Godišnje", 12);
    
    private final String displayName;
    private final int months;
    
    SubscriptionPeriod(String displayName, int months) {
        this.displayName = displayName;
        this.months = months;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMonths() {
        return months;
    }
    
    public BigDecimal calculateTotalPrice(BigDecimal monthlyPrice) {
        return monthlyPrice.multiply(BigDecimal.valueOf(months));
    }
}
