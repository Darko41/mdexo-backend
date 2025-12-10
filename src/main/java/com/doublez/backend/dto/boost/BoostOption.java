package com.doublez.backend.dto.boost;

import java.math.BigDecimal;

import com.doublez.backend.config.CreditPricingConfiguration;

public class BoostOption {
    private String boostType;
    private String displayName;
    private int creditCost;
    private boolean canAfford;
    private int rsdCost;
    
    public BoostOption(String boostType, String displayName, int creditCost, int userCredits) {
        this.boostType = boostType;
        this.displayName = displayName;
        this.creditCost = creditCost;
        this.canAfford = userCredits >= creditCost;
        this.rsdCost = creditCost; // 1:1 with RSD
    }
    
    // Getters and setters
    public String getBoostType() { return boostType; }
    public void setBoostType(String boostType) { this.boostType = boostType; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public int getCreditCost() { return creditCost; }
    public void setCreditCost(int creditCost) { this.creditCost = creditCost; }
    
    public boolean isCanAfford() { return canAfford; }
    public void setCanAfford(boolean canAfford) { this.canAfford = canAfford; }
    
    public int getRsdCost() { return rsdCost; }
    public void setRsdCost(int rsdCost) { this.rsdCost = rsdCost; }
}