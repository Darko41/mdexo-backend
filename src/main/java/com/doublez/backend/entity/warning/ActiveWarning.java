package com.doublez.backend.entity.warning;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.warnings.WarningStatus;
import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "active_warnings", indexes = {
    @Index(name = "idx_warning_user", columnList = "target_user_id"),
    @Index(name = "idx_warning_status", columnList = "status"),
    @Index(name = "idx_warning_detected", columnList = "detected_at"),
    @Index(name = "idx_warning_entity", columnList = "entity_type,entity_id")
})
public class ActiveWarning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === DEFINITION ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_code", referencedColumnName = "code")
    private WarningDefinition definition;
    
    // === TARGET USER ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id")
    private User targetUser;
    
    // === AGENCY CONTEXT ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;
    
    // === ENTITY REFERENCE ===
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // "LEAD", "LISTING", "AGENT", "AGENCY"
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    // Reference to actual entity (not FK for flexibility)
    @Transient
    private Object entityReference;
    
    // === WARNING DATA ===
    @Column(name = "current_value", length = 100)
    private String currentValue; // e.g., "36" (hours since lead received)
    
    @Column(name = "threshold_value", length = 100)
    private String thresholdValue; // e.g., "24" (max hours)
    
    @Column(name = "difference_value", length = 100)
    private String differenceValue; // How far over threshold
    
    // === STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WarningStatus status = WarningStatus.ACTIVE;
    
    // === TIMESTAMPS ===
    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    @Column(name = "snoozed_until")
    private LocalDateTime snoozedUntil;
    
    // === CONTEXT DATA ===
    @Column(name = "details_json", columnDefinition = "JSON")
    private String detailsJson; // Additional context
    
    @Column(name = "action_taken", length = 500)
    private String actionTaken; // What user did to resolve
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    // === ESCALATION TRACKING ===
    @Column(name = "escalated_to_user_id")
    private Long escalatedToUserId;
    
    @Column(name = "original_target_user_id")
    private Long originalTargetUserId;
    
    @Column(name = "escalation_level")
    private Integer escalationLevel = 0; // 0=initial, 1=first escalation, etc.
    
    // === NOTIFICATION TRACKING ===
    @Column(name = "last_notified_at")
    private LocalDateTime lastNotifiedAt;
    
    @Column(name = "notification_count")
    private Integer notificationCount = 0;
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Integer version;
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === HELPER METHODS ===
    
    /**
     * Acknowledge this warning
     */
    public void acknowledge(User acknowledgedBy) {
        if (this.status == WarningStatus.ACTIVE) {
            this.status = WarningStatus.ACKNOWLEDGED;
            this.acknowledgedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Resolve this warning
     */
    public void resolve(String actionTaken, String notes) {
        this.status = WarningStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.actionTaken = actionTaken;
        this.resolutionNotes = notes;
    }
    
    /**
     * Dismiss this warning (false alarm)
     */
    public void dismiss(String reason) {
        this.status = WarningStatus.DISMISSED;
        this.dismissedAt = LocalDateTime.now();
        this.resolutionNotes = reason;
    }
    
    /**
     * Snooze warning for specified hours
     */
    public void snooze(Integer hours) {
        this.snoozedUntil = LocalDateTime.now().plusHours(hours);
    }
    
    /**
     * Check if warning is active (not resolved/dismissed)
     */
    public boolean isActive() {
        return status == WarningStatus.ACTIVE || status == WarningStatus.ACKNOWLEDGED;
    }
    
    /**
     * Get details as map (convenience method for JSON field)
     */
    @Transient
    public Map<String, Object> getDetails() {
        return JsonUtils.parseStringObjectMap(detailsJson);
    }

    /**
     * Set details from map (convenience method for JSON field)
     */
    public void setDetails(Map<String, Object> details) {
        this.detailsJson = JsonUtils.toJson(details);
    }

    /**
     * Add detail to details map
     */
    public void addDetail(String key, Object value) {
        Map<String, Object> currentDetails = getDetails();
        currentDetails.put(key, value);
        setDetails(currentDetails);
    }

    /**
     * Get warning age in hours
     */
    @Transient
    public Long getAgeInHours() {
        if (detectedAt == null) return null;
        return Duration.between(detectedAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if warning needs escalation
     */
    @Transient
    public boolean needsEscalation(Integer escalationHours) {
        if (escalationHours == null || escalatedAt != null) return false;
        Long ageHours = getAgeInHours();
        return ageHours != null && ageHours >= escalationHours;
    }

    /**
     * Check if warning is currently snoozed
     */
    @Transient
    public boolean isSnoozed() {
        return snoozedUntil != null && LocalDateTime.now().isBefore(snoozedUntil);
    }
    
    // Getters and setters...

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WarningDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(WarningDefinition definition) {
		this.definition = definition;
	}

	public User getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}

	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public Object getEntityReference() {
		return entityReference;
	}

	public void setEntityReference(Object entityReference) {
		this.entityReference = entityReference;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public String getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(String thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public String getDifferenceValue() {
		return differenceValue;
	}

	public void setDifferenceValue(String differenceValue) {
		this.differenceValue = differenceValue;
	}

	public WarningStatus getStatus() {
		return status;
	}

	public void setStatus(WarningStatus status) {
		this.status = status;
	}

	public LocalDateTime getDetectedAt() {
		return detectedAt;
	}

	public void setDetectedAt(LocalDateTime detectedAt) {
		this.detectedAt = detectedAt;
	}

	public LocalDateTime getAcknowledgedAt() {
		return acknowledgedAt;
	}

	public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
		this.acknowledgedAt = acknowledgedAt;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public LocalDateTime getDismissedAt() {
		return dismissedAt;
	}

	public void setDismissedAt(LocalDateTime dismissedAt) {
		this.dismissedAt = dismissedAt;
	}

	public LocalDateTime getEscalatedAt() {
		return escalatedAt;
	}

	public void setEscalatedAt(LocalDateTime escalatedAt) {
		this.escalatedAt = escalatedAt;
	}

	public LocalDateTime getSnoozedUntil() {
		return snoozedUntil;
	}

	public void setSnoozedUntil(LocalDateTime snoozedUntil) {
		this.snoozedUntil = snoozedUntil;
	}

	public String getDetailsJson() {
		return detailsJson;
	}

	public void setDetailsJson(String detailsJson) {
		this.detailsJson = detailsJson;
	}

	public String getActionTaken() {
		return actionTaken;
	}

	public void setActionTaken(String actionTaken) {
		this.actionTaken = actionTaken;
	}

	public String getResolutionNotes() {
		return resolutionNotes;
	}

	public void setResolutionNotes(String resolutionNotes) {
		this.resolutionNotes = resolutionNotes;
	}

	public Long getEscalatedToUserId() {
		return escalatedToUserId;
	}

	public void setEscalatedToUserId(Long escalatedToUserId) {
		this.escalatedToUserId = escalatedToUserId;
	}

	public Long getOriginalTargetUserId() {
		return originalTargetUserId;
	}

	public void setOriginalTargetUserId(Long originalTargetUserId) {
		this.originalTargetUserId = originalTargetUserId;
	}

	public Integer getEscalationLevel() {
		return escalationLevel;
	}

	public void setEscalationLevel(Integer escalationLevel) {
		this.escalationLevel = escalationLevel;
	}

	public LocalDateTime getLastNotifiedAt() {
		return lastNotifiedAt;
	}

	public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) {
		this.lastNotifiedAt = lastNotifiedAt;
	}

	public Integer getNotificationCount() {
		return notificationCount;
	}

	public void setNotificationCount(Integer notificationCount) {
		this.notificationCount = notificationCount;
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
    
}
