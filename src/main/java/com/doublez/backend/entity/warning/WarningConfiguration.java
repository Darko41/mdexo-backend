package com.doublez.backend.entity.warning;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.enums.warnings.WarningSeverity;

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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "warning_configurations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"agency_id", "definition_code"}))
public class WarningConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === AGENCY REFERENCE ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id")
    private Agency agency;
    
    // === WARNING DEFINITION ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_code", referencedColumnName = "code")
    private WarningDefinition definition;
    
    // === ENABLED/TOGGLE ===
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    // === CUSTOM THRESHOLDS ===
    @Column(name = "custom_threshold")
    private Integer customThreshold; // Override default threshold
    
    @Column(name = "custom_threshold_unit", length = 20)
    private String customThresholdUnit;
    
    // === NOTIFICATION SETTINGS ===
    @Column(name = "notify_in_app", nullable = false)
    private Boolean notifyInApp = true;
    
    @Column(name = "notify_email")
    private Boolean notifyEmail = false;
    
    @Column(name = "notify_sms")
    private Boolean notifySms = false;
    
    @Column(name = "notify_push")
    private Boolean notifyPush = false;
    
    // === ESCALATION SETTINGS ===
    @Column(name = "escalate_to_super_agent")
    private Boolean escalateToSuperAgent = false;
    
    @Column(name = "escalate_to_owner")
    private Boolean escalateToOwner = false;
    
    @Column(name = "escalation_delay_hours")
    private Integer escalationDelayHours = 24;
    
    // === SNOOZE & QUIET HOURS ===
    @Column(name = "allow_snooze")
    private Boolean allowSnooze = true;
    
    @Column(name = "max_snooze_hours")
    private Integer maxSnoozeHours = 24;
    
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart; // e.g., 22:00
    
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd;   // e.g., 08:00
    
    // === PRIORITY OVERRIDES ===
    @Column(name = "custom_priority")
    private Integer customPriority; // Override definition priority
    
    @Column(name = "custom_severity", length = 20)
    @Enumerated(EnumType.STRING)
    private WarningSeverity customSeverity;
    
    // === CUSTOM MESSAGES ===
    @Column(name = "custom_message", columnDefinition = "TEXT")
    private String customMessage; // Agency-specific instructions
    
    @Column(name = "custom_action", columnDefinition = "TEXT")
    private String customAction; // Agency-specific resolution steps
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "configured_by")
    private Long configuredBy; // User ID who configured this
    
    @Version
    private Integer version;
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === HELPER METHODS ===
    
    /**
     * Get effective threshold (custom or default)
     */
    @Transient
    public Integer getEffectiveThreshold() {
        return customThreshold != null ? customThreshold : 
               (definition != null ? definition.getDefaultThreshold() : null);
    }

    /**
     * Get effective threshold unit
     */
    @Transient
    public String getEffectiveThresholdUnit() {
        return customThresholdUnit != null ? customThresholdUnit : 
               (definition != null ? definition.getThresholdUnit() : null);
    }

    /**
     * Get effective severity (custom or default)
     */
    @Transient
    public WarningSeverity getEffectiveSeverity() {
        return customSeverity != null ? customSeverity : 
               (definition != null ? definition.getSeverity() : null);
    }

    /**
     * Check if notifications are currently allowed (outside quiet hours)
     */
    @Transient
    public boolean isNotificationAllowed() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return true; // No quiet hours configured
        }
        
        // If start equals end, quiet hours are disabled
        if (quietHoursStart.equals(quietHoursEnd)) {
            return true;
        }
        
        LocalTime now = LocalTime.now();
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Normal range: 08:00 to 22:00 (quiet hours outside this)
            return now.isBefore(quietHoursStart) || now.isAfter(quietHoursEnd);
        } else {
            // Overnight range: 22:00 to 08:00 (crosses midnight)
            return now.isBefore(quietHoursStart) && now.isAfter(quietHoursEnd);
        }
    }

    /**
     * Check if at least one notification channel is enabled
     */
    @Transient
    public boolean hasActiveNotifications() {
        return Boolean.TRUE.equals(notifyInApp) || 
               Boolean.TRUE.equals(notifyEmail) || 
               Boolean.TRUE.equals(notifySms) || 
               Boolean.TRUE.equals(notifyPush);
    }
    
    // Getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public WarningDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(WarningDefinition definition) {
		this.definition = definition;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getCustomThreshold() {
		return customThreshold;
	}

	public void setCustomThreshold(Integer customThreshold) {
		this.customThreshold = customThreshold;
	}

	public String getCustomThresholdUnit() {
		return customThresholdUnit;
	}

	public void setCustomThresholdUnit(String customThresholdUnit) {
		this.customThresholdUnit = customThresholdUnit;
	}

	public Boolean getNotifyInApp() {
		return notifyInApp;
	}

	public void setNotifyInApp(Boolean notifyInApp) {
		this.notifyInApp = notifyInApp;
	}

	public Boolean getNotifyEmail() {
		return notifyEmail;
	}

	public void setNotifyEmail(Boolean notifyEmail) {
		this.notifyEmail = notifyEmail;
	}

	public Boolean getNotifySms() {
		return notifySms;
	}

	public void setNotifySms(Boolean notifySms) {
		this.notifySms = notifySms;
	}

	public Boolean getNotifyPush() {
		return notifyPush;
	}

	public void setNotifyPush(Boolean notifyPush) {
		this.notifyPush = notifyPush;
	}

	public Boolean getEscalateToSuperAgent() {
		return escalateToSuperAgent;
	}

	public void setEscalateToSuperAgent(Boolean escalateToSuperAgent) {
		this.escalateToSuperAgent = escalateToSuperAgent;
	}

	public Boolean getEscalateToOwner() {
		return escalateToOwner;
	}

	public void setEscalateToOwner(Boolean escalateToOwner) {
		this.escalateToOwner = escalateToOwner;
	}

	public Integer getEscalationDelayHours() {
		return escalationDelayHours;
	}

	public void setEscalationDelayHours(Integer escalationDelayHours) {
		this.escalationDelayHours = escalationDelayHours;
	}

	public Boolean getAllowSnooze() {
		return allowSnooze;
	}

	public void setAllowSnooze(Boolean allowSnooze) {
		this.allowSnooze = allowSnooze;
	}

	public Integer getMaxSnoozeHours() {
		return maxSnoozeHours;
	}

	public void setMaxSnoozeHours(Integer maxSnoozeHours) {
		this.maxSnoozeHours = maxSnoozeHours;
	}

	public LocalTime getQuietHoursStart() {
		return quietHoursStart;
	}

	public void setQuietHoursStart(LocalTime quietHoursStart) {
		this.quietHoursStart = quietHoursStart;
	}

	public LocalTime getQuietHoursEnd() {
		return quietHoursEnd;
	}

	public void setQuietHoursEnd(LocalTime quietHoursEnd) {
		this.quietHoursEnd = quietHoursEnd;
	}

	public Integer getCustomPriority() {
		return customPriority;
	}

	public void setCustomPriority(Integer customPriority) {
		this.customPriority = customPriority;
	}

	public WarningSeverity getCustomSeverity() {
		return customSeverity;
	}

	public void setCustomSeverity(WarningSeverity customSeverity) {
		this.customSeverity = customSeverity;
	}

	public String getCustomMessage() {
		return customMessage;
	}

	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}

	public String getCustomAction() {
		return customAction;
	}

	public void setCustomAction(String customAction) {
		this.customAction = customAction;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getConfiguredBy() {
		return configuredBy;
	}

	public void setConfiguredBy(Long configuredBy) {
		this.configuredBy = configuredBy;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
