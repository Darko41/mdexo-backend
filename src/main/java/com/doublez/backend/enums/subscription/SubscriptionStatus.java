package com.doublez.backend.enums.subscription;

public enum SubscriptionStatus {
    ACTIVE("Aktivna", "Subscription is active and in good standing"),
    TRIAL("Probni period", "In trial period"),
    PENDING("Na čekanju", "Awaiting payment or activation"),
    EXPIRING("Ističe uskoro", "Subscription is about to expire"),
    EXPIRED("Istekla", "Subscription has expired"),
    CANCELLED("Otkazana", "Subscription has been cancelled"),
    SUSPENDED("Suspendovana", "Subscription is suspended due to non-payment"),
    PAUSED("Pauzirana", "Subscription is temporarily paused");
    
    private final String displayName;
    private final String description;
    
    SubscriptionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }
    
    public boolean canBeRenewed() {
        return this == ACTIVE || this == EXPIRING || this == EXPIRED;
    }
}
