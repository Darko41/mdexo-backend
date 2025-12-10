package com.doublez.backend.dto.agent;

import java.math.BigDecimal;

import com.doublez.backend.enums.agency.AgentRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class AgentCreateDTO {
    
    @Email
    @NotNull
    private String email;  // Email of user to invite/assign as agent
    
    @NotNull
    private Long agencyId;
    
    @NotNull
    private AgentRole role = AgentRole.AGENT;
    
    private Integer maxListings;
    private BigDecimal commissionRate;
    private String customPermissions; // JSON string for custom permissions
    
    // For existing users, you can specify user ID instead of email
    private Long userId;
    
    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    
    public Integer getMaxListings() { return maxListings; }
    public void setMaxListings(Integer maxListings) { this.maxListings = maxListings; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public String getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(String customPermissions) { this.customPermissions = customPermissions; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
