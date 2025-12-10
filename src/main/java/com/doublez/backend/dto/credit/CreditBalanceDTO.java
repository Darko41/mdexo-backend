package com.doublez.backend.dto.credit;

import java.time.LocalDateTime;

public class CreditBalanceDTO {
    private Integer currentBalance;
    private Integer lifetimeCreditsEarned;
    private Integer lifetimeCreditsSpent;
    private LocalDateTime lastUpdated;

    // Constructors
    public CreditBalanceDTO() {}

    public CreditBalanceDTO(Integer currentBalance, Integer lifetimeCreditsEarned, 
                           Integer lifetimeCreditsSpent, LocalDateTime lastUpdated) {
        this.currentBalance = currentBalance;
        this.lifetimeCreditsEarned = lifetimeCreditsEarned;
        this.lifetimeCreditsSpent = lifetimeCreditsSpent;
        this.lastUpdated = lastUpdated;
    }

    // Helper methods
    public boolean hasSufficientCredits(Integer required) {
        return currentBalance >= required;
    }

    public Integer getNetCredits() {
        return lifetimeCreditsEarned - lifetimeCreditsSpent;
    }

    // Getters and Setters
    public Integer getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Integer currentBalance) { this.currentBalance = currentBalance; }
    public Integer getLifetimeCreditsEarned() { return lifetimeCreditsEarned; }
    public void setLifetimeCreditsEarned(Integer lifetimeCreditsEarned) { this.lifetimeCreditsEarned = lifetimeCreditsEarned; }
    public Integer getLifetimeCreditsSpent() { return lifetimeCreditsSpent; }
    public void setLifetimeCreditsSpent(Integer lifetimeCreditsSpent) { this.lifetimeCreditsSpent = lifetimeCreditsSpent; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
