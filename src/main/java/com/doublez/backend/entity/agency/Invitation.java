package com.doublez.backend.entity.agency;

import java.time.LocalDateTime;
import java.util.UUID;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.enums.agency.InvitationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invitations")
public class Invitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Unique token for invitation
    @Column(name = "token", nullable = false, unique = true)
    private String token = UUID.randomUUID().toString();
    
    // Email of invited person
    @Column(name = "email", nullable = false)
    private String email;
    
    // Invited user (if they already exist in system)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;
    
    // Agency the invitation is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;
    
    // Role being offered
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private AgentRole role = AgentRole.AGENT;
    
    // Who sent the invitation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;
    
    // Timestamps
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // Metadata
    @Column(name = "message", length = 500)
    private String message;
    
    @Column(name = "custom_permissions", length = 1000) // JSON string for custom permissions
    private String customPermissions;
    
    // ========================
    // CONSTRUCTORS
    // ========================
    
    public Invitation() {}
    
    public Invitation(String email, Agency agency, AgentRole role, User invitedBy) {
        this.email = email;
        this.agency = agency;
        this.role = role;
        this.invitedBy = invitedBy;
        this.sentAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7); // 7-day expiry by default
        this.status = InvitationStatus.PENDING;
    }
    
    public Invitation(String email, Agency agency, AgentRole role, User invitedBy, String message) {
        this(email, agency, role, invitedBy);
        this.message = message;
    }
    
    public Invitation(User invitedUser, Agency agency, AgentRole role, User invitedBy) {
        this(invitedUser.getEmail(), agency, role, invitedBy);
        this.invitedUser = invitedUser;
    }
    
    // ========================
    // BUSINESS METHODS
    // ========================
    
    /**
     * Check if invitation is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if invitation is still valid (not expired and pending)
     */
    public boolean isValid() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
    
    /**
     * Accept the invitation
     */
    public void accept(User acceptingUser) {
        if (!isValid()) {
            throw new IllegalStateException("Invitation is not valid for acceptance");
        }
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.invitedUser = acceptingUser;
    }
    
    /**
     * Reject the invitation
     */
    public void reject() {
        if (status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be rejected");
        }
        this.status = InvitationStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }
    
    /**
     * Cancel the invitation
     */
    public void cancel() {
        if (status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be cancelled");
        }
        this.status = InvitationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    /**
     * Resend invitation (create new token and extend expiry)
     */
    public void resend(User resendingUser) {
        if (status != InvitationStatus.PENDING && status != InvitationStatus.EXPIRED) {
            throw new IllegalStateException("Only pending or expired invitations can be resent");
        }
        this.token = UUID.randomUUID().toString();
        this.sentAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7);
        this.status = InvitationStatus.PENDING;
        this.invitedBy = resendingUser;
    }
    
    /**
     * Mark invitation as expired
     */
    public void markAsExpired() {
        if (status == InvitationStatus.PENDING) {
            this.status = InvitationStatus.EXPIRED;
        }
    }
    
    /**
     * Get days until expiry
     */
    public long getDaysUntilExpiry() {
        if (status != InvitationStatus.PENDING) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public User getInvitedUser() { return invitedUser; }
    public void setInvitedUser(User invitedUser) { this.invitedUser = invitedUser; }
    
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { this.role = role; }
    
    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }
    
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
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
    
    @Override
    public String toString() {
        return "Invitation{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", agencyId=" + (agency != null ? agency.getId() : "null") +
                ", role=" + role +
                ", status=" + status +
                ", expiresAt=" + expiresAt +
                '}';
    }
}