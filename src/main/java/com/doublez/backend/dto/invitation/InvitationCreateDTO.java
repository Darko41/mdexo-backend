package com.doublez.backend.dto.invitation;

import com.doublez.backend.enums.agency.AgentRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class InvitationCreateDTO {
    
    @Email
    @NotNull
    private String email;
    
    @NotNull
    private Long agencyId;
    
    @NotNull
    private AgentRole role = AgentRole.AGENT;
    
    private String message;
    private String customPermissions; // JSON string
    private Integer expiryDays = 7; // Default 7 days
    
    // For existing users only
    private Long userId;
    
    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(String customPermissions) { this.customPermissions = customPermissions; }
    
    public Integer getExpiryDays() { return expiryDays; }
    public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
