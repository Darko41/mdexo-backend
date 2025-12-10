package com.doublez.backend.enums.agency;

public enum InvitationStatus {
    PENDING("Na čekanju", "Invitation sent, awaiting response"),
    ACCEPTED("Prihvaćeno", "Invitation accepted, user joined team"),
    REJECTED("Odbijeno", "Invitation rejected by user"),
    CANCELLED("Otkazano", "Invitation cancelled by sender"),
    EXPIRED("Isteklo", "Invitation expired before response");
    
    private final String displayName;
    private final String description;
    
    InvitationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
