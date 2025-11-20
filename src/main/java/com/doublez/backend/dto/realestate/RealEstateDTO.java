package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;

public class RealEstateDTO {
    private Long propertyId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private ListingType listingType;
    private BigDecimal price;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private BigDecimal sizeInSqMt;
    private List<String> features;
    private List<String> images;
    private Long ownerId;
    private Long agencyId;
    private String agencyName;
    private String agentName;
    private String agentPhone;
    private String agentLicense;
    private Boolean isActive;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    // 🆕 ADD MISSING FIELDS
    private Boolean isFeatured;
    private LocalDateTime featuredAt;
    private LocalDateTime featuredUntil;

    // Constructors
    public RealEstateDTO() {
    }

    // Updated constructor
    public RealEstateDTO(String title, String description, PropertyType propertyType, ListingType listingType,
            BigDecimal price, String address, String city, String state, String zipCode, BigDecimal sizeInSqMt,
            List<String> features, List<String> images, Long ownerId, Long agencyId, String agentName,
            String agentPhone, String agentLicense, Boolean isActive, Boolean isFeatured, 
            LocalDateTime featuredAt, LocalDateTime featuredUntil) {
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.listingType = listingType;
        this.price = price;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.sizeInSqMt = sizeInSqMt;
        this.features = features;
        this.images = images;
        this.ownerId = ownerId;
        this.agencyId = agencyId;
        this.agentName = agentName;
        this.agentPhone = agentPhone;
        this.agentLicense = agentLicense;
        this.isActive = isActive != null ? isActive : true;
        this.isFeatured = isFeatured != null ? isFeatured : false;
        this.featuredAt = featuredAt;
        this.featuredUntil = featuredUntil;
    }

    // Getters and setters for all fields
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public ListingType getListingType() { return listingType; }
    public void setListingType(ListingType listingType) { this.listingType = listingType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public BigDecimal getSizeInSqMt() { return sizeInSqMt; }
    public void setSizeInSqMt(BigDecimal sizeInSqMt) { this.sizeInSqMt = sizeInSqMt; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getAgentPhone() { return agentPhone; }
    public void setAgentPhone(String agentPhone) { this.agentPhone = agentPhone; }

    public String getAgentLicense() { return agentLicense; }
    public void setAgentLicense(String agentLicense) { this.agentLicense = agentLicense; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public LocalDateTime getFeaturedAt() { return featuredAt; }
    public void setFeaturedAt(LocalDateTime featuredAt) { this.featuredAt = featuredAt; }

    public LocalDateTime getFeaturedUntil() { return featuredUntil; }
    public void setFeaturedUntil(LocalDateTime featuredUntil) { this.featuredUntil = featuredUntil; }
}