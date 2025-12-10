package com.doublez.backend.dto.invitation;

import java.time.LocalDateTime;

import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.enums.agency.InvitationStatus;

public class InvitationResponseDTO {
    
    private Long id;
    private String token;
    private String email;
    private Long invitedUserId;
    private String invitedUserName;
    private Long agencyId;
    private String agencyName;
    private AgentRole role;
    private String roleDisplayName;
    private Long invitedById;
    private String invitedByName;
    private InvitationStatus status;
    private String statusDisplayName;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private String message;
    private String customPermissions;
    private boolean expired;
    private long daysUntilExpiry;
    private boolean canResend;
    private boolean canCancel;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Long getInvitedUserId() { return invitedUserId; }
    public void setInvitedUserId(Long invitedUserId) { this.invitedUserId = invitedUserId; }
    
    public String getInvitedUserName() { return invitedUserName; }
    public void setInvitedUserName(String invitedUserName) { this.invitedUserName = invitedUserName; }
    
    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
    
    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { 
        this.role = role;
        if (role != null) {
            this.roleDisplayName = role.getDisplayName();
        }
    }
    
    public String getRoleDisplayName() { return roleDisplayName; }
    public void setRoleDisplayName(String roleDisplayName) { this.roleDisplayName = roleDisplayName; }
    
    public Long getInvitedById() { return invitedById; }
    public void setInvitedById(Long invitedById) { this.invitedById = invitedById; }
    
    public String getInvitedByName() { return invitedByName; }
    public void setInvitedByName(String invitedByName) { this.invitedByName = invitedByName; }
    
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { 
        this.status = status;
        if (status != null) {
            this.statusDisplayName = status.getDisplayName();
        }
    }
    
    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { 
        this.expiresAt = expiresAt;
        updateExpiryInfo();
    }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(String customPermissions) { this.customPermissions = customPermissions; }
    
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    
    public long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }
    
    public boolean isCanResend() { return canResend; }
    public void setCanResend(boolean canResend) { this.canResend = canResend; }
    
    public boolean isCanCancel() { return canCancel; }
    public void setCanCancel(boolean canCancel) { this.canCancel = canCancel; }
    
    private void updateExpiryInfo() {
        if (expiresAt == null || status != InvitationStatus.PENDING) {
            this.expired = false;
            this.daysUntilExpiry = 0;
        } else {
            this.expired = LocalDateTime.now().isAfter(expiresAt);
            this.daysUntilExpiry = expired ? 0 : 
                java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
        }
        
        // Business rules for actions
        this.canResend = status == InvitationStatus.PENDING || status == InvitationStatus.EXPIRED;
        this.canCancel = status == InvitationStatus.PENDING;
    }
}