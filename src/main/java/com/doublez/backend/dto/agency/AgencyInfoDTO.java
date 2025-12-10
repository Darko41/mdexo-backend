package com.doublez.backend.dto.agency;

import com.doublez.backend.entity.user.UserTier;

public class AgencyInfoDTO {
    private Long id;
    private String name;
    private String description;
    private String logo;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private UserTier effectiveTier;
    private Boolean isActive;
    private String position; // User's position in agency (if needed)

    // Constructors
    public AgencyInfoDTO() {}

    public AgencyInfoDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public AgencyInfoDTO(Long id, String name, String description, String logo, 
                        String contactEmail, String contactPhone, String website,
                        UserTier effectiveTier, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.website = website;
        this.effectiveTier = effectiveTier;
        this.isActive = isActive;
    }

    // Helper methods
    public String getContactInfo() {
        StringBuilder sb = new StringBuilder();
        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            sb.append("Email: ").append(contactEmail);
        }
        if (contactPhone != null && !contactPhone.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Phone: ").append(contactPhone);
        }
        return sb.length() > 0 ? sb.toString() : "No contact information";
    }

    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return (contactEmail != null && !contactEmail.trim().isEmpty()) || 
               (contactPhone != null && !contactPhone.trim().isEmpty());
    }

    public boolean isPremiumTier() {
        return effectiveTier == UserTier.AGENCY_PREMIUM;
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
    public UserTier getEffectiveTier() { return effectiveTier; }
    public void setEffectiveTier(UserTier effectiveTier) { this.effectiveTier = effectiveTier; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}