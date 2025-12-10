package com.doublez.backend.dto.agency;

import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.dto.trial.TierLimitsDTO;
import com.doublez.backend.dto.trial.TrialInfoDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.enums.VerificationStatus;

public class AgencyResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String licenseNumber;
    private String pib;
    private String mb;
    private Boolean isActive;
    
    // Tier information
    private UserTier tier;
    private UserTier effectiveTier;
    private TierLimitsDTO tierLimits;
    
    // Trial information
    private TrialInfoDTO trialInfo;
    
    // Location
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String fullAddress;
    private String locationDisplay;
    
    // Admin info
    private UserResponseDTO admin;
    private LocalDateTime createdAt;
    
    // 🆕 VERIFICATION FIELDS
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;
    private String verificationNotes;
    private List<String> verificationDocuments;
    private LocalDateTime verificationSubmittedAt;
    private Boolean isVerified;

    // Constructors
    public AgencyResponseDTO() {}

    // Helper methods
    public boolean isInTrial() {
        return trialInfo != null && trialInfo.getInTrialPeriod();
    }

    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return (contactEmail != null && !contactEmail.trim().isEmpty()) || 
               (contactPhone != null && !contactPhone.trim().isEmpty());
    }

    // Getters and Setters
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
    public UserTier getEffectiveTier() { return effectiveTier; }
    public void setEffectiveTier(UserTier effectiveTier) { this.effectiveTier = effectiveTier; }
    public TierLimitsDTO getTierLimits() { return tierLimits; }
    public void setTierLimits(TierLimitsDTO tierLimits) { this.tierLimits = tierLimits; }
    public TrialInfoDTO getTrialInfo() { return trialInfo; }
    public void setTrialInfo(TrialInfoDTO trialInfo) { this.trialInfo = trialInfo; }
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
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getLocationDisplay() { return locationDisplay; }
    public void setLocationDisplay(String locationDisplay) { this.locationDisplay = locationDisplay; }
    public UserResponseDTO getAdmin() { return admin; }
    public void setAdmin(UserResponseDTO admin) { this.admin = admin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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

	public List<String> getVerificationDocuments() {
		return verificationDocuments;
	}

	public void setVerificationDocuments(List<String> verificationDocuments) {
		this.verificationDocuments = verificationDocuments;
	}

	public LocalDateTime getVerificationSubmittedAt() {
		return verificationSubmittedAt;
	}

	public void setVerificationSubmittedAt(LocalDateTime verificationSubmittedAt) {
		this.verificationSubmittedAt = verificationSubmittedAt;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}
}
