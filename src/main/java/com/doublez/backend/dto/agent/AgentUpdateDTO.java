package com.doublez.backend.dto.agent;

import java.math.BigDecimal;

import com.doublez.backend.enums.agency.AgentRole;

import jakarta.validation.constraints.NotNull;

public class AgentUpdateDTO {
    
    @NotNull
    private Long id;
    
    private AgentRole role;
    private Integer maxListings;
    private BigDecimal commissionRate;
    private Boolean canManageListings;
    private Boolean canViewAnalytics;
    private Boolean canManageBilling;
    private Boolean canInviteAgents;
    private Boolean isActive;
    private String customPermissions; // JSON string
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    
    public Integer getMaxListings() { return maxListings; }
    public void setMaxListings(Integer maxListings) { this.maxListings = maxListings; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public Boolean getCanManageListings() { return canManageListings; }
    public void setCanManageListings(Boolean canManageListings) { this.canManageListings = canManageListings; }
    
    public Boolean getCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(Boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }
    
    public Boolean getCanManageBilling() { return canManageBilling; }
    public void setCanManageBilling(Boolean canManageBilling) { this.canManageBilling = canManageBilling; }
    
    public Boolean getCanInviteAgents() { return canInviteAgents; }
    public void setCanInviteAgents(Boolean canInviteAgents) { this.canInviteAgents = canInviteAgents; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(String customPermissions) { this.customPermissions = customPermissions; }
}
