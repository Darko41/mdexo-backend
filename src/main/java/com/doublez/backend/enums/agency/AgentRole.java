package com.doublez.backend.enums.agency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.doublez.backend.enums.PermissionType;

public enum AgentRole {
    OWNER("Vlasnik Agencije", "Full access to all agency features and team management"),
    SUPER_AGENT("Super Agent", "Can manage other agents, view analytics, and access billing"),
    AGENT("Agent", "Standard agent with listing management capabilities");
    
    private final String displayName;
    private final String description;
    
    AgentRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Helper method to check hierarchy
    public boolean canManage(AgentRole otherRole) {
        return this.ordinal() < otherRole.ordinal(); // Lower ordinal = higher privilege
    }
    
    // Get all roles that this role can manage
    public List<AgentRole> getManageableRoles() {
        List<AgentRole> manageable = new ArrayList<>();
        for (AgentRole role : AgentRole.values()) {
            if (this.canManage(role)) {
                manageable.add(role);
            }
        }
        return manageable;
    }
    
    /**
     * Get default permissions for this role
     */
    public Set<PermissionType> getDefaultPermissions() {
        Set<PermissionType> permissions = new HashSet<>();
        
        switch (this) {
            case OWNER:
                permissions.addAll(Arrays.asList(
                    PermissionType.VIEW_TEAM,
                    PermissionType.MANAGE_TEAM,
                    PermissionType.INVITE_AGENTS,
                    PermissionType.CREATE_LISTING,
                    PermissionType.VIEW_LISTING,
                    PermissionType.EDIT_LISTING,
                    PermissionType.DELETE_LISTING,
                    PermissionType.MANAGE_LISTINGS,
                    PermissionType.VIEW_BILLING,
                    PermissionType.MANAGE_BILLING,
                    PermissionType.PURCHASE_CREDITS,
                    PermissionType.VIEW_ANALYTICS,
                    PermissionType.VIEW_AGENT_PERFORMANCE,
                    PermissionType.MANAGE_AGENCY_SETTINGS,
                    PermissionType.VERIFY_AGENTS
                ));
                break;
                
            case SUPER_AGENT:
                permissions.addAll(Arrays.asList(
                    PermissionType.VIEW_TEAM,
                    PermissionType.MANAGE_TEAM,
                    PermissionType.INVITE_AGENTS,
                    PermissionType.CREATE_LISTING,
                    PermissionType.VIEW_LISTING,
                    PermissionType.EDIT_LISTING,
                    PermissionType.MANAGE_LISTINGS,
                    PermissionType.VIEW_BILLING,
                    PermissionType.VIEW_ANALYTICS,
                    PermissionType.VIEW_AGENT_PERFORMANCE
                ));
                break;
                
            case AGENT:
                permissions.addAll(Arrays.asList(
                    PermissionType.VIEW_TEAM,
                    PermissionType.CREATE_LISTING,
                    PermissionType.VIEW_LISTING,
                    PermissionType.EDIT_LISTING
                ));
                break;
        }
        
        return permissions;
    }
    
    /**
     * Check if role has specific permission
     */
    public boolean hasPermission(PermissionType permission) {
        return getDefaultPermissions().contains(permission);
    }
}
