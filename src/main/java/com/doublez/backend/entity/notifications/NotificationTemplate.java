package com.doublez.backend.entity.notifications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.enums.notifications.NotificationChannel;
import com.doublez.backend.enums.notifications.NotificationType;
import com.doublez.backend.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

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
@Table(name = "notification_templates",
       indexes = @Index(name = "idx_template_code", columnList = "code", unique = true))
public class NotificationTemplate {
	    
    @Id
    @Column(name = "code", length = 100, nullable = false)
    private String code; // e.g., "WARNING_LEAD_UNANSWERED", "NEW_LEAD_ALERT"
    
    // === BASIC INFO ===
    @Column(name = "name", nullable = false, length = 200)
    private String name; // Display name
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // === CHANNEL & TYPE ===
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel = NotificationChannel.EMAIL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type = NotificationType.SYSTEM;
    
    // === CONTENT TEMPLATES ===
    @Column(name = "subject_template", nullable = false, length = 500)
    private String subjectTemplate; // Can contain variables: {{agentName}}
    
    @Column(name = "body_template", columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate; // HTML/text template
    
    @Column(name = "sms_template", columnDefinition = "TEXT")
    private String smsTemplate; // For SMS notifications (160 chars max)
    
    @Column(name = "push_template", columnDefinition = "TEXT")
    private String pushTemplate; // For push notifications
    
    // === LOCALIZATION ===
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode = "en";
    
    @Column(name = "locale", length = 20)
    private String locale; // e.g., "en_US", "sr_RS"
    
    // === VARIABLE DEFINITIONS ===
    @Column(name = "available_variables_json", columnDefinition = "JSON")
    private String availableVariablesJson;
    // {"agentName": "Full name of agent", "leadCount": "Number of new leads"}
    
    @Column(name = "default_values_json", columnDefinition = "JSON")
    private String defaultValuesJson;
    // {"platformName": "RealEstate Pro", "supportEmail": "support@example.com"}
    
    // === FORMATTING ===
    @Column(name = "content_type", length = 100)
    private String contentType = "text/html"; // text/plain, text/html
    
    @Column(name = "css_styles", columnDefinition = "TEXT")
    private String cssStyles; // CSS for HTML emails
    
    @Column(name = "footer_template", columnDefinition = "TEXT")
    private String footerTemplate; // Email footer
    
    @Column(name = "header_template", columnDefinition = "TEXT")
    private String headerTemplate; // Email header
    
    // === TARGETING ===
    @Enumerated(EnumType.STRING)
    @Column(name = "target_tier", length = 20)
    private UserTier targetTier; // Which tier can use this template
    
    @Column(name = "target_roles", length = 500)
    private String targetRoles; // Comma-separated: "AGENT,SUPER_AGENT"
    
    // === SYSTEM CONTROLS ===
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "is_system")
    private Boolean isSystem = false; // Can't be deleted/modified
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    @Column(name = "cache_key", length = 100)
    private String cacheKey; // For template caching
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.cacheKey == null) {
            this.cacheKey = this.code + "_v" + this.version;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.version = this.version + 1;
        this.cacheKey = this.code + "_v" + this.version;
    }
    
    // === HELPER METHODS ===
        
    /**
     * Get available variables as map (convenience method)
     */
    @Transient
    public Map<String, String> getAvailableVariables() {
        return JsonUtils.parseStringMap(availableVariablesJson);
    }

    /**
     * Set available variables from map (convenience method)
     */
    public void setAvailableVariables(Map<String, String> variables) {
        this.availableVariablesJson = JsonUtils.toJson(variables);
    }

    /**
     * Get default values as map (convenience method)
     */
    @Transient
    public Map<String, String> getDefaultValues() {
        return JsonUtils.parseStringMap(defaultValuesJson);
    }

    /**
     * Set default values from map (convenience method)
     */
    public void setDefaultValues(Map<String, String> values) {
        this.defaultValuesJson = JsonUtils.toJson(values);
    }

    /**
     * Get available variable names as list
     */
    @Transient
    public List<String> getAvailableVariableNames() {
        return new ArrayList<>(getAvailableVariables().keySet());
    }

    /**
     * Render template with provided variables
     */
    @Transient
    public String renderSubject(Map<String, Object> variables) {
        return renderTemplate(subjectTemplate, variables);
    }

    @Transient
    public String renderBody(Map<String, Object> variables) {
        return renderTemplate(bodyTemplate, variables);
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null) return "";
        
        String result = template;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        
        return result;
    }

    /**
     * Check if template applies to user role
     */
    @Transient
    public boolean appliesToRole(String role) {
        if (targetRoles == null || targetRoles.isEmpty()) return true;
        return Arrays.asList(targetRoles.split(",")).contains(role);
    }

    /**
     * Check if template applies to tier
     */
    @Transient
    public boolean appliesToTier(UserTier tier) {
        if (targetTier == null) return true;
        return tier.ordinal() >= targetTier.ordinal();
    }

    /**
     * Increment usage counter
     */
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    // Getters and setters...

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public void setChannel(NotificationChannel channel) {
		this.channel = channel;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}

	public String getBodyTemplate() {
		return bodyTemplate;
	}

	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
	}

	public String getSmsTemplate() {
		return smsTemplate;
	}

	public void setSmsTemplate(String smsTemplate) {
		this.smsTemplate = smsTemplate;
	}

	public String getPushTemplate() {
		return pushTemplate;
	}

	public void setPushTemplate(String pushTemplate) {
		this.pushTemplate = pushTemplate;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getAvailableVariablesJson() {
		return availableVariablesJson;
	}

	public void setAvailableVariablesJson(String availableVariablesJson) {
		this.availableVariablesJson = availableVariablesJson;
	}

	public String getDefaultValuesJson() {
		return defaultValuesJson;
	}

	public void setDefaultValuesJson(String defaultValuesJson) {
		this.defaultValuesJson = defaultValuesJson;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCssStyles() {
		return cssStyles;
	}

	public void setCssStyles(String cssStyles) {
		this.cssStyles = cssStyles;
	}

	public String getFooterTemplate() {
		return footerTemplate;
	}

	public void setFooterTemplate(String footerTemplate) {
		this.footerTemplate = footerTemplate;
	}

	public String getHeaderTemplate() {
		return headerTemplate;
	}

	public void setHeaderTemplate(String headerTemplate) {
		this.headerTemplate = headerTemplate;
	}

	public UserTier getTargetTier() {
		return targetTier;
	}

	public void setTargetTier(UserTier targetTier) {
		this.targetTier = targetTier;
	}

	public String getTargetRoles() {
		return targetRoles;
	}

	public void setTargetRoles(String targetRoles) {
		this.targetRoles = targetRoles;
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
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

	public LocalDateTime getLastUsedAt() {
		return lastUsedAt;
	}

	public void setLastUsedAt(LocalDateTime lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}

	public Integer getUsageCount() {
		return usageCount;
	}

	public void setUsageCount(Integer usageCount) {
		this.usageCount = usageCount;
	}
    
    
}
