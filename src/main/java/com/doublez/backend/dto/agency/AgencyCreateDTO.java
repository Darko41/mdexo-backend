package com.doublez.backend.dto.agency;

import com.doublez.backend.entity.user.UserTier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgencyCreateDTO {
    @NotBlank
    private String name;
    
    private String description;
    private String logo;
    
    @Email
    private String contactEmail;
    
    private String contactPhone;
    private String website;
    private String licenseNumber;
    
    // Business registration
    private String pib;
    private String mb;
    
    // Address
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Tier selection
    @NotNull
    private UserTier tier = UserTier.AGENCY_FREE;
    
    // Trial activation
    private Boolean startTrial = true;

    // Constructors
    public AgencyCreateDTO() {}

    public AgencyCreateDTO(String name, String description, UserTier tier) {
        this.name = name;
        this.description = description;
        this.tier = tier;
    }

    // Helper methods
    public boolean hasBusinessRegistration() {
        return pib != null && !pib.trim().isEmpty() && mb != null && !mb.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return (contactEmail != null && !contactEmail.trim().isEmpty()) || 
               (contactPhone != null && !contactPhone.trim().isEmpty());
    }

    public boolean hasLocationInfo() {
        return address != null || city != null || country != null;
    }

    // Getters and Setters
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
    public UserTier getTier() { return tier; }
    public void setTier(UserTier tier) { this.tier = tier; }
    public Boolean getStartTrial() { return startTrial; }
    public void setStartTrial(Boolean startTrial) { this.startTrial = startTrial; }

    @Override
    public String toString() {
        return "AgencyCreateDTO{" +
                "name='" + name + '\'' +
                ", tier=" + tier +
                ", hasBusinessRegistration=" + hasBusinessRegistration() +
                '}';
    }
}