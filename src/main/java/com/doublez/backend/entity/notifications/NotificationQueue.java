package com.doublez.backend.entity.notifications;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.doublez.backend.entity.Lead;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.warning.ActiveWarning;
import com.doublez.backend.enums.notifications.NotificationChannel;
import com.doublez.backend.enums.notifications.NotificationStatus;
import com.doublez.backend.enums.notifications.NotificationType;
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
@Table(name = "notification_queue", indexes = {
    @Index(name = "idx_notif_status", columnList = "status"),
    @Index(name = "idx_notif_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notif_scheduled", columnList = "scheduled_for"),
    @Index(name = "idx_notif_created", columnList = "created_at DESC")
})
public class NotificationQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === RECIPIENT ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;
    
    // === NOTIFICATION DETAILS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;
    
    @Column(name = "template_code", length = 100)
    private String templateCode; // Reference to NotificationTemplate
    
    // === CONTENT ===
    @Column(name = "subject", length = 500)
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "short_content", length = 500)
    private String shortContent; // For SMS/push preview
    
    @Column(name = "metadata_json", columnDefinition = "JSON")
    private String metadataJson; // Additional data for rendering
    
    // === STATUS ===
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "status_message", length = 500)
    private String statusMessage; // Error/success details
    
    // === DELIVERY TRACKING ===
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor; // When to send (null = immediately)
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "clicked_at")
    private LocalDateTime clickedAt; // If contains links
    
    // === RETRY LOGIC ===
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "retry_backoff_minutes", nullable = false)
    private Integer retryBackoffMinutes = 5; // Exponential backoff
    
    // === LINKS TO OTHER ENTITIES ===
    @Column(name = "entity_type", length = 50)
    private String entityType; // e.g., "LEAD", "WARNING", "LISTING"
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warning_id")
    private ActiveWarning warning;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;
    
    // === PRIORITY ===
    @Column(name = "priority", nullable = false)
    private Integer priority = 50; // 1-100 (1=highest priority)
    
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;
    
    // === TTL (Time To Live) ===
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Auto-cancel if not sent by this time
    
    // === DELIVERY INFO ===
    @Column(name = "delivery_provider", length = 100)
    private String deliveryProvider; // "SENDGRID", "TWILIO", "FCM"
    
    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId; // External provider's ID
    
    @Column(name = "delivery_cost", precision = 10, scale = 4)
    private BigDecimal deliveryCost; // Cost of sending (for SMS/email providers)
    
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
        
        // Set default expiry (7 days for non-urgent, 24h for urgent)
        if (this.expiresAt == null) {
            int expiryHours = Boolean.TRUE.equals(isUrgent) ? 24 : 168; // 7 days
            this.expiresAt = LocalDateTime.now().plusHours(expiryHours);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === HELPER METHODS ===
    
    /**
     * Mark as sent
     */
    public void markAsSent(String providerMessageId, String provider) {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.providerMessageId = providerMessageId;
        this.deliveryProvider = provider;
        this.retryCount = 0;
        this.nextRetryAt = null;
    }
    
    /**
     * Mark as delivered
     */
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    /**
     * Mark as read
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Mark as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.statusMessage = errorMessage;
    }
    
    /**
     * Schedule retry
     */
    public void scheduleRetry() {
        if (retryCount >= maxRetries) {
            markAsFailed("Max retries exceeded");
            return;
        }
        
        this.retryCount++;
        int backoffMinutes = retryBackoffMinutes * (int) Math.pow(2, retryCount - 1); // Exponential
        this.nextRetryAt = LocalDateTime.now().plusMinutes(backoffMinutes);
        this.status = NotificationStatus.PENDING;
    }
    
    /**
     * Cancel notification
     */
    public void cancel() {
        this.status = NotificationStatus.FAILED;
        this.statusMessage = "Cancelled";
    }
    
    /**
     * Get metadata as map (convenience method)
     */
    @Transient
    public Map<String, Object> getMetadata() {
        return JsonUtils.parseStringObjectMap(metadataJson);
    }

    /**
     * Set metadata from map (convenience method)
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadataJson = JsonUtils.toJson(metadata);
    }

    /**
     * Check if notification should be sent now
     */
    @Transient
    public boolean shouldSendNow() {
        if (status != NotificationStatus.PENDING) return false;
        if (scheduledFor != null && LocalDateTime.now().isBefore(scheduledFor)) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        return true;
    }

    /**
     * Check if retry is due
     */
    @Transient
    public boolean isRetryDue() {
        return status == NotificationStatus.FAILED && 
               nextRetryAt != null && 
               LocalDateTime.now().isAfter(nextRetryAt);
    }

    /**
     * Check if notification is expired
     */
    @Transient
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters and setters...

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public void setChannel(NotificationChannel channel) {
		this.channel = channel;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getShortContent() {
		return shortContent;
	}

	public void setShortContent(String shortContent) {
		this.shortContent = shortContent;
	}

	public String getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public LocalDateTime getScheduledFor() {
		return scheduledFor;
	}

	public void setScheduledFor(LocalDateTime scheduledFor) {
		this.scheduledFor = scheduledFor;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public LocalDateTime getReadAt() {
		return readAt;
	}

	public void setReadAt(LocalDateTime readAt) {
		this.readAt = readAt;
	}

	public LocalDateTime getClickedAt() {
		return clickedAt;
	}

	public void setClickedAt(LocalDateTime clickedAt) {
		this.clickedAt = clickedAt;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Integer getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	public LocalDateTime getNextRetryAt() {
		return nextRetryAt;
	}

	public void setNextRetryAt(LocalDateTime nextRetryAt) {
		this.nextRetryAt = nextRetryAt;
	}

	public Integer getRetryBackoffMinutes() {
		return retryBackoffMinutes;
	}

	public void setRetryBackoffMinutes(Integer retryBackoffMinutes) {
		this.retryBackoffMinutes = retryBackoffMinutes;
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

	public ActiveWarning getWarning() {
		return warning;
	}

	public void setWarning(ActiveWarning warning) {
		this.warning = warning;
	}

	public Lead getLead() {
		return lead;
	}

	public void setLead(Lead lead) {
		this.lead = lead;
	}

	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Boolean getIsUrgent() {
		return isUrgent;
	}

	public void setIsUrgent(Boolean isUrgent) {
		this.isUrgent = isUrgent;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getDeliveryProvider() {
		return deliveryProvider;
	}

	public void setDeliveryProvider(String deliveryProvider) {
		this.deliveryProvider = deliveryProvider;
	}

	public String getProviderMessageId() {
		return providerMessageId;
	}

	public void setProviderMessageId(String providerMessageId) {
		this.providerMessageId = providerMessageId;
	}

	public BigDecimal getDeliveryCost() {
		return deliveryCost;
	}

	public void setDeliveryCost(BigDecimal deliveryCost) {
		this.deliveryCost = deliveryCost;
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
