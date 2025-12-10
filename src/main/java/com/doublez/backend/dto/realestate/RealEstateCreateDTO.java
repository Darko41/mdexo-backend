package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.FurnitureStatus;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.OwnershipType;
import com.doublez.backend.enums.property.PropertyCondition;
import com.doublez.backend.enums.property.PropertySubtype;
import com.doublez.backend.enums.property.PropertyType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RealEstateCreateDTO {
    
    // ===== BASIC INFORMATION =====
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    // ===== PROPERTY CLASSIFICATION =====
    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    private PropertySubtype propertySubtype; 

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    // ===== PRICE & FINANCIAL =====
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 12, fraction = 2, message = "Price must have up to 12 integer and 2 decimal digits")
    private BigDecimal price;

    private String currency = "RSD"; 

    @Positive(message = "Original price must be positive")
    @Digits(integer = 12, fraction = 2, message = "Original price must have up to 12 integer and 2 decimal digits")
    private BigDecimal originalPrice; 

    private Boolean priceNegotiable = true; 
    
    @Positive(message = "Discount amount must be positive")
    @Digits(integer = 12, fraction = 2, message = "Discount amount must have up to 12 integer and 2 decimal digits")
    private BigDecimal discountAmount;

    @FutureOrPresent(message = "Discount end date must be today or in the future")
    private LocalDate discountEndDate;

    private Boolean includesUtilities; 

    @Positive(message = "Deposit amount must be positive")
    @Digits(integer = 12, fraction = 2, message = "Deposit amount must have up to 12 integer and 2 decimal digits")
    private BigDecimal depositAmount; 

    // ===== LOCATION INFORMATION =====
    @NotBlank(message = "Address is required")
    private String address;

    private String streetNumber; 

    private String neighborhood; 

    @NotBlank(message = "City is required")
    private String city;

    private String municipality;

    @NotBlank(message = "State is required")
    private String state = "Srbija";

    private String zipCode; // Made optional

    @Size(max = 500, message = "Location description cannot exceed 500 characters")
    private String locationDescription; 

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    // ===== PROPERTY CHARACTERISTICS - CORE =====
    @NotNull(message = "Size in square meters is required")
    @DecimalMin(value = "1.0", message = "Size must be at least 1 square meter")
    @Digits(integer = 6, fraction = 2, message = "Size must have up to 6 integer and 2 decimal digits")
    private BigDecimal sizeInSqMt;

    @DecimalMin(value = "0.5", message = "Room count must be at least 0.5 (studio)")
    @DecimalMax(value = "20.0", message = "Room count cannot exceed 20")
    @Digits(integer = 2, fraction = 1, message = "Room count must have up to 2 integer and 1 decimal digit")
    private BigDecimal roomCount;

    @DecimalMin(value = "0.5", message = "Bathroom count must be at least 0.5")
    @DecimalMax(value = "10.0", message = "Bathroom count cannot exceed 10")
    @Digits(integer = 2, fraction = 1, message = "Bathroom count must have up to 2 integer and 1 decimal digit")
    private BigDecimal bathroomCount; 

    @Min(value = 0, message = "Balcony count cannot be negative")
    @Max(value = 10, message = "Balcony count cannot exceed 10")
    private Integer balconyCount; 

    @Min(value = -5, message = "Floor cannot be less than -5 (basement)")
    @Max(value = 200, message = "Floor cannot exceed 200")
    private Integer floor;

    @Min(value = 1, message = "Total floors must be at least 1")
    @Max(value = 200, message = "Total floors cannot exceed 200")
    private Integer totalFloors;

    @Min(value = 1500, message = "Construction year must be after 1500")
    @Max(value = 2030, message = "Construction year cannot be in the future beyond 2030")
    private Integer constructionYear;

    private PropertyCondition propertyCondition;

    private HeatingType heatingType;

    @Size(max = 100, message = "Other heating type description cannot exceed 100 characters")
    private String otherHeatingTypeDescription; 

    private FurnitureStatus furnitureStatus; 

    // ===== AMENITIES & COMFORT =====
    private Boolean hasElevator; 
    private Boolean hasAirConditioning; 
    private Boolean hasInternet; 
    private Boolean hasCableTV; 
    private Boolean hasSecurity; 
    private Boolean hasParking = false; 
    private Integer parkingSpaces; 
    private Boolean hasGarden; 
    
    @DecimalMin(value = "0.0", message = "Garden size cannot be negative")
    @Digits(integer = 5, fraction = 2, message = "Garden size must have up to 5 integer and 2 decimal digits")
    private BigDecimal gardenSizeSqMt; 
    
    private Boolean hasTerrace; 
    private Boolean hasBalcony; 

    // ===== ENERGY & UTILITIES =====
    private EnergyEfficiency energyEfficiency; 
    private Boolean hasWater; 
    private Boolean hasSewage; 
    private Boolean hasElectricity; 
    private Boolean hasGas; 

    // ===== LEGAL & DOCUMENTATION =====
    private Boolean hasConstructionPermit; 
    private Boolean hasUsePermit; 
    private OwnershipType ownershipType; 
    
    @Size(max = 100, message = "Other ownership type description cannot exceed 100 characters")
    private String otherOwnershipTypeDescription; 
    
    private Boolean isRegistered; 

    // ===== COMMERCIAL-SPECIFIC =====
    @Size(max = 100, message = "Business type cannot exceed 100 characters")
    private String businessType; 
    private Boolean hasShowcaseWindow; 
    private Boolean hasStorageRoom; 
    
    @Min(value = 0, message = "Employee capacity cannot be negative")
    @Max(value = 1000, message = "Employee capacity cannot exceed 1000")
    private Integer employeeCapacity; 

    // ===== LAND-SPECIFIC =====
    @Size(max = 50, message = "Land type cannot exceed 50 characters")
    private String landType; 
    private Boolean hasWaterSource; 
    private Boolean hasElectricityAccess; 
    private Boolean hasRoadAccess; 

    // ===== OWNERSHIP & AGENCY =====
    @Nullable
    private Long ownerId;

    @Nullable
    private Long agencyId;

    // ===== AGENT OVERRIDES =====
    private String agentName;
    private String agentPhone;
    private String agentLicense;
    
    @Email(message = "Contact email must be valid")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    private String contactEmail; 
    
    private String preferredContactMethod; 

    // ===== AVAILABILITY =====
    @FutureOrPresent(message = "Available from date must be today or in the future")
    private LocalDate availableFrom; 
    
    @Min(value = 1, message = "Minimum rent period must be at least 1 month")
    @Max(value = 60, message = "Minimum rent period cannot exceed 60 months")
    private Integer minimumRentPeriod; 

    // ===== MEDIA & FEATURES =====
    @Size(max = 15, message = "Cannot have more than 15 features")
    private List<String> features = new ArrayList<>();

    private List<String> images = new ArrayList<>();

    // ===== STATUS =====
    @NotNull
    private Boolean isActive = true;

    @NotNull
    private Boolean isFeatured = false;

    // ===== VALIDATION METHODS =====
    
    /**
     * Custom validation for OTHER enum fields
     */
    public boolean hasValidOtherDescriptions() {
        if (heatingType == HeatingType.OTHER && 
            (otherHeatingTypeDescription == null || otherHeatingTypeDescription.trim().isEmpty())) {
            return false;
        }
        if (ownershipType == OwnershipType.OTHER && 
            (otherOwnershipTypeDescription == null || otherOwnershipTypeDescription.trim().isEmpty())) {
            return false;
        }
        return true;
    }
    
    /**
     * Validate that property subtype matches property type
     */
    public boolean isSubtypeValid() {
        if (propertySubtype == null) return true;
        return propertySubtype.getParentType() == propertyType;
    }
    
    /**
     * Validate commercial fields are only used with commercial properties
     */
    public boolean areCommercialFieldsValid() {
        if (businessType != null || hasShowcaseWindow != null || hasStorageRoom != null || employeeCapacity != null) {
            return propertyType == PropertyType.COMMERCIAL || propertyType == PropertyType.OFFICE_SPACE;
        }
        return true;
    }
    
    /**
     * Validate land fields are only used with land properties
     */
    public boolean areLandFieldsValid() {
        if (landType != null || hasWaterSource != null || hasElectricityAccess != null || hasRoadAccess != null) {
            return propertyType == PropertyType.LAND;
        }
        return true;
    }
    
    /**
     * Validate discount logic
     */
    @AssertTrue(message = "Cannot set both discount amount and have original price different from current price")
    public boolean isDiscountValid() {
        return !(discountAmount != null && originalPrice != null && originalPrice.compareTo(price) > 0);
    }

    // ===== GETTERS AND SETTERS =====
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public PropertySubtype getPropertySubtype() { return propertySubtype; }
    public void setPropertySubtype(PropertySubtype propertySubtype) { this.propertySubtype = propertySubtype; }

    public ListingType getListingType() { return listingType; }
    public void setListingType(ListingType listingType) { this.listingType = listingType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Boolean getPriceNegotiable() { return priceNegotiable; }
    public void setPriceNegotiable(Boolean priceNegotiable) { this.priceNegotiable = priceNegotiable; }

    public Boolean getIncludesUtilities() { return includesUtilities; }
    public void setIncludesUtilities(Boolean includesUtilities) { this.includesUtilities = includesUtilities; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getSizeInSqMt() { return sizeInSqMt; }
    public void setSizeInSqMt(BigDecimal sizeInSqMt) { this.sizeInSqMt = sizeInSqMt; }

    public BigDecimal getRoomCount() { return roomCount; }
    public void setRoomCount(BigDecimal roomCount) { this.roomCount = roomCount; }

    public BigDecimal getBathroomCount() { return bathroomCount; }
    public void setBathroomCount(BigDecimal bathroomCount) { this.bathroomCount = bathroomCount; }

    public Integer getBalconyCount() { return balconyCount; }
    public void setBalconyCount(Integer balconyCount) { this.balconyCount = balconyCount; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }

    public Integer getConstructionYear() { return constructionYear; }
    public void setConstructionYear(Integer constructionYear) { this.constructionYear = constructionYear; }

    public PropertyCondition getPropertyCondition() { return propertyCondition; }
    public void setPropertyCondition(PropertyCondition propertyCondition) { this.propertyCondition = propertyCondition; }

    public HeatingType getHeatingType() { return heatingType; }
    public void setHeatingType(HeatingType heatingType) { this.heatingType = heatingType; }

    public String getOtherHeatingTypeDescription() { return otherHeatingTypeDescription; }
    public void setOtherHeatingTypeDescription(String otherHeatingTypeDescription) { this.otherHeatingTypeDescription = otherHeatingTypeDescription; }

    public FurnitureStatus getFurnitureStatus() { return furnitureStatus; }
    public void setFurnitureStatus(FurnitureStatus furnitureStatus) { this.furnitureStatus = furnitureStatus; }

    public Boolean getHasElevator() { return hasElevator; }
    public void setHasElevator(Boolean hasElevator) { this.hasElevator = hasElevator; }

    public Boolean getHasAirConditioning() { return hasAirConditioning; }
    public void setHasAirConditioning(Boolean hasAirConditioning) { this.hasAirConditioning = hasAirConditioning; }

    public Boolean getHasInternet() { return hasInternet; }
    public void setHasInternet(Boolean hasInternet) { this.hasInternet = hasInternet; }

    public Boolean getHasCableTV() { return hasCableTV; }
    public void setHasCableTV(Boolean hasCableTV) { this.hasCableTV = hasCableTV; }

    public Boolean getHasSecurity() { return hasSecurity; }
    public void setHasSecurity(Boolean hasSecurity) { this.hasSecurity = hasSecurity; }

    public Boolean getHasParking() { return hasParking; }
    public void setHasParking(Boolean hasParking) { this.hasParking = hasParking; }

    public Integer getParkingSpaces() { return parkingSpaces; }
    public void setParkingSpaces(Integer parkingSpaces) { this.parkingSpaces = parkingSpaces; }

    public Boolean getHasGarden() { return hasGarden; }
    public void setHasGarden(Boolean hasGarden) { this.hasGarden = hasGarden; }

    public BigDecimal getGardenSizeSqMt() { return gardenSizeSqMt; }
    public void setGardenSizeSqMt(BigDecimal gardenSizeSqMt) { this.gardenSizeSqMt = gardenSizeSqMt; }

    public Boolean getHasTerrace() { return hasTerrace; }
    public void setHasTerrace(Boolean hasTerrace) { this.hasTerrace = hasTerrace; }

    public Boolean getHasBalcony() { return hasBalcony; }
    public void setHasBalcony(Boolean hasBalcony) { this.hasBalcony = hasBalcony; }

    public EnergyEfficiency getEnergyEfficiency() { return energyEfficiency; }
    public void setEnergyEfficiency(EnergyEfficiency energyEfficiency) { this.energyEfficiency = energyEfficiency; }

    public Boolean getHasWater() { return hasWater; }
    public void setHasWater(Boolean hasWater) { this.hasWater = hasWater; }

    public Boolean getHasSewage() { return hasSewage; }
    public void setHasSewage(Boolean hasSewage) { this.hasSewage = hasSewage; }

    public Boolean getHasElectricity() { return hasElectricity; }
    public void setHasElectricity(Boolean hasElectricity) { this.hasElectricity = hasElectricity; }

    public Boolean getHasGas() { return hasGas; }
    public void setHasGas(Boolean hasGas) { this.hasGas = hasGas; }

    public Boolean getHasConstructionPermit() { return hasConstructionPermit; }
    public void setHasConstructionPermit(Boolean hasConstructionPermit) { this.hasConstructionPermit = hasConstructionPermit; }

    public Boolean getHasUsePermit() { return hasUsePermit; }
    public void setHasUsePermit(Boolean hasUsePermit) { this.hasUsePermit = hasUsePermit; }

    public OwnershipType getOwnershipType() { return ownershipType; }
    public void setOwnershipType(OwnershipType ownershipType) { this.ownershipType = ownershipType; }

    public String getOtherOwnershipTypeDescription() { return otherOwnershipTypeDescription; }
    public void setOtherOwnershipTypeDescription(String otherOwnershipTypeDescription) { this.otherOwnershipTypeDescription = otherOwnershipTypeDescription; }

    public Boolean getIsRegistered() { return isRegistered; }
    public void setIsRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public Boolean getHasShowcaseWindow() { return hasShowcaseWindow; }
    public void setHasShowcaseWindow(Boolean hasShowcaseWindow) { this.hasShowcaseWindow = hasShowcaseWindow; }

    public Boolean getHasStorageRoom() { return hasStorageRoom; }
    public void setHasStorageRoom(Boolean hasStorageRoom) { this.hasStorageRoom = hasStorageRoom; }

    public Integer getEmployeeCapacity() { return employeeCapacity; }
    public void setEmployeeCapacity(Integer employeeCapacity) { this.employeeCapacity = employeeCapacity; }

    public String getLandType() { return landType; }
    public void setLandType(String landType) { this.landType = landType; }

    public Boolean getHasWaterSource() { return hasWaterSource; }
    public void setHasWaterSource(Boolean hasWaterSource) { this.hasWaterSource = hasWaterSource; }

    public Boolean getHasElectricityAccess() { return hasElectricityAccess; }
    public void setHasElectricityAccess(Boolean hasElectricityAccess) { this.hasElectricityAccess = hasElectricityAccess; }

    public Boolean getHasRoadAccess() { return hasRoadAccess; }
    public void setHasRoadAccess(Boolean hasRoadAccess) { this.hasRoadAccess = hasRoadAccess; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    public String getAgentPhone() { return agentPhone; }
    public void setAgentPhone(String agentPhone) { this.agentPhone = agentPhone; }

    public String getAgentLicense() { return agentLicense; }
    public void setAgentLicense(String agentLicense) { this.agentLicense = agentLicense; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }

    public Integer getMinimumRentPeriod() { return minimumRentPeriod; }
    public void setMinimumRentPeriod(Integer minimumRentPeriod) { this.minimumRentPeriod = minimumRentPeriod; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

	public LocalDate getDiscountEndDate() { return discountEndDate; }
	public void setDiscountEndDate(LocalDate discountEndDate) { this.discountEndDate = discountEndDate; }
}