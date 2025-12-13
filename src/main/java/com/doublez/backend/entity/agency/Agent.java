package com.doublez.backend.entity.agency;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.doublez.backend.entity.Lead;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.warning.ActiveWarning;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.utils.JsonUtils;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    
    // ===== WARNING/NOTIFICATION SYSTEM =====
    
    @Column(name = "performance_metrics_json", columnDefinition = "JSON")
    private String performanceMetricsJson; // Detailed performance metrics
    
    @Column(name = "notification_settings_json", columnDefinition = "JSON")
    private String notificationSettingsJson; // Agent-specific notification preferences
    
    @Column(name = "warning_stats_json", columnDefinition = "JSON")
    private String warningStatsJson; // Warning statistics and history
    
    @Column(name = "last_lead_response_at")
    private LocalDateTime lastLeadResponseAt; // For lead response time warnings
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt; // For inactivity warnings
    
    @Column(name = "working_hours_json", columnDefinition = "JSON")
    private String workingHoursJson; // Agent's working hours schedule
    
    // ===== NEW RELATIONSHIPS =====
    
    @OneToMany(mappedBy = "assignedAgent", fetch = FetchType.LAZY)
    private List<Lead> assignedLeads = new ArrayList<>(); // Leads assigned to this agent
    
    @OneToMany(mappedBy = "targetUser", fetch = FetchType.LAZY)
    private List<ActiveWarning> warnings = new ArrayList<>(); // Warnings targeting this agent
    
    @OneToMany(mappedBy = "listingAgent", fetch = FetchType.LAZY)
    private List<RealEstate> managedListings = new ArrayList<>(); // Listings managed by this agent

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
        this.lastActiveDate = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        
        // Initialize JSON fields
        initializePerformanceMetrics();
        initializeNotificationSettings();
        initializeWarningStats();
        initializeWorkingHours();
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
     * Get remaining listing slots
     */
    public Integer getRemainingListingSlots() {
        if (this.maxListings == null) return null;
        return Math.max(0, this.maxListings - (this.activeListingsCount != null ? this.activeListingsCount : 0));
    }
    
    /**
     * Initialize performance metrics with defaults
     */
    private void initializePerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("responseTimeAvg", 0.0);
        metrics.put("leadConversionRate", 0.0);
        metrics.put("listingQualityScore", 0);
        metrics.put("customerSatisfaction", 0);
        metrics.put("tasksCompleted", 0);
        metrics.put("tasksOverdue", 0);
        this.performanceMetricsJson = JsonUtils.toJson(metrics);
    }
    
    /**
     * Initialize notification settings with defaults
     */
    private void initializeNotificationSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("emailEnabled", true);
        settings.put("smsEnabled", false);
        settings.put("pushEnabled", true);
        settings.put("quietHoursEnabled", false);
        settings.put("quietHoursStart", "22:00");
        settings.put("quietHoursEnd", "08:00");
        settings.put("minWarningSeverity", "MEDIUM");
        this.notificationSettingsJson = JsonUtils.toJson(settings);
    }
    
    /**
     * Initialize warning statistics
     */
    private void initializeWarningStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWarnings", 0);
        stats.put("resolvedWarnings", 0);
        stats.put("activeWarnings", 0);
        stats.put("avgResolutionTimeHours", 0);
        stats.put("warningTypes", new HashMap<>());
        this.warningStatsJson = JsonUtils.toJson(stats);
    }
    
    /**
     * Initialize working hours (default: Mon-Fri 9-17)
     */
    private void initializeWorkingHours() {
        Map<String, Object> hours = new HashMap<>();
        hours.put("monday", "09:00-17:00");
        hours.put("tuesday", "09:00-17:00");
        hours.put("wednesday", "09:00-17:00");
        hours.put("thursday", "09:00-17:00");
        hours.put("friday", "09:00-17:00");
        hours.put("saturday", "closed");
        hours.put("sunday", "closed");
        this.workingHoursJson = JsonUtils.toJson(hours);
    }
    
    /**
     * Get performance metrics as map
     */
    @Transient
    public Map<String, Object> getPerformanceMetrics() {
        return JsonUtils.parseStringObjectMap(performanceMetricsJson);
    }
    
    /**
     * Update a performance metric
     */
    public void updatePerformanceMetric(String key, Object value) {
        Map<String, Object> metrics = getPerformanceMetrics();
        metrics.put(key, value);
        this.performanceMetricsJson = JsonUtils.toJson(metrics);
    }
    
    /**
     * Get notification settings as map
     */
    @Transient
    public Map<String, Object> getNotificationSettings() {
        return JsonUtils.parseStringObjectMap(notificationSettingsJson);
    }
    
    /**
     * Check if notification channel is enabled
     */
    @Transient
    public boolean isNotificationChannelEnabled(String channel) {
        Map<String, Object> settings = getNotificationSettings();
        return Boolean.TRUE.equals(settings.get(channel + "Enabled"));
    }
    
    /**
     * Get warning statistics as map
     */
    @Transient
    public Map<String, Object> getWarningStats() {
        return JsonUtils.parseStringObjectMap(warningStatsJson);
    }
    
    /**
     * Update warning statistics
     */
    public void updateWarningStats(String key, Object value) {
        Map<String, Object> stats = getWarningStats();
        stats.put(key, value);
        this.warningStatsJson = JsonUtils.toJson(stats);
    }
    
    /**
     * Increment warning count
     */
    public void incrementWarningCount(String warningType) {
        // Parse the entire stats with proper type handling
        Map<String, Object> stats = JsonUtils.parseStringObjectMap(warningStatsJson);
        if (stats == null) {
            stats = new HashMap<>();
        }
        
        // Update total warnings
        Integer total = (Integer) stats.getOrDefault("totalWarnings", 0);
        stats.put("totalWarnings", total + 1);
        
        // Get warning types with type-safe parsing
        Map<String, Integer> types = getWarningTypesSafe(stats);
        types.put(warningType, types.getOrDefault(warningType, 0) + 1);
        stats.put("warningTypes", types);
        
        this.warningStatsJson = JsonUtils.toJson(stats);
    }

    /**
     * Type-safe method to get warning types
     */
    private Map<String, Integer> getWarningTypesSafe(Map<String, Object> stats) {
        Object warningTypesObj = stats.get("warningTypes");
        
        if (warningTypesObj == null) {
            return new HashMap<>();
        }
        
        // If it's already a Map, try to cast (we know it should be String->Integer)
        if (warningTypesObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Integer> result = (Map<String, Integer>) warningTypesObj;
                return result;
            } catch (ClassCastException e) {
                // If cast fails, parse from JSON string
                return parseWarningTypesFromJson(warningTypesObj);
            }
        }
        
        return new HashMap<>();
    }

    /**
     * Parse warning types from JSON string or object
     */
    private Map<String, Integer> parseWarningTypesFromJson(Object warningTypesObj) {
        if (warningTypesObj instanceof String) {
            // It's a JSON string, parse it
            return JsonUtils.parseMap((String) warningTypesObj, String.class, Integer.class);
        }
        
        // Convert to JSON string and parse
        String json = JsonUtils.toJson(warningTypesObj);
        return JsonUtils.parseMap(json, String.class, Integer.class);
    }
    
    /**
     * Get working hours as map
     */
    @Transient
    public Map<String, String> getWorkingHours() {
        return JsonUtils.parseStringMap(workingHoursJson);
    }
    
    /**
     * Check if agent is currently working (within working hours)
     */
    @Transient
    public boolean isCurrentlyWorking() {
        if (workingHoursJson == null) return true; // No schedule = always working
        
        Map<String, String> hours = getWorkingHours();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String dayKey = today.toString().toLowerCase();
        
        // Try full day name, then short name
        String schedule = hours.get(dayKey);
        if (schedule == null) {
            // Try short name (mon, tue, etc.)
            String shortName = dayKey.substring(0, 3);
            schedule = hours.get(shortName);
        }
        
        if (schedule == null || "closed".equalsIgnoreCase(schedule)) {
            return false;
        }
        
        // Parse schedule like "09:00-17:00"
        String[] times = schedule.split("-");
        if (times.length != 2) return true;
        
        try {
            LocalTime start = LocalTime.parse(times[0].trim());
            LocalTime end = LocalTime.parse(times[1].trim());
            LocalTime now = LocalTime.now();
            
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            return true; // If parsing fails, assume working
        }
    }
    
    /**
     * Check if agent is inactive (no activity for X days)
     */
    @Transient
    public boolean isInactive(int daysThreshold) {
        if (lastActiveDate == null) return true;
        return Duration.between(lastActiveDate, LocalDateTime.now()).toDays() >= daysThreshold;
    }
    
    /**
     * Check if agent is slow to respond (last response > X hours)
     */
    @Transient
    public boolean isSlowToRespond(int hoursThreshold) {
        if (lastLeadResponseAt == null) return false;
        return Duration.between(lastLeadResponseAt, LocalDateTime.now()).toHours() >= hoursThreshold;
    }
    
    /**
     * Update last activity timestamp
     */
    public void updateLastActivity() {
        this.lastActiveDate = LocalDateTime.now();
    }
    
    /**
     * Update last lead response timestamp
     */
    public void updateLastLeadResponse() {
        this.lastLeadResponseAt = LocalDateTime.now();
    }
    
    /**
     * Calculate conversion rate
     */
    @Transient
    public Double calculateConversionRate() {
        if (leadsGenerated == null || leadsGenerated == 0) return 0.0;
        if (dealsClosed == null) return 0.0;
        return (dealsClosed.doubleValue() / leadsGenerated.doubleValue()) * 100;
    }
    
    /**
     * Check if agent is super agent (team lead)
     */
    @Transient
    public boolean isSuperAgent() {
        return AgentRole.SUPER_AGENT.equals(role);
    }
    
    /**
     * Check if agent can manage other agents
     */
    @Transient
    public boolean canManageTeam() {
        return isSuperAgent() || Boolean.TRUE.equals(canInviteAgents);
    }
    
    /**
     * Check if agent has reached listing limit
     */
    @Transient
    public boolean hasReachedListingLimit() {
        if (maxListings == null) return false;
        if (activeListingsCount == null) return false;
        return activeListingsCount >= maxListings;
    }
    
    /**
     * Get agent's display name (user name or custom)
     */
    @Transient
    public String getDisplayName() {
        if (user != null) {
            String name = user.getFirstName() + " " + user.getLastName();
            if (!name.trim().isEmpty()) return name.trim();
        }
        return "Agent #" + id;
    }
    
    /**
     * Get agent's contact email (user email or custom)
     */
    @Transient
    public String getContactEmail() {
        if (user != null && user.getEmail() != null) {
            return user.getEmail();
        }
        return null;
    }
    
    /**
     * Get agent's contact phone (user phone or custom)
     */
    @Transient
    public String getContactPhone() {
        if (user != null && user.getPhone() != null) {
            return user.getPhone();
        }
        return null;
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
    
    public String getPerformanceMetricsJson() {
		return performanceMetricsJson;
	}

	public void setPerformanceMetricsJson(String performanceMetricsJson) {
		this.performanceMetricsJson = performanceMetricsJson;
	}

	public String getNotificationSettingsJson() {
		return notificationSettingsJson;
	}

	public void setNotificationSettingsJson(String notificationSettingsJson) {
		this.notificationSettingsJson = notificationSettingsJson;
	}

	public String getWarningStatsJson() {
		return warningStatsJson;
	}

	public void setWarningStatsJson(String warningStatsJson) {
		this.warningStatsJson = warningStatsJson;
	}

	public LocalDateTime getLastLeadResponseAt() {
		return lastLeadResponseAt;
	}

	public void setLastLeadResponseAt(LocalDateTime lastLeadResponseAt) {
		this.lastLeadResponseAt = lastLeadResponseAt;
	}

	public LocalDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(LocalDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public String getWorkingHoursJson() {
		return workingHoursJson;
	}

	public void setWorkingHoursJson(String workingHoursJson) {
		this.workingHoursJson = workingHoursJson;
	}

	public List<Lead> getAssignedLeads() {
		return assignedLeads;
	}

	public void setAssignedLeads(List<Lead> assignedLeads) {
		this.assignedLeads = assignedLeads;
	}

	public List<ActiveWarning> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<ActiveWarning> warnings) {
		this.warnings = warnings;
	}

	public List<RealEstate> getManagedListings() {
		return managedListings;
	}

	public void setManagedListings(List<RealEstate> managedListings) {
		this.managedListings = managedListings;
	}

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
