package com.doublez.backend.entity.agency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.AgentRole;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "agents", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "agency_id"}) // A user can only be agent in one agency
       })
public class Agent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // User who is an agent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Agency this agent belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;
    
    // Agent's role within the agency
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private AgentRole role = AgentRole.AGENT;
    
    // Who invited this agent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;
    
    // Status tracking
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;
    
    @Column(name = "last_active_date")
    private LocalDateTime lastActiveDate;
    
    // Agent-specific settings and overrides
    @Column(name = "max_listings")
    private Integer maxListings; // Override agency tier limits
    
    @Column(name = "can_manage_listings", nullable = false)
    private Boolean canManageListings = true;
    
    @Column(name = "can_view_analytics", nullable = false)
    private Boolean canViewAnalytics = false;
    
    @Column(name = "can_manage_billing", nullable = false)
    private Boolean canManageBilling = false;
    
    @Column(name = "can_invite_agents", nullable = false)
    private Boolean canInviteAgents = false;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate; // Agent's commission percentage
    
    // Performance metrics (updated regularly)
    @Column(name = "total_listings_created")
    private Integer totalListingsCreated = 0;
    
    @Column(name = "active_listings_count")
    private Integer activeListingsCount = 0;
    
    @Column(name = "leads_generated")
    private Integer leadsGenerated = 0;
    
    @Column(name = "deals_closed")
    private Integer dealsClosed = 0;
    
    @Column(name = "response_time_minutes")
    private Double averageResponseTimeMinutes;
    
    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ========================
    // CONSTRUCTORS
    // ========================
    
    public Agent() {}
    
    public Agent(User user, Agency agency, AgentRole role) {
        this.user = user;
        this.agency = agency;
        this.role = role;
        this.isActive = true;
        this.joinDate = LocalDateTime.now();
        this.canManageListings = true;
        this.canViewAnalytics = role == AgentRole.OWNER || role == AgentRole.SUPER_AGENT;
        this.canManageBilling = role == AgentRole.OWNER;
        this.canInviteAgents = role == AgentRole.OWNER || role == AgentRole.SUPER_AGENT;
    }
    
    public Agent(User user, Agency agency, AgentRole role, User invitedBy) {
        this(user, agency, role);
        this.invitedBy = invitedBy;
    }
    
    // ========================
    // LIFECYCLE METHODS
    // ========================
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.joinDate = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========================
    // BUSINESS METHODS
    // ========================
    
    /**
     * Check if this agent can manage another agent
     */
    public boolean canManageAgent(Agent otherAgent) {
        if (!this.isActive || !this.agency.getId().equals(otherAgent.getAgency().getId())) {
            return false;
        }
        return this.role.canManage(otherAgent.getRole());
    }
    
    /**
     * Check if this agent can manage a listing
     */
    public boolean canManageListing(RealEstate listing) {
        if (!this.isActive) return false;
        
        // Agency owner/super-agent can manage all agency listings
        if (this.role == AgentRole.OWNER || this.role == AgentRole.SUPER_AGENT) {
            return listing.getAgency() != null && 
                   listing.getAgency().getId().equals(this.agency.getId());
        }
        
        // Regular agents can only manage their own listings
        // We'll need to track listing ownership - this requires a change to RealEstate entity
        // For now, return true if they have permission
        return this.canManageListings;
    }
    
    /**
     * Check if agent can view sensitive agency data
     */
    public boolean canViewSensitiveData() {
        return this.role == AgentRole.OWNER || this.role == AgentRole.SUPER_AGENT;
    }
    
    /**
     * Update agent's last active date
     */
    public void updateLastActive() {
        this.lastActiveDate = LocalDateTime.now();
    }
    
    /**
     * Deactivate agent (soft delete)
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reactivate agent
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update performance metrics
     */
    public void incrementListingsCreated() {
        this.totalListingsCreated = (this.totalListingsCreated == null) ? 1 : this.totalListingsCreated + 1;
        this.activeListingsCount = (this.activeListingsCount == null) ? 1 : this.activeListingsCount + 1;
    }
    
    public void decrementActiveListings() {
        if (this.activeListingsCount != null && this.activeListingsCount > 0) {
            this.activeListingsCount--;
        }
    }
    
    public void incrementLeadsGenerated() {
        this.leadsGenerated = (this.leadsGenerated == null) ? 1 : this.leadsGenerated + 1;
    }
    
    public void incrementDealsClosed() {
        this.dealsClosed = (this.dealsClosed == null) ? 1 : this.dealsClosed + 1;
    }
    
    public void updateResponseTime(Double responseTimeMinutes) {
        if (this.averageResponseTimeMinutes == null) {
            this.averageResponseTimeMinutes = responseTimeMinutes;
        } else {
            // Simple moving average
            this.averageResponseTimeMinutes = (this.averageResponseTimeMinutes + responseTimeMinutes) / 2;
        }
    }
    
    /**
     * Check if agent has reached listing limit
     */
    public boolean hasReachedListingLimit() {
        if (this.maxListings != null) {
            return this.activeListingsCount >= this.maxListings;
        }
        // If no agent-specific limit, check agency tier limits
        // This will be implemented in TierLimitationService
        return false;
    }
    
    /**
     * Get remaining listing slots
     */
    public Integer getRemainingListingSlots() {
        if (this.maxListings == null) return null;
        return Math.max(0, this.maxListings - (this.activeListingsCount != null ? this.activeListingsCount : 0));
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }
    
    public AgentRole getRole() { return role; }
    public void setRole(AgentRole role) { 
        this.role = role;
        // Update permissions based on role
        if (role != null) {
            this.canViewAnalytics = role == AgentRole.OWNER || role == AgentRole.SUPER_AGENT;
            this.canManageBilling = role == AgentRole.OWNER;
            this.canInviteAgents = role == AgentRole.OWNER || role == AgentRole.SUPER_AGENT;
        }
    }
    
    public User getInvitedBy() { return invitedBy; }
    public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDateTime joinDate) { this.joinDate = joinDate; }
    
    public LocalDateTime getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDateTime lastActiveDate) { this.lastActiveDate = lastActiveDate; }
    
    public Integer getMaxListings() { return maxListings; }
    public void setMaxListings(Integer maxListings) { this.maxListings = maxListings; }
    
    public Boolean getCanManageListings() { return canManageListings; }
    public void setCanManageListings(Boolean canManageListings) { this.canManageListings = canManageListings; }
    
    public Boolean getCanViewAnalytics() { return canViewAnalytics; }
    public void setCanViewAnalytics(Boolean canViewAnalytics) { this.canViewAnalytics = canViewAnalytics; }
    
    public Boolean getCanManageBilling() { return canManageBilling; }
    public void setCanManageBilling(Boolean canManageBilling) { this.canManageBilling = canManageBilling; }
    
    public Boolean getCanInviteAgents() { return canInviteAgents; }
    public void setCanInviteAgents(Boolean canInviteAgents) { this.canInviteAgents = canInviteAgents; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public Integer getTotalListingsCreated() { return totalListingsCreated; }
    public void setTotalListingsCreated(Integer totalListingsCreated) { this.totalListingsCreated = totalListingsCreated; }
    
    public Integer getActiveListingsCount() { return activeListingsCount; }
    public void setActiveListingsCount(Integer activeListingsCount) { this.activeListingsCount = activeListingsCount; }
    
    public Integer getLeadsGenerated() { return leadsGenerated; }
    public void setLeadsGenerated(Integer leadsGenerated) { this.leadsGenerated = leadsGenerated; }
    
    public Integer getDealsClosed() { return dealsClosed; }
    public void setDealsClosed(Integer dealsClosed) { this.dealsClosed = dealsClosed; }
    
    public Double getAverageResponseTimeMinutes() { return averageResponseTimeMinutes; }
    public void setAverageResponseTimeMinutes(Double averageResponseTimeMinutes) { this.averageResponseTimeMinutes = averageResponseTimeMinutes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", agencyId=" + (agency != null ? agency.getId() : "null") +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}
