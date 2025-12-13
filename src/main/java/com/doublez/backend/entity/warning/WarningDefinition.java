package com.doublez.backend.entity.warning;

import java.time.LocalDateTime;

import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.enums.warnings.WarningCategory;
import com.doublez.backend.enums.warnings.WarningSeverity;
import com.doublez.backend.enums.warnings.WarningTargetRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "warning_definitions", 
       indexes = @Index(name = "idx_warning_code", columnList = "code", unique = true))
public class WarningDefinition {
    
    @Id
    @Column(name = "code", length = 100, nullable = false)
    private String code; // e.g., "LEAD_UNANSWERED_24H", "LISTING_NO_PHOTOS"
    
    // === BASIC INFORMATION ===
    @Column(name = "title", nullable = false, length = 200)
    private String title; // Display title
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Detailed explanation
    
    // === CATEGORIZATION ===
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private WarningCategory category = WarningCategory.OPERATIONAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private WarningSeverity severity = WarningSeverity.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", nullable = false, length = 20)
    private WarningTargetRole targetRole = WarningTargetRole.AGENT;
    
    // === TIER & ACCESS CONTROL ===
    @Enumerated(EnumType.STRING)
    @Column(name = "required_tier", nullable = false, length = 20)
    private UserTier requiredTier = UserTier.AGENCY_BASIC;
    
    @Column(name = "is_premium_feature")
    private Boolean isPremiumFeature = false;
    
    // === TRIGGER LOGIC ===
    @Column(name = "trigger_logic", columnDefinition = "TEXT", nullable = false)
    private String triggerLogic; // DSL or SQL snippet
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // "LEAD", "LISTING", "AGENT", "AGENCY"
    
    @Column(name = "check_frequency", nullable = false, length = 20)
    private String checkFrequency = "HOURLY"; // REALTIME, HOURLY, DAILY
    
    // === THRESHOLDS & CONFIGURATION ===
    @Column(name = "default_threshold")
    private Integer defaultThreshold; // e.g., 24 (hours), 70 (percent)
    
    @Column(name = "threshold_unit", length = 20)
    private String thresholdUnit; // "HOURS", "DAYS", "PERCENT", "COUNT"
    
    @Column(name = "min_threshold")
    private Integer minThreshold; // Minimum allowed value
    
    @Column(name = "max_threshold")
    private Integer maxThreshold; // Maximum allowed value
    
    // === RESOLUTION & ESCALATION ===
    @Column(name = "auto_resolve_after_hours")
    private Integer autoResolveAfterHours; // Auto-resolve after X hours
    
    @Column(name = "escalate_after_hours")
    private Integer escalateAfterHours; // Escalate to higher role after X hours
    
    @Enumerated(EnumType.STRING)
    @Column(name = "escalate_to_role", length = 20)
    private WarningTargetRole escalateToRole;
    
    // === UI & DISPLAY ===
    @Column(name = "icon_class", length = 50)
    private String iconClass; // CSS icon class
    
    @Column(name = "color_code", length = 10)
    private String colorCode; // Hex color for UI
    
    @Column(name = "suggested_action", columnDefinition = "TEXT")
    private String suggestedAction; // What the user should do
    
    // === SYSTEM CONTROLS ===
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "is_system")
    private Boolean isSystem = false; // Can't be disabled/deleted
    
    @Column(name = "priority")
    private Integer priority = 50; // 1-100 (sort order)
    
    @Column(name = "version")
    private Integer version = 1; // For definition updates
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy = "SYSTEM";
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.version = this.version + 1;
    }
    
    // === HELPER METHODS ===
    
    /**
     * Get threshold with unit for display
     */
    @Transient
    public String getThresholdDisplay() {
        if (defaultThreshold == null) return "N/A";
        return defaultThreshold + " " + (thresholdUnit != null ? thresholdUnit : "");
    }

    /**
     * Check if this warning applies to given tier
     */
    @Transient
    public boolean appliesToTier(UserTier tier) {
        return tier.ordinal() >= requiredTier.ordinal();
    }

    /**
     * Check if warning can be configured by agency
     */
    @Transient
    public boolean isConfigurable() {
        return !Boolean.TRUE.equals(isSystem);
    }
    
    // Getters and setters...

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public WarningCategory getCategory() {
		return category;
	}

	public void setCategory(WarningCategory category) {
		this.category = category;
	}

	public WarningSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(WarningSeverity severity) {
		this.severity = severity;
	}

	public WarningTargetRole getTargetRole() {
		return targetRole;
	}

	public void setTargetRole(WarningTargetRole targetRole) {
		this.targetRole = targetRole;
	}

	public UserTier getRequiredTier() {
		return requiredTier;
	}

	public void setRequiredTier(UserTier requiredTier) {
		this.requiredTier = requiredTier;
	}

	public Boolean getIsPremiumFeature() {
		return isPremiumFeature;
	}

	public void setIsPremiumFeature(Boolean isPremiumFeature) {
		this.isPremiumFeature = isPremiumFeature;
	}

	public String getTriggerLogic() {
		return triggerLogic;
	}

	public void setTriggerLogic(String triggerLogic) {
		this.triggerLogic = triggerLogic;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getCheckFrequency() {
		return checkFrequency;
	}

	public void setCheckFrequency(String checkFrequency) {
		this.checkFrequency = checkFrequency;
	}

	public Integer getDefaultThreshold() {
		return defaultThreshold;
	}

	public void setDefaultThreshold(Integer defaultThreshold) {
		this.defaultThreshold = defaultThreshold;
	}

	public String getThresholdUnit() {
		return thresholdUnit;
	}

	public void setThresholdUnit(String thresholdUnit) {
		this.thresholdUnit = thresholdUnit;
	}

	public Integer getMinThreshold() {
		return minThreshold;
	}

	public void setMinThreshold(Integer minThreshold) {
		this.minThreshold = minThreshold;
	}

	public Integer getMaxThreshold() {
		return maxThreshold;
	}

	public void setMaxThreshold(Integer maxThreshold) {
		this.maxThreshold = maxThreshold;
	}

	public Integer getAutoResolveAfterHours() {
		return autoResolveAfterHours;
	}

	public void setAutoResolveAfterHours(Integer autoResolveAfterHours) {
		this.autoResolveAfterHours = autoResolveAfterHours;
	}

	public Integer getEscalateAfterHours() {
		return escalateAfterHours;
	}

	public void setEscalateAfterHours(Integer escalateAfterHours) {
		this.escalateAfterHours = escalateAfterHours;
	}

	public WarningTargetRole getEscalateToRole() {
		return escalateToRole;
	}

	public void setEscalateToRole(WarningTargetRole escalateToRole) {
		this.escalateToRole = escalateToRole;
	}

	public String getIconClass() {
		return iconClass;
	}

	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}

	public String getColorCode() {
		return colorCode;
	}

	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	public String getSuggestedAction() {
		return suggestedAction;
	}

	public void setSuggestedAction(String suggestedAction) {
		this.suggestedAction = suggestedAction;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
    
}