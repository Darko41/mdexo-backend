package com.doublez.backend.entity.notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.notifications.NotificationChannel;
import com.doublez.backend.enums.notifications.NotificationType;
import com.doublez.backend.enums.warnings.WarningSeverity;
import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "user_notification_settings",
       indexes = @Index(name = "idx_user_notif", columnList = "user_id", unique = true))
public class UserNotificationSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === USER REFERENCE ===
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    // === CHANNEL PREFERENCES ===
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;
    
    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;
    
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = false;
    
    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = false;
    
    @Column(name = "webhook_enabled", nullable = false)
    private Boolean webhookEnabled = false;
    
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;
    
    // === FREQUENCY PREFERENCES ===
    @Column(name = "email_frequency", nullable = false, length = 20)
    private String emailFrequency = "IMMEDIATE"; // IMMEDIATE, DAILY_DIGEST, WEEKLY
    
    @Column(name = "digest_hour", nullable = false)
    private Integer digestHour = 9; // 0-23, when to send daily digest
    
    @Column(name = "digest_day", length = 10)
    private String digestDay = "MONDAY"; // For weekly digest
    
    // === TYPE PREFERENCES ===
    @Column(name = "warnings_enabled", nullable = false)
    private Boolean warningsEnabled = true;
    
    @Column(name = "warnings_min_severity", length = 20)
    @Enumerated(EnumType.STRING)
    private WarningSeverity warningsMinSeverity = WarningSeverity.MEDIUM;
    
    @Column(name = "leads_enabled", nullable = false)
    private Boolean leadsEnabled = true;
    
    @Column(name = "leads_priority_min", nullable = false)
    private Integer leadsPriorityMin = 1; // Only notify for leads >= this priority
    
    @Column(name = "system_enabled", nullable = false)
    private Boolean systemEnabled = true;
    
    @Column(name = "team_enabled", nullable = false)
    private Boolean teamEnabled = true;
    
    @Column(name = "billing_enabled", nullable = false)
    private Boolean billingEnabled = true;
    
    @Column(name = "promotional_enabled", nullable = false)
    private Boolean promotionalEnabled = false; // Marketing emails
    
    // === QUIET HOURS ===
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = false;
    
    @Column(name = "quiet_hours_start")
    private LocalTime quietHoursStart = LocalTime.of(22, 0); // 10 PM
    
    @Column(name = "quiet_hours_end")
    private LocalTime quietHoursEnd = LocalTime.of(8, 0);    // 8 AM
    
    @Column(name = "quiet_hours_days", length = 100)
    private String quietHoursDays = "MON,TUE,WED,THU,FRI,SAT,SUN"; // All days
    
    // === DO NOT DISTURB ===
    @Column(name = "do_not_disturb_until")
    private LocalDateTime doNotDisturbUntil;
    
    @Column(name = "do_not_disturb_reason", length = 200)
    private String doNotDisturbReason;
    
    // === DEVICE PREFERENCES ===
    @Column(name = "preferred_device_types", length = 200)
    private String preferredDeviceTypes; // "MOBILE,DESKTOP" or "MOBILE_ONLY"
    
    @Column(name = "push_device_tokens_json", columnDefinition = "JSON")
    private String pushDeviceTokensJson; // {"ios": ["token1"], "android": ["token2"]}
    
    // === AGENCY OVERRIDES ===
    @Column(name = "agency_overrides_enabled", nullable = false)
    private Boolean agencyOverridesEnabled = true; // Allow agency to override settings
    
    @Column(name = "super_agent_escalation_enabled", nullable = false)
    private Boolean superAgentEscalationEnabled = true;
    
    @Column(name = "owner_escalation_enabled", nullable = false)
    private Boolean ownerEscalationEnabled = true;
    
    // === LANGUAGE & LOCALIZATION ===
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";
    
    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Belgrade";
    
    // === NOTIFICATION LIMITS ===
    @Column(name = "max_daily_notifications")
    private Integer maxDailyNotifications = 50;
    
    @Column(name = "notifications_today")
    private Integer notificationsToday = 0;
    
    @Column(name = "last_notification_reset")
    private LocalDate lastNotificationReset = LocalDate.now();
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_modified_by")
    private Long lastModifiedBy; // User ID who last changed settings
    
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
     * Get push device tokens as map (convenience method)
     */
    @Transient
    public Map<String, List<String>> getPushDeviceTokens() {
        if (pushDeviceTokensJson == null) return new HashMap<>();
        return JsonUtils.parseWithTypeReference(
            pushDeviceTokensJson, 
            JsonUtils.Types.MAP_STRING_LIST_STRING
        );
    }

    /**
     * Set push device tokens from map (convenience method)
     */
    public void setPushDeviceTokens(Map<String, List<String>> tokens) {
        this.pushDeviceTokensJson = JsonUtils.toJson(tokens);
    }

    /**
     * Add push device token
     */
    public void addPushDeviceToken(String platform, String token) {
        Map<String, List<String>> tokens = getPushDeviceTokens();
        tokens.computeIfAbsent(platform.toLowerCase(), k -> new ArrayList<>());
        
        List<String> platformTokens = tokens.get(platform.toLowerCase());
        if (!platformTokens.contains(token)) {
            platformTokens.add(token);
            setPushDeviceTokens(tokens);
        }
    }

    /**
     * Remove push device token
     */
    public void removePushDeviceToken(String platform, String token) {
        Map<String, List<String>> tokens = getPushDeviceTokens();
        if (tokens.containsKey(platform.toLowerCase())) {
            tokens.get(platform.toLowerCase()).remove(token);
            setPushDeviceTokens(tokens);
        }
    }

    /**
     * Get quiet hours days as list
     */
    @Transient
    public List<String> getQuietHoursDaysList() {
        if (quietHoursDays == null) return new ArrayList<>();
        return Arrays.asList(quietHoursDays.split(","));
    }

    /**
     * Set quiet hours days from list
     */
    public void setQuietHoursDaysList(List<String> days) {
        this.quietHoursDays = String.join(",", days);
    }

    /**
     * Get push tokens for specific platform
     */
    @Transient
    public List<String> getPushDeviceTokens(String platform) {
        Map<String, List<String>> tokens = getPushDeviceTokens();
        return tokens.getOrDefault(platform.toLowerCase(), new ArrayList<>());
    }

    /**
     * Check if current time is within quiet hours
     */
    @Transient
    private boolean isWithinQuietHours() {
        if (!Boolean.TRUE.equals(quietHoursEnabled) || quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        // Check if today is in quiet hours days
        String today = LocalDate.now().getDayOfWeek().toString().substring(0, 3); // "MON", "TUE"
        List<String> quietDays = getQuietHoursDaysList();
        if (!quietDays.isEmpty() && !quietDays.contains(today)) {
            return false; // Quiet hours not applicable today
        }
        
        LocalTime now = LocalTime.now();
        
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            // Normal range: 22:00 to 08:00
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Overnight range: 22:00 to 08:00 (crosses midnight)
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }

    /**
     * Check if daily notification limit is exceeded
     */
    @Transient
    private boolean hasExceededDailyLimit() {
        // Reset counter if it's a new day
        if (lastNotificationReset == null || !lastNotificationReset.equals(LocalDate.now())) {
            notificationsToday = 0;
            lastNotificationReset = LocalDate.now();
            return false;
        }
        
        return notificationsToday >= maxDailyNotifications;
    }

    /**
     * Increment notification counter
     */
    public void incrementNotificationCount() {
        // Reset if new day
        if (lastNotificationReset == null || !lastNotificationReset.equals(LocalDate.now())) {
            notificationsToday = 0;
            lastNotificationReset = LocalDate.now();
        }
        
        notificationsToday++;
    }

    /**
     * Enable Do Not Disturb for specified hours
     */
    public void enableDoNotDisturb(Integer hours, String reason) {
        this.doNotDisturbUntil = LocalDateTime.now().plusHours(hours);
        this.doNotDisturbReason = reason;
    }

    /**
     * Disable Do Not Disturb
     */
    public void disableDoNotDisturb() {
        this.doNotDisturbUntil = null;
        this.doNotDisturbReason = null;
    }

    /**
     * Check if notifications are allowed right now (considering quiet hours/DND)
     */
    @Transient
    public boolean isNotificationAllowedNow() {
        // Check Do Not Disturb
        if (doNotDisturbUntil != null && LocalDateTime.now().isBefore(doNotDisturbUntil)) {
            return false;
        }
        
        // Check quiet hours
        if (isWithinQuietHours()) {
            return false;
        }
        
        // Check daily limit
        if (hasExceededDailyLimit()) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if channel is enabled
     */
    @Transient
    public boolean isChannelEnabled(NotificationChannel channel) {
        switch (channel) {
            case EMAIL: return Boolean.TRUE.equals(emailEnabled);
            case IN_APP: return Boolean.TRUE.equals(inAppEnabled);
            case PUSH: return Boolean.TRUE.equals(pushEnabled);
            case SMS: return Boolean.TRUE.equals(smsEnabled);
            case WEBHOOK: return Boolean.TRUE.equals(webhookEnabled);
            default: return false;
        }
    }

    /**
     * Check if notification type is enabled
     */
    @Transient
    public boolean isTypeEnabled(NotificationType type) {
        switch (type) {
            case WARNING: return Boolean.TRUE.equals(warningsEnabled);
            case LEAD: return Boolean.TRUE.equals(leadsEnabled);
            case SYSTEM: return Boolean.TRUE.equals(systemEnabled);
            case TEAM: return Boolean.TRUE.equals(teamEnabled);
            case BILLING: return Boolean.TRUE.equals(billingEnabled);
            default: return true;
        }
    }
    
    
    
    
    // Getters and setters...
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getEmailEnabled() {
		return emailEnabled;
	}

	public void setEmailEnabled(Boolean emailEnabled) {
		this.emailEnabled = emailEnabled;
	}

	public Boolean getInAppEnabled() {
		return inAppEnabled;
	}

	public void setInAppEnabled(Boolean inAppEnabled) {
		this.inAppEnabled = inAppEnabled;
	}

	public Boolean getPushEnabled() {
		return pushEnabled;
	}

	public void setPushEnabled(Boolean pushEnabled) {
		this.pushEnabled = pushEnabled;
	}

	public Boolean getSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(Boolean smsEnabled) {
		this.smsEnabled = smsEnabled;
	}

	public Boolean getWebhookEnabled() {
		return webhookEnabled;
	}

	public void setWebhookEnabled(Boolean webhookEnabled) {
		this.webhookEnabled = webhookEnabled;
	}

	public String getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public String getEmailFrequency() {
		return emailFrequency;
	}

	public void setEmailFrequency(String emailFrequency) {
		this.emailFrequency = emailFrequency;
	}

	public Integer getDigestHour() {
		return digestHour;
	}

	public void setDigestHour(Integer digestHour) {
		this.digestHour = digestHour;
	}

	public String getDigestDay() {
		return digestDay;
	}

	public void setDigestDay(String digestDay) {
		this.digestDay = digestDay;
	}

	public Boolean getWarningsEnabled() {
		return warningsEnabled;
	}

	public void setWarningsEnabled(Boolean warningsEnabled) {
		this.warningsEnabled = warningsEnabled;
	}

	public WarningSeverity getWarningsMinSeverity() {
		return warningsMinSeverity;
	}

	public void setWarningsMinSeverity(WarningSeverity warningsMinSeverity) {
		this.warningsMinSeverity = warningsMinSeverity;
	}

	public Boolean getLeadsEnabled() {
		return leadsEnabled;
	}

	public void setLeadsEnabled(Boolean leadsEnabled) {
		this.leadsEnabled = leadsEnabled;
	}

	public Integer getLeadsPriorityMin() {
		return leadsPriorityMin;
	}

	public void setLeadsPriorityMin(Integer leadsPriorityMin) {
		this.leadsPriorityMin = leadsPriorityMin;
	}

	public Boolean getSystemEnabled() {
		return systemEnabled;
	}

	public void setSystemEnabled(Boolean systemEnabled) {
		this.systemEnabled = systemEnabled;
	}

	public Boolean getTeamEnabled() {
		return teamEnabled;
	}

	public void setTeamEnabled(Boolean teamEnabled) {
		this.teamEnabled = teamEnabled;
	}

	public Boolean getBillingEnabled() {
		return billingEnabled;
	}

	public void setBillingEnabled(Boolean billingEnabled) {
		this.billingEnabled = billingEnabled;
	}

	public Boolean getPromotionalEnabled() {
		return promotionalEnabled;
	}

	public void setPromotionalEnabled(Boolean promotionalEnabled) {
		this.promotionalEnabled = promotionalEnabled;
	}

	public Boolean getQuietHoursEnabled() {
		return quietHoursEnabled;
	}

	public void setQuietHoursEnabled(Boolean quietHoursEnabled) {
		this.quietHoursEnabled = quietHoursEnabled;
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

	public String getQuietHoursDays() {
		return quietHoursDays;
	}

	public void setQuietHoursDays(String quietHoursDays) {
		this.quietHoursDays = quietHoursDays;
	}

	public LocalDateTime getDoNotDisturbUntil() {
		return doNotDisturbUntil;
	}

	public void setDoNotDisturbUntil(LocalDateTime doNotDisturbUntil) {
		this.doNotDisturbUntil = doNotDisturbUntil;
	}

	public String getDoNotDisturbReason() {
		return doNotDisturbReason;
	}

	public void setDoNotDisturbReason(String doNotDisturbReason) {
		this.doNotDisturbReason = doNotDisturbReason;
	}

	public String getPreferredDeviceTypes() {
		return preferredDeviceTypes;
	}

	public void setPreferredDeviceTypes(String preferredDeviceTypes) {
		this.preferredDeviceTypes = preferredDeviceTypes;
	}

	public String getPushDeviceTokensJson() {
		return pushDeviceTokensJson;
	}

	public void setPushDeviceTokensJson(String pushDeviceTokensJson) {
		this.pushDeviceTokensJson = pushDeviceTokensJson;
	}

	public Boolean getAgencyOverridesEnabled() {
		return agencyOverridesEnabled;
	}

	public void setAgencyOverridesEnabled(Boolean agencyOverridesEnabled) {
		this.agencyOverridesEnabled = agencyOverridesEnabled;
	}

	public Boolean getSuperAgentEscalationEnabled() {
		return superAgentEscalationEnabled;
	}

	public void setSuperAgentEscalationEnabled(Boolean superAgentEscalationEnabled) {
		this.superAgentEscalationEnabled = superAgentEscalationEnabled;
	}

	public Boolean getOwnerEscalationEnabled() {
		return ownerEscalationEnabled;
	}

	public void setOwnerEscalationEnabled(Boolean ownerEscalationEnabled) {
		this.ownerEscalationEnabled = ownerEscalationEnabled;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Integer getMaxDailyNotifications() {
		return maxDailyNotifications;
	}

	public void setMaxDailyNotifications(Integer maxDailyNotifications) {
		this.maxDailyNotifications = maxDailyNotifications;
	}

	public Integer getNotificationsToday() {
		return notificationsToday;
	}

	public void setNotificationsToday(Integer notificationsToday) {
		this.notificationsToday = notificationsToday;
	}

	public LocalDate getLastNotificationReset() {
		return lastNotificationReset;
	}

	public void setLastNotificationReset(LocalDate lastNotificationReset) {
		this.lastNotificationReset = lastNotificationReset;
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

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
    
    
}
