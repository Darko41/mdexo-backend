package com.doublez.backend.dto.agency;

import jakarta.validation.constraints.Email;

public class AgencyUpdateDTO {
    private String name;
    private String description;
    private String logo;
    
    @Email
    private String contactEmail;
    
    private String contactPhone;
    private String website;
    
    // Address updates
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // Constructors
    public AgencyUpdateDTO() {}

    // Helper methods
    public boolean hasContactInfoUpdate() {
        return contactEmail != null || contactPhone != null || website != null;
    }

    public boolean hasLocationUpdate() {
        return address != null || city != null || state != null || zipCode != null || country != null;
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
}
