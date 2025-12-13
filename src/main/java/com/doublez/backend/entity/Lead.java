package com.doublez.backend.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.enums.notifications.LeadStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.persistence.Index;

@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_lead_agent", columnList = "agent_id"),
    @Index(name = "idx_lead_agency", columnList = "agency_id"),
    @Index(name = "idx_lead_status", columnList = "status"),
    @Index(name = "idx_lead_received", columnList = "received_at"),
    @Index(name = "idx_lead_assigned", columnList = "assigned_at")
})
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Property the lead is about
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private RealEstate property;
    
    // Assigned agent (nullable for unassigned leads)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent assignedAgent;
    
    // Agency this lead belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;
    
    // === LEAD SOURCE ===
    @Column(name = "source", length = 50)
    private String source = "WEBSITE"; // WEBSITE, PHONE, EMAIL, SOCIAL, REFERRAL
    
    @Column(name = "referrer_url", length = 500)
    private String referrerUrl; // Page where lead was submitted
    
    @Column(name = "campaign_id")
    private String campaignId; // Marketing campaign tracking
    
    // === CONTACT INFORMATION ===
    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;
    
    @Email
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    // === LEAD DETAILS ===
    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // Lead inquiry text
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeadStatus status = LeadStatus.NEW;
    
    @Column(name = "priority")
    private Integer priority = 1; // 1-5 scale (1=lowest, 5=highest)
    
    @Column(name = "interest_level")
    private Integer interestLevel; // 1-10 scale (optional)
    
    // === CRITICAL TIMESTAMPS (for warning calculations) ===
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt; // When lead was submitted
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt; // When assigned to agent
    
    @Column(name = "opened_at")
    private LocalDateTime openedAt; // When agent first opened/viewed
    
    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt; // When agent first replied
    
    @Column(name = "last_contact_at")
    private LocalDateTime lastContactAt; // Last contact attempt
    
    @Column(name = "follow_up_at")
    private LocalDateTime followUpAt; // Scheduled follow-up
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt; // When lead was converted/lost
    
    // === WARNING TRACKING ===
    @Column(name = "has_warning")
    private Boolean hasWarning = false;
    
    @Column(name = "warning_type", length = 50)
    private String warningType; // e.g., "UNANSWERED", "NO_FOLLOWUP"
    
    @Column(name = "warning_generated_at")
    private LocalDateTime warningGeneratedAt;
    
    // === METADATA & TAGS ===
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated: "urgent,foreign,buyer"
    
    @Column(name = "metadata_json", columnDefinition = "JSON")
    private String metadataJson; // Additional flexible data
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Integer version; // Optimistic locking
    
    // === LIFECYCLE METHODS ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.receivedAt == null) {
            this.receivedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === HELPER METHODS ===
    
    /**
     * Calculate hours since lead was received
     */
    public Long getHoursSinceReceived() {
        if (receivedAt == null) return null;
        return Duration.between(receivedAt, LocalDateTime.now()).toHours();
    }
    
    /**
     * Calculate response time in minutes (from received to first response)
     */
    public Long getResponseTimeMinutes() {
        if (receivedAt == null || firstResponseAt == null) return null;
        return Duration.between(receivedAt, firstResponseAt).toMinutes();
    }
    
    /**
     * Check if lead is still active (not closed/archived)
     */
    public boolean isActive() {
        return status != LeadStatus.CONVERTED && 
               status != LeadStatus.LOST && 
               status != LeadStatus.ARCHIVED;
    }
    
    /**
     * Check if lead has been opened by agent
     */
    public boolean isOpened() {
        return openedAt != null;
    }
    
    /**
     * Check if lead has been responded to
     */
    public boolean isResponded() {
        return firstResponseAt != null;
    }
    
    /**
     * Mark lead as opened (first view by agent)
     */
    public void markAsOpened() {
        if (this.openedAt == null) {
            this.openedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Mark first response sent
     */
    public void markFirstResponse() {
        if (this.firstResponseAt == null) {
            this.firstResponseAt = LocalDateTime.now();
        }
        this.lastContactAt = LocalDateTime.now();
    }
    
    /**
     * Assign lead to agent
     */
    public void assignToAgent(Agent agent) {
        this.assignedAgent = agent;
        this.assignedAt = LocalDateTime.now();
        this.status = LeadStatus.OPENED;
    }
    
    /**
     * Get lead age in days
     */
    public Long getAgeInDays() {
        if (receivedAt == null) return null;
        return Duration.between(receivedAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get metadata as map (convenience method for JSON field)
     */
    @Transient
    public Map<String, Object> getMetadata() {
        return JsonUtils.parseStringObjectMap(metadataJson);
    }

    /**
     * Set metadata from map (convenience method for JSON field)
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadataJson = JsonUtils.toJson(metadata);
    }

    /**
     * Get tags as list (convenience method for comma-separated field)
     */
    @Transient
    public List<String> getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

    /**
     * Set tags from list (convenience method for comma-separated field)
     */
    public void setTagsList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * Add a tag
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) return;
        
        List<String> currentTags = getTagsList();
        if (!currentTags.contains(tag.trim())) {
            currentTags.add(tag.trim());
            setTagsList(currentTags);
        }
    }

    /**
     * Remove a tag
     */
    public void removeTag(String tag) {
        if (tag == null) return;
        
        List<String> currentTags = getTagsList();
        currentTags.remove(tag.trim());
        setTagsList(currentTags);
    }

    /**
     * Check if has specific tag
     */
    @Transient
    public boolean hasTag(String tag) {
        return getTagsList().contains(tag.trim());
    }
    
    // Getters and setters 

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RealEstate getProperty() {
		return property;
	}

	public void setProperty(RealEstate property) {
		this.property = property;
	}

	public Agent getAssignedAgent() {
		return assignedAgent;
	}

	public void setAssignedAgent(Agent assignedAgent) {
		this.assignedAgent = assignedAgent;
	}

	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getReferrerUrl() {
		return referrerUrl;
	}

	public void setReferrerUrl(String referrerUrl) {
		this.referrerUrl = referrerUrl;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LeadStatus getStatus() {
		return status;
	}

	public void setStatus(LeadStatus status) {
		this.status = status;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getInterestLevel() {
		return interestLevel;
	}

	public void setInterestLevel(Integer interestLevel) {
		this.interestLevel = interestLevel;
	}

	public LocalDateTime getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(LocalDateTime receivedAt) {
		this.receivedAt = receivedAt;
	}

	public LocalDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public LocalDateTime getOpenedAt() {
		return openedAt;
	}

	public void setOpenedAt(LocalDateTime openedAt) {
		this.openedAt = openedAt;
	}

	public LocalDateTime getFirstResponseAt() {
		return firstResponseAt;
	}

	public void setFirstResponseAt(LocalDateTime firstResponseAt) {
		this.firstResponseAt = firstResponseAt;
	}

	public LocalDateTime getLastContactAt() {
		return lastContactAt;
	}

	public void setLastContactAt(LocalDateTime lastContactAt) {
		this.lastContactAt = lastContactAt;
	}

	public LocalDateTime getFollowUpAt() {
		return followUpAt;
	}

	public void setFollowUpAt(LocalDateTime followUpAt) {
		this.followUpAt = followUpAt;
	}

	public LocalDateTime getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.closedAt = closedAt;
	}

	public Boolean getHasWarning() {
		return hasWarning;
	}

	public void setHasWarning(Boolean hasWarning) {
		this.hasWarning = hasWarning;
	}

	public String getWarningType() {
		return warningType;
	}

	public void setWarningType(String warningType) {
		this.warningType = warningType;
	}

	public LocalDateTime getWarningGeneratedAt() {
		return warningGeneratedAt;
	}

	public void setWarningGeneratedAt(LocalDateTime warningGeneratedAt) {
		this.warningGeneratedAt = warningGeneratedAt;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
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
