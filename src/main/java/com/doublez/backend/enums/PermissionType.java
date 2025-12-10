package com.doublez.backend.enums;

public enum PermissionType {
    
    // Team permissions
    VIEW_TEAM("view_team", "View team members and structure"),
    MANAGE_TEAM("manage_team", "Add/remove team members"),
    INVITE_AGENTS("invite_agents", "Send team invitations"),
    
    // Listing permissions
    CREATE_LISTING("create_listing", "Create new property listings"),
    VIEW_LISTING("view_listing", "View property listings"),
    EDIT_LISTING("edit_listing", "Edit property listings"),
    DELETE_LISTING("delete_listing", "Delete property listings"),
    MANAGE_LISTINGS("manage_listings", "Manage all agency listings"),
    
    // Billing permissions
    VIEW_BILLING("view_billing", "View billing information"),
    MANAGE_BILLING("manage_billing", "Manage billing and payments"),
    PURCHASE_CREDITS("purchase_credits", "Purchase credits for agency"),
    
    // Analytics permissions
    VIEW_ANALYTICS("view_analytics", "View agency analytics"),
    VIEW_AGENT_PERFORMANCE("view_agent_performance", "View agent performance data"),
    
    // Admin permissions
    MANAGE_AGENCY_SETTINGS("manage_agency_settings", "Manage agency settings"),
    VERIFY_AGENTS("verify_agents", "Verify agent credentials");
    
    private final String code;
    private final String description;
    
    PermissionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
