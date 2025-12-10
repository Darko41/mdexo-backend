package com.doublez.backend.enums.agency;

import java.util.ArrayList;
import java.util.List;

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
}
