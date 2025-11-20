package com.doublez.backend.entity.agency;

import java.time.LocalDate;

import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
    
    @Column(name = "is_active", nullable = true)
    private Boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
    }

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor (required by JPA)
     */
    public Agency() {
    }

    /**
     * Minimal constructor for basic agency creation
     */
    public Agency(String name, String description, User admin, String licenseNumber) {
        this.name = name;
        this.description = description;
        this.admin = admin;
        this.licenseNumber = licenseNumber;
        this.isActive = true;
    }

    /**
     * Extended constructor with contact information
     */
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
    }

    /**
     * 🆕 COMPLETE constructor with all fields including location
     */
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
    }

    /**
     * 🆕 Constructor from AgencyDTO.Create
     * Useful for service layer when creating from DTO
     */
    public static Agency fromCreateDto(AgencyDTO.Create createDto, User admin) {
        Agency agency = new Agency();
        agency.setName(createDto.getName());
        agency.setDescription(createDto.getDescription());
        agency.setLogo(createDto.getLogo());
        agency.setContactEmail(createDto.getContactEmail());
        agency.setContactPhone(createDto.getContactPhone());
        agency.setWebsite(createDto.getWebsite());
        agency.setLicenseNumber(createDto.getLicenseNumber());
        agency.setAdmin(admin);
        agency.setAddress(createDto.getAddress());
        agency.setCity(createDto.getCity());
        agency.setState(createDto.getState());
        agency.setZipCode(createDto.getZipCode());
        agency.setCountry(createDto.getCountry());
        agency.setIsActive(true);
        return agency;
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    /**
     * 🆕 Get full address as a single string
     */
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

    /**
     * 🆕 Check if agency has location information
     */
    public boolean hasLocation() {
        return (city != null && !city.trim().isEmpty()) || 
               (address != null && !address.trim().isEmpty());
    }

    /**
     * 🆕 Get location for display (city, state)
     */
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

    @Override
    public String toString() {
        return "Agency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
