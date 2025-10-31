package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.enums.HeatingType;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyCondition;
import com.doublez.backend.enums.PropertyType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class RealEstateUpdateDTO {
    
    // Existing fields
    private String title;
    private String description;
    private PropertyType propertyType;
    private ListingType listingType;
    private BigDecimal price;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String sizeInSqMt;
    private List<String> features;
    private Long ownerId;

    // 🆕 NEW: Geographic coordinates
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    // 🆕 NEW: Room and floor information
    @DecimalMin(value = "0.5", message = "Room count must be at least 0.5")
    @DecimalMax(value = "20.0", message = "Room count cannot exceed 20")
    private BigDecimal roomCount;
    
    @Min(value = -5, message = "Floor cannot be lower than -5 (5th basement)")
    @Max(value = 200, message = "Floor cannot be higher than 200")
    private Integer floor;
    
    @Min(value = 1, message = "Total floors must be at least 1")
    @Max(value = 200, message = "Total floors cannot exceed 200")
    private Integer totalFloors;
    
    // 🆕 NEW: Property characteristics
    @Min(value = 1500, message = "Construction year must be realistic")
    @Max(value = 2030, message = "Construction year cannot be in far future")
    private Integer constructionYear;
    
    private String municipality;
    
    // 🆕 NEW: ENUM fields
    private HeatingType heatingType;
    private PropertyCondition propertyCondition;

    // Constructors
    public RealEstateUpdateDTO() {
    }

    public RealEstateUpdateDTO(
            String title, 
            String description,
            PropertyType propertyType,
            ListingType listingType,
            BigDecimal price,
            String address,
            String city, 
            String state,
            String zipCode,
            String sizeInSqMt,
            List<String> features,
            Long ownerId,
            // 🆕 NEW: Add new fields to constructor
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal roomCount,
            Integer floor,
            Integer totalFloors,
            Integer constructionYear,
            String municipality,
            HeatingType heatingType,
            PropertyCondition propertyCondition) {
        
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
        this.features = features != null ? features : new ArrayList<>();
        this.ownerId = ownerId;
        
        // 🆕 NEW: Initialize new fields
        this.latitude = latitude;
        this.longitude = longitude;
        this.roomCount = roomCount;
        this.floor = floor;
        this.totalFloors = totalFloors;
        this.constructionYear = constructionYear;
        this.municipality = municipality;
        this.heatingType = heatingType;
        this.propertyCondition = propertyCondition;
    }

    // Getters and Setters for existing fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public String getSizeInSqMt() {
        return sizeInSqMt;
    }

    public void setSizeInSqMt(String sizeInSqMt) {
        this.sizeInSqMt = sizeInSqMt;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    // 🆕 NEW: Getters and Setters for new fields
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(BigDecimal roomCount) {
        this.roomCount = roomCount;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public HeatingType getHeatingType() {
        return heatingType;
    }

    public void setHeatingType(HeatingType heatingType) {
        this.heatingType = heatingType;
    }

    public PropertyCondition getPropertyCondition() {
        return propertyCondition;
    }

    public void setPropertyCondition(PropertyCondition propertyCondition) {
        this.propertyCondition = propertyCondition;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "RealEstateUpdateDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", propertyType=" + propertyType +
                ", listingType=" + listingType +
                ", price=" + price +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", sizeInSqMt='" + sizeInSqMt + '\'' +
                ", features=" + features +
                ", ownerId=" + ownerId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", roomCount=" + roomCount +
                ", floor=" + floor +
                ", totalFloors=" + totalFloors +
                ", constructionYear=" + constructionYear +
                ", municipality='" + municipality + '\'' +
                ", heatingType=" + heatingType +
                ", propertyCondition=" + propertyCondition +
                '}';
    }
}