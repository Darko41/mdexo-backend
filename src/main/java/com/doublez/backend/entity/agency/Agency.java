package com.doublez.backend.entity.agency;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.doublez.backend.dto.agency.AgencyCreateDTO;
import com.doublez.backend.dto.agency.AgencyResponseDTO;
import com.doublez.backend.dto.trial.TrialInfoDTO;
import com.doublez.backend.entity.Lead;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.entity.warning.ActiveWarning;
import com.doublez.backend.entity.warning.WarningConfiguration;
import com.doublez.backend.enums.VerificationStatus;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.enums.agency.InvitationStatus;
import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "agencies")
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    private String logo;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "license_number", nullable = true)
    private String licenseNumber;
    
    // 🆕 ADD BUSINESS REGISTRATION FIELDS
    @Column(name = "pib", length = 9) // Serbian Tax ID
    private String pib;
    
    @Column(name = "mb", length = 8) // Serbian Business ID  
    private String mb;
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    // 🆕 ADD TIER SYSTEM
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private UserTier tier = UserTier.AGENCY_FREE;

    // 🆕 ADD TRIAL MANAGEMENT
    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "trial_used")
    private Boolean trialUsed = false;

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;  // This user MUST have ROLE_AGENCY

    // 🆕 CHANGE TO LocalDateTime
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    @Column(name = "country")
    private String country;
    
    // 🆕 VERIFICATION SYSTEM
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID who verified

    @Column(name = "verification_notes", length = 1000)
    private String verificationNotes;

    @Column(name = "verification_documents") // JSON or comma-separated document references
    private String verificationDocuments;

    @Column(name = "verification_submitted_at")
    private LocalDateTime verificationSubmittedAt;
    
    // Boosts and features
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "showcase_featured_until")
    private LocalDateTime showcaseFeaturedUntil;

    @Column(name = "is_showcase_featured")
    private Boolean isShowcaseFeatured = false;

    @Column(name = "premium_badge_until")
    private LocalDateTime premiumBadgeUntil;

    @Column(name = "has_premium_badge")
    private Boolean hasPremiumBadge = false;
    
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Agent> agents = new ArrayList<>();

    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Invitation> invitations = new ArrayList<>();
    
    // WARNING/NOTIFICATION SYSTEM =====
    
    @Column(name = "plan_limits_json", columnDefinition = "JSON")
    private String planLimitsJson; // Tier limits and features
    
    @Column(name = "current_usage_json", columnDefinition = "JSON")
    private String currentUsageJson; // Current usage counters
    
    @Column(name = "notification_settings_json", columnDefinition = "JSON")
    private String notificationSettingsJson; // Agency-wide notification preferences
    
    @Column(name = "warning_config_json", columnDefinition = "JSON")
    private String warningConfigJson; // Agency warning configuration
    
    @Column(name = "last_warning_check")
    private LocalDateTime lastWarningCheck; // When warnings were last checked
    
    // ===== NEW RELATIONSHIPS =====
    
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY)
    private List<Lead> leads = new ArrayList<>(); // All leads for this agency
    
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY)
    private List<ActiveWarning> warnings = new ArrayList<>(); // Agency warnings
    
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY)
    private List<WarningConfiguration> warningConfigurations = new ArrayList<>(); // Custom warning settings

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ========================
    // CONSTRUCTORS
    // ========================

    public Agency() {
    }

    public Agency(String name, String description, User admin, String licenseNumber) {
        this.name = name;
        this.description = description;
        this.admin = admin;
        this.licenseNumber = licenseNumber;
        this.isActive = true;
        this.tier = UserTier.AGENCY_FREE;
    }

    public Agency(String name, String description, String logo, String contactEmail, 
                  String contactPhone, String website, String licenseNumber, User admin) {
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.website = website;
        this.licenseNumber = licenseNumber;
        this.admin = admin;
        this.isActive = true;
        this.tier = UserTier.AGENCY_FREE;
    }

    public Agency(String name, String description, String logo, String contactEmail, 
                  String contactPhone, String website, String licenseNumber, User admin,
                  String address, String city, String state, String zipCode, String country) {
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.website = website;
        this.licenseNumber = licenseNumber;
        this.admin = admin;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
        this.isActive = true;
        this.tier = UserTier.AGENCY_FREE;
    }

    /**
     * 🆕 Constructor from AgencyCreateDTO with tier selection
     */
    public static Agency fromCreateDto(AgencyCreateDTO createDto, User admin) {
        Agency agency = new Agency();
        agency.setName(createDto.getName());
        agency.setDescription(createDto.getDescription());
        agency.setLogo(createDto.getLogo());
        agency.setContactEmail(createDto.getContactEmail());
        agency.setContactPhone(createDto.getContactPhone());
        agency.setWebsite(createDto.getWebsite());
        agency.setLicenseNumber(createDto.getLicenseNumber());
        agency.setPib(createDto.getPib());
        agency.setMb(createDto.getMb());
        agency.setAdmin(admin);
        agency.setAddress(createDto.getAddress());
        agency.setCity(createDto.getCity());
        agency.setState(createDto.getState());
        agency.setZipCode(createDto.getZipCode());
        agency.setCountry(createDto.getCountry());
        agency.setIsActive(true);
        
        // 🆕 SET TIER FROM DTO (user's choice during registration)
        agency.setTier(createDto.getTier() != null ? createDto.getTier() : UserTier.AGENCY_FREE);
        
        // 🆕 START TRIAL IF APPLICABLE
        if (createDto.getStartTrial() != null && createDto.getStartTrial()) {
            agency.startTrial(45); // 45-day trial
        }
        
        return agency;
    }
    
    /**
     * Convert Agency entity to AgencyResponseDTO
     */
    public AgencyResponseDTO toResponseDTO() {
        AgencyResponseDTO dto = new AgencyResponseDTO();
        dto.setId(this.id);
        dto.setName(this.name);
        dto.setDescription(this.description);
        dto.setLogo(this.logo);
        dto.setContactEmail(this.contactEmail);
        dto.setContactPhone(this.contactPhone);
        dto.setWebsite(this.website);
        dto.setLicenseNumber(this.licenseNumber);
        dto.setPib(this.pib);
        dto.setMb(this.mb);
        dto.setIsActive(this.isActive);
        dto.setTier(this.tier);
        dto.setEffectiveTier(this.getEffectiveTier());
        dto.setAddress(this.address);
        dto.setCity(this.city);
        dto.setState(this.state);
        dto.setZipCode(this.zipCode);
        dto.setCountry(this.country);
        dto.setFullAddress(this.getFullAddress());
        dto.setLocationDisplay(this.getLocationDisplay());
        dto.setCreatedAt(this.createdAt);
        
        // Set trial info
        TrialInfoDTO trialInfo = new TrialInfoDTO();
        trialInfo.setTrialUsed(this.trialUsed);
        trialInfo.setTrialEndDate(this.trialEndDate);
        trialInfo.setInTrialPeriod(this.isInTrialPeriod());
        trialInfo.setDaysRemaining(this.getTrialDaysRemaining());
        dto.setTrialInfo(trialInfo);
        
        // 🆕 Verification mappings
        dto.setVerificationStatus(this.verificationStatus);
        dto.setVerifiedAt(this.verifiedAt);
        dto.setVerifiedBy(this.verifiedBy);
        dto.setVerificationNotes(this.verificationNotes);
        dto.setVerificationDocuments(this.verificationDocuments != null ? 
            Arrays.asList(this.verificationDocuments.split(",")) : new ArrayList<>());
        dto.setVerificationSubmittedAt(this.verificationSubmittedAt);
        dto.setIsVerified(this.isVerified());
        
        return dto;
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    /**
     * 🆕 Trial management
     */
    public boolean isInTrialPeriod() {
        return Boolean.TRUE.equals(trialUsed) && 
               trialEndDate != null && 
               LocalDateTime.now().isBefore(trialEndDate);
    }

    public boolean isTrialExpired() {
        return Boolean.TRUE.equals(trialUsed) &&
               trialEndDate != null &&
               LocalDateTime.now().isAfter(trialEndDate);
    }

    public void startTrial(int trialDays) {
        this.trialUsed = true;
        this.trialEndDate = LocalDateTime.now().plusDays(trialDays);
    }

    public void endTrial() {
        this.trialUsed = false;
        this.trialEndDate = null;
    }

    /**
     * 🆕 Effective tier calculation (trial overrides chosen tier)
     */
    public UserTier getEffectiveTier() {
        if (isInTrialPeriod()) {
            return UserTier.AGENCY_PRO; // Trial gives PRO features
        }
        return this.tier;
    }

    /**
     * 🆕 Tier upgrade/downgrade
     */
    public void upgradeTier(UserTier newTier) {
        if (newTier.ordinal() > this.tier.ordinal()) {
            this.tier = newTier;
        }
    }

    public void downgradeTier(UserTier newTier) {
        if (newTier.ordinal() < this.tier.ordinal()) {
            this.tier = newTier;
        }
    }

    /**
     * 🆕 Check if agency can create more listings
     */
    public boolean canCreateListing() {
        // Implementation will depend on RealEstateService integration
        // This is a placeholder for business logic
        return true;
    }

    /**
     * 🆕 Check if agency can upload more images
     */
    public boolean canUploadImage() {
        // Implementation will depend on RealEstateService integration  
        // This is a placeholder for business logic
        return true;
    }
    
    public boolean isActive() {
        return Boolean.TRUE.equals(getIsActive());
    }
    
    // ========================
    // VERIFICATION BUSINESS METHODS
    // ========================

    public void submitVerification(String documents) {
        this.verificationStatus = VerificationStatus.SUBMITTED;
        this.verificationDocuments = documents;
        this.verificationSubmittedAt = LocalDateTime.now();
    }

    public void approveVerification(Long adminUserId, String notes) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = adminUserId;
        this.verificationNotes = notes;
    }

    public void rejectVerification(Long adminUserId, String notes) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = adminUserId;
        this.verificationNotes = notes;
    }

    public void requestVerification(String notes) {
        this.verificationStatus = VerificationStatus.REQUESTED;
        this.verificationNotes = notes;
    }

    public boolean canListProperties() {
        return isVerified() && isActive();
    }

    // ========================
    // HELPER METHODS
    // ========================

    public String getDisplayContactInfo() {
        StringBuilder sb = new StringBuilder();
        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            sb.append("Email: ").append(contactEmail);
        }
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Phone: ").append(contactPhone);
        }
        if (website != null && !website.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Website: ").append(website);
        }
        return sb.length() > 0 ? sb.toString() : "No contact information";
    }

    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
    }

    public boolean hasContactEmail() {
        return contactEmail != null && !contactEmail.trim().isEmpty();
    }

    public boolean hasContactPhone() {
        return contactPhone != null && !contactPhone.trim().isEmpty();
    }

    public String getFullAddress() {
        StringBuilder addressBuilder = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            addressBuilder.append(address);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(state);
        }
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" ");
            addressBuilder.append(zipCode);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(country);
        }
        return addressBuilder.length() > 0 ? addressBuilder.toString() : "Address not specified";
    }

    public boolean hasLocation() {
        return (city != null && !city.trim().isEmpty()) || 
               (address != null && !address.trim().isEmpty());
    }

    public String getLocationDisplay() {
        if (city != null && state != null) {
            return city + ", " + state;
        } else if (city != null) {
            return city;
        } else if (state != null) {
            return state;
        }
        return "Location not specified";
    }

    public void checkAndResetExpiredFeatures() {
        LocalDateTime now = LocalDateTime.now();
        if (featuredUntil != null && now.isAfter(featuredUntil)) {
            featuredUntil = null;
            isFeatured = false;
        }
    }
    
    // Agent helper methods
    public List<Agent> getActiveAgents() {
        if (agents == null) return new ArrayList<>();
        return agents.stream()
                .filter(Agent::getIsActive)
                .collect(Collectors.toList());
    }

    public List<Agent> getAgentsByRole(AgentRole role) {
        if (agents == null) return new ArrayList<>();
        return agents.stream()
                .filter(Agent::getIsActive)
                .filter(agent -> agent.getRole() == role)
                .collect(Collectors.toList());
    }

    public int getAgentCount() {
        return getActiveAgents().size();
    }

    public int getSuperAgentCount() {
        return getAgentsByRole(AgentRole.SUPER_AGENT).size();
    }

    public boolean hasReachedAgentLimit() {
        // This will be implemented based on tier limits
        return false;
    }

    public List<Invitation> getPendingInvitations() {
        if (invitations == null) return new ArrayList<>();
        return invitations.stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * Get plan limits as map
     */
    @Transient
    public Map<String, Object> getPlanLimits() {
        return JsonUtils.parseStringObjectMap(planLimitsJson);
    }
    
    /**
     * Set plan limits from map
     */
    public void setPlanLimits(Map<String, Object> limits) {
        this.planLimitsJson = JsonUtils.toJson(limits);
    }
    
    /**
     * Get current usage as map
     */
    @Transient
    public Map<String, Object> getCurrentUsage() {
        return JsonUtils.parseStringObjectMap(currentUsageJson);
    }
    
    /**
     * Set current usage from map
     */
    public void setCurrentUsage(Map<String, Object> usage) {
        this.currentUsageJson = JsonUtils.toJson(usage);
    }
    
    /**
     * Update usage for a specific resource
     */
    public void updateUsage(String resource, Object value) {
        Map<String, Object> usage = getCurrentUsage();
        usage.put(resource, value);
        setCurrentUsage(usage);
    }
    
    /**
     * Get specific plan limit
     */
    @Transient
    public Object getPlanLimit(String key) {
        return getPlanLimits().get(key);
    }
    
    /**
     * Check if agency has exceeded a plan limit
     */
    @Transient
    public boolean hasExceededLimit(String limitKey) {
        Map<String, Object> limits = getPlanLimits();
        Map<String, Object> usage = getCurrentUsage();
        
        if (!limits.containsKey(limitKey) || !usage.containsKey(limitKey)) {
            return false;
        }
        
        Object limitValue = limits.get(limitKey);
        Object usageValue = usage.get(limitKey);
        
        if (limitValue instanceof Number && usageValue instanceof Number) {
            return ((Number) usageValue).doubleValue() > ((Number) limitValue).doubleValue();
        }
        
        return false;
    }
    
    /**
     * Get notification settings as map
     */
    @Transient
    public Map<String, Object> getNotificationSettings() {
        return JsonUtils.parseStringObjectMap(notificationSettingsJson);
    }
    
    /**
     * Set notification settings from map
     */
    public void setNotificationSettings(Map<String, Object> settings) {
        this.notificationSettingsJson = JsonUtils.toJson(settings);
    }
    
    /**
     * Get warning configuration as map
     */
    @Transient
    public Map<String, Object> getWarningConfig() {
        return JsonUtils.parseStringObjectMap(warningConfigJson);
    }
    
    /**
     * Set warning configuration from map
     */
    public void setWarningConfig(Map<String, Object> config) {
        this.warningConfigJson = JsonUtils.toJson(config);
    }
    
    /**
     * Check if agency is on trial
     */
    @Transient
    public boolean isOnTrial() {
        return trialEndDate != null && LocalDateTime.now().isBefore(trialEndDate);
    }
    
    /**
     * Get days remaining in trial
     */
    @Transient
    public Long getTrialDaysRemaining() {
        if (trialEndDate == null || !isOnTrial()) return 0L;
        return Duration.between(LocalDateTime.now(), trialEndDate).toDays();
    }
    
    /**
     * Check if agency is verified
     */
    @Transient
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
    
    /**
     * Check if agency can add more agents
     */
    @Transient
    public boolean canAddMoreAgents() {
        if (hasExceededLimit("maxAgents")) return false;
        
        // Also check actual agent count
        if (agents != null) {
            Map<String, Object> limits = getPlanLimits();
            Integer maxAgents = limits.containsKey("maxAgents") ? 
                ((Number) limits.get("maxAgents")).intValue() : null;
            
            if (maxAgents != null && agents.size() >= maxAgents) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if agency can add more listings
     */
    @Transient
    public boolean canAddMoreListings(Integer currentListingCount) {
        if (hasExceededLimit("maxListings")) return false;
        
        Map<String, Object> limits = getPlanLimits();
        Integer maxListings = limits.containsKey("maxListings") ? 
            ((Number) limits.get("maxListings")).intValue() : null;
        
        if (maxListings != null && currentListingCount >= maxListings) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get active agent count
     */
    @Transient
    public long getActiveAgentCount() {
        if (agents == null) return 0;
        return agents.stream()
            .filter(agent -> Boolean.TRUE.equals(agent.getIsActive()))
            .count();
    }
    
    /**
     * Get super agent (team lead)
     */
    @Transient
    public Optional<Agent> getSuperAgent() {
        if (agents == null) return Optional.empty();
        return agents.stream()
            .filter(agent -> AgentRole.SUPER_AGENT.equals(agent.getRole()))
            .filter(agent -> Boolean.TRUE.equals(agent.getIsActive()))
            .findFirst();
    }
    
    /**
     * Check if agency is currently featured
     */
    @Transient
    public boolean isCurrentlyFeatured() {
        if (!Boolean.TRUE.equals(isFeatured)) return false;
        if (featuredUntil == null) return true;
        return LocalDateTime.now().isBefore(featuredUntil);
    }
    
    /**
     * Check if agency has premium badge
     */
    @Transient
    public boolean hasActivePremiumBadge() {
        if (!Boolean.TRUE.equals(hasPremiumBadge)) return false;
        if (premiumBadgeUntil == null) return true;
        return LocalDateTime.now().isBefore(premiumBadgeUntil);
    }
    
    /**
     * Initialize default plan limits based on tier
     */
    public void initializePlanLimits() {
        Map<String, Object> limits = new HashMap<>();
        
        switch (tier) {
            case AGENCY_FREE:
                limits.put("maxAgents", 1);
                limits.put("maxListings", 10);
                limits.put("maxStorageMB", 100);
                limits.put("features", Arrays.asList("basic_listing", "website_embed"));
                break;
            case AGENCY_BASIC:
                limits.put("maxAgents", 3);
                limits.put("maxListings", 50);
                limits.put("maxStorageMB", 1000);
                limits.put("features", Arrays.asList("advanced_analytics", "team_management", "basic_support"));
                break;
            case AGENCY_PRO:
                limits.put("maxAgents", 10);
                limits.put("maxListings", 200);
                limits.put("maxStorageMB", 5000);
                limits.put("features", Arrays.asList("white_label", "api_access", "priority_support", "custom_domain"));
                break;
            case AGENCY_PREMIUM:
                limits.put("maxAgents", -1); // Unlimited
                limits.put("maxListings", -1); // Unlimited
                limits.put("maxStorageMB", -1); // Unlimited
                limits.put("features", Arrays.asList("all_features", "dedicated_support", "ai_analytics", "custom_development"));
                break;
        }
        
        setPlanLimits(limits);
        
        // Initialize usage to zero
        Map<String, Object> usage = new HashMap<>();
        usage.put("agentCount", 0);
        usage.put("listingCount", 0);
        usage.put("storageUsedMB", 0);
        usage.put("leadCount", 0);
        setCurrentUsage(usage);
    }
    
    /**
     * Initialize default notification settings
     */
    public void initializeNotificationSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("emailAlerts", true);
        settings.put("smsAlerts", false);
        settings.put("pushAlerts", false);
        settings.put("warningFrequency", "DAILY");
        settings.put("reportSchedule", "WEEKLY");
        setNotificationSettings(settings);
    }
    
    // ===== LIFECYCLE METHODS =====
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        
        // Initialize JSON fields if null
        if (planLimitsJson == null) {
            initializePlanLimits();
        }
        if (notificationSettingsJson == null) {
            initializeNotificationSettings();
        }
        if (warningConfigJson == null) {
            setWarningConfig(new HashMap<>());
        }
        if (currentUsageJson == null) {
            Map<String, Object> usage = new HashMap<>();
            usage.put("agentCount", 0);
            usage.put("listingCount", 0);
            setCurrentUsage(usage);
        }
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getPib() { return pib; }
    public void setPib(String pib) { this.pib = pib; }

    public String getMb() { return mb; }
    public void setMb(String mb) { this.mb = mb; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public UserTier getTier() { return tier; }
    public void setTier(UserTier tier) { this.tier = tier; }

    public LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }

    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }

    public User getAdmin() { return admin; }
    public void setAdmin(User admin) { this.admin = admin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public VerificationStatus getVerificationStatus() {
		return verificationStatus;
	}

	public void setVerificationStatus(VerificationStatus verificationStatus) {
		this.verificationStatus = verificationStatus;
	}

	public LocalDateTime getVerifiedAt() {
		return verifiedAt;
	}

	public void setVerifiedAt(LocalDateTime verifiedAt) {
		this.verifiedAt = verifiedAt;
	}

	public Long getVerifiedBy() {
		return verifiedBy;
	}

	public void setVerifiedBy(Long verifiedBy) {
		this.verifiedBy = verifiedBy;
	}

	public String getVerificationNotes() {
		return verificationNotes;
	}

	public void setVerificationNotes(String verificationNotes) {
		this.verificationNotes = verificationNotes;
	}

	public String getVerificationDocuments() {
		return verificationDocuments;
	}

	public void setVerificationDocuments(String verificationDocuments) {
		this.verificationDocuments = verificationDocuments;
	}

	public LocalDateTime getVerificationSubmittedAt() {
		return verificationSubmittedAt;
	}

	public void setVerificationSubmittedAt(LocalDateTime verificationSubmittedAt) {
		this.verificationSubmittedAt = verificationSubmittedAt;
	}

	public LocalDateTime getFeaturedUntil() {
		return featuredUntil;
	}

	public void setFeaturedUntil(LocalDateTime featuredUntil) {
		this.featuredUntil = featuredUntil;
	}

	public Boolean getIsFeatured() {
		return isFeatured;
	}

	public void setIsFeatured(Boolean isFeatured) {
		this.isFeatured = isFeatured;
	}

	public LocalDateTime getShowcaseFeaturedUntil() {
		return showcaseFeaturedUntil;
	}

	public void setShowcaseFeaturedUntil(LocalDateTime showcaseFeaturedUntil) {
		this.showcaseFeaturedUntil = showcaseFeaturedUntil;
	}

	public Boolean getIsShowcaseFeatured() {
		return isShowcaseFeatured;
	}

	public void setIsShowcaseFeatured(Boolean isShowcaseFeatured) {
		this.isShowcaseFeatured = isShowcaseFeatured;
	}

	public LocalDateTime getPremiumBadgeUntil() {
		return premiumBadgeUntil;
	}

	public void setPremiumBadgeUntil(LocalDateTime premiumBadgeUntil) {
		this.premiumBadgeUntil = premiumBadgeUntil;
	}

	public Boolean getHasPremiumBadge() {
		return hasPremiumBadge;
	}

	public void setHasPremiumBadge(Boolean hasPremiumBadge) {
		this.hasPremiumBadge = hasPremiumBadge;
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	public List<Invitation> getInvitations() {
		return invitations;
	}

	public void setInvitations(List<Invitation> invitations) {
		this.invitations = invitations;
	}

	public String getPlanLimitsJson() {
		return planLimitsJson;
	}

	public void setPlanLimitsJson(String planLimitsJson) {
		this.planLimitsJson = planLimitsJson;
	}

	public String getCurrentUsageJson() {
		return currentUsageJson;
	}

	public void setCurrentUsageJson(String currentUsageJson) {
		this.currentUsageJson = currentUsageJson;
	}

	public String getNotificationSettingsJson() {
		return notificationSettingsJson;
	}

	public void setNotificationSettingsJson(String notificationSettingsJson) {
		this.notificationSettingsJson = notificationSettingsJson;
	}

	public String getWarningConfigJson() {
		return warningConfigJson;
	}

	public void setWarningConfigJson(String warningConfigJson) {
		this.warningConfigJson = warningConfigJson;
	}

	public LocalDateTime getLastWarningCheck() {
		return lastWarningCheck;
	}

	public void setLastWarningCheck(LocalDateTime lastWarningCheck) {
		this.lastWarningCheck = lastWarningCheck;
	}

	public List<Lead> getLeads() {
		return leads;
	}

	public void setLeads(List<Lead> leads) {
		this.leads = leads;
	}

	public List<ActiveWarning> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<ActiveWarning> warnings) {
		this.warnings = warnings;
	}

	public List<WarningConfiguration> getWarningConfigurations() {
		return warningConfigurations;
	}

	public void setWarningConfigurations(List<WarningConfiguration> warningConfigurations) {
		this.warningConfigurations = warningConfigurations;
	}

	@Override
    public String toString() {
        return "Agency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tier=" + tier +
                ", city='" + city + '\'' +
                ", isActive=" + isActive +
                ", trialActive=" + isInTrialPeriod() +
                '}';
    }
}