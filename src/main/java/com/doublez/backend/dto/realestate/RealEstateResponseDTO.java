package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.FurnitureStatus;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.OwnershipType;
import com.doublez.backend.enums.property.PropertyCondition;
import com.doublez.backend.enums.property.PropertySubtype;
import com.doublez.backend.enums.property.PropertyType;

public class RealEstateResponseDTO {
    // ===== IDENTIFICATION =====
    private Long propertyId;

    // ===== BASIC INFORMATION =====
    private String title;
    private String description;

    // ===== PROPERTY CLASSIFICATION =====
    private PropertyType propertyType;
    private PropertySubtype propertySubtype; 
    private ListingType listingType;

    // ===== PRICE & FINANCIAL =====
    private BigDecimal price;
    private String currency = "RSD"; 
    private BigDecimal originalPrice; 
    private Boolean priceNegotiable = true; 
    private Boolean includesUtilities; 
    private BigDecimal depositAmount; 
    private BigDecimal pricePerSqMt;  // calculated
    private BigDecimal discountPercentage;  // calculated
    
    private BigDecimal discountAmount;
    private LocalDate discountEndDate;
    private Boolean isDiscountActive;
    private BigDecimal currentPrice; // The getCurrentPrice() calculated field

    // ===== LOCATION INFORMATION =====
    private String address;
    private String streetNumber; 
    private String neighborhood; 
    private String city;
    private String municipality;
    private String state = "Srbija";
    private String zipCode;
    private String locationDescription; 
    private BigDecimal latitude;
    private BigDecimal longitude;

    // ===== PROPERTY CHARACTERISTICS - CORE =====
    private BigDecimal sizeInSqMt;
    private BigDecimal roomCount;
    private BigDecimal bathroomCount; 
    private Integer balconyCount; 
    private Integer floor;
    private Integer totalFloors;
    private Integer constructionYear;
    private PropertyCondition propertyCondition;
    private HeatingType heatingType;
    private String otherHeatingTypeDescription; 
    private Boolean isFurnished; 
    private Boolean isSemiFurnished;
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
    private String otherOwnershipTypeDescription; 
    private Boolean isRegistered; 

    // ===== COMMERCIAL-SPECIFIC =====
    private String businessType; 
    private Boolean hasShowcaseWindow; 
    private Boolean hasStorageRoom; 
    private Integer employeeCapacity; 

    // ===== LAND-SPECIFIC =====
    private String landType; 
    private Boolean hasWaterSource; 
    private Boolean hasElectricityAccess; 
    private Boolean hasRoadAccess; 

    // ===== OWNERSHIP & AGENCY =====
    private Long ownerId;
    private String ownerEmail;
    private String ownerName; 
    private Long agencyId;
    private String agencyName;
    private String agentName;
    private String agentPhone;
    private String agentLicense;
    private String contactEmail; 
    private String preferredContactMethod; 

    // ===== AVAILABILITY =====
    private LocalDate availableFrom; 
    private Integer minimumRentPeriod; 
    private Boolean availableNow;	// calculated
    private Long daysUntilAvailable;	// calculated

    // ===== MEDIA & FEATURES =====
    private List<String> features;
    private List<String> images;

    // ===== STATUS & FEATURING =====
    private Boolean isActive;
    private Boolean isFeatured;
    private LocalDateTime featuredAt;
    private LocalDateTime featuredUntil;
    private Boolean isCurrentlyFeatured;

    // ===== TIMESTAMPS =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== AUDIT & ANALYTICS =====
    private Long viewCount; 
    private Long contactCount; 
    private Long favoriteCount; 

    // ===== CALCULATED DISPLAY FIELDS =====
    private String floorDisplay;
    private String roomCountDisplay;
    private String bathroomCountDisplay; 
    private Integer propertyAge;
    private String amenitiesSummary; 
    private String ownershipTypeDisplay; 
    private String totalSizeDisplay; 
    private Boolean hasRequiredPermits; 

    // ===== CONSTRUCTORS =====
    public RealEstateResponseDTO() {
    }

    public RealEstateResponseDTO(RealEstate realEstate) {
        if (realEstate == null) {
            throw new IllegalArgumentException("RealEstate cannot be null");
        }

        // ===== BASIC MAPPING =====
        this.propertyId = realEstate.getPropertyId();
        this.title = realEstate.getTitle();
        this.description = realEstate.getDescription();
        this.propertyType = realEstate.getPropertyType();
        this.propertySubtype = realEstate.getPropertySubtype();
        this.listingType = realEstate.getListingType();
        
        this.price = realEstate.getPrice();
        this.currency = realEstate.getCurrency();
        this.originalPrice = realEstate.getOriginalPrice();
        this.priceNegotiable = realEstate.getPriceNegotiable();
        this.includesUtilities = realEstate.getIncludesUtilities();
        this.depositAmount = realEstate.getDepositAmount();
        this.pricePerSqMt = realEstate.getPricePerSqMt();
        this.discountPercentage = realEstate.getDiscountPercentage();
        
        this.discountAmount = realEstate.getDiscountAmount();
        this.discountEndDate = realEstate.getDiscountEndDate();
        this.isDiscountActive = realEstate.isDiscountActive();
        this.currentPrice = realEstate.getCurrentPrice();

        // ===== LOCATION =====
        this.address = realEstate.getAddress();
        this.streetNumber = realEstate.getStreetNumber();
        this.neighborhood = realEstate.getNeighborhood();
        this.city = realEstate.getCity();
        this.municipality = realEstate.getMunicipality();
        this.state = realEstate.getState();
        this.zipCode = realEstate.getZipCode();
        this.locationDescription = realEstate.getLocationDescription();
        this.latitude = realEstate.getLatitude();
        this.longitude = realEstate.getLongitude();

        // ===== PROPERTY CHARACTERISTICS =====
        this.sizeInSqMt = realEstate.getSizeInSqMt();
        this.roomCount = realEstate.getRoomCount();
        this.bathroomCount = realEstate.getBathroomCount();
        this.balconyCount = realEstate.getBalconyCount();
        this.floor = realEstate.getFloor();
        this.totalFloors = realEstate.getTotalFloors();
        this.constructionYear = realEstate.getConstructionYear();
        this.propertyCondition = realEstate.getPropertyCondition();
        this.heatingType = realEstate.getHeatingType();
        this.otherHeatingTypeDescription = realEstate.getOtherHeatingTypeDescription();
        this.isFurnished = realEstate.getIsFurnished();
        this.isSemiFurnished = realEstate.getIsSemiFurnished();
        this.furnitureStatus = mapEntityToFurnitureStatus(realEstate);

        // ===== AMENITIES =====
        this.hasElevator = realEstate.getHasElevator();
        this.hasAirConditioning = realEstate.getHasAirConditioning();
        this.hasInternet = realEstate.getHasInternet();
        this.hasCableTV = realEstate.getHasCableTV();
        this.hasSecurity = realEstate.getHasSecurity();
        this.hasParking = realEstate.getHasParking();
        this.parkingSpaces = realEstate.getParkingSpaces();
        this.hasGarden = realEstate.getHasGarden();
        this.gardenSizeSqMt = realEstate.getGardenSizeSqMt();
        this.hasTerrace = realEstate.getHasTerrace();
        this.hasBalcony = realEstate.getHasBalcony();

        // ===== ENERGY & LEGAL =====
        this.energyEfficiency = realEstate.getEnergyEfficiency();
        this.hasWater = realEstate.getHasWater();
        this.hasSewage = realEstate.getHasSewage();
        this.hasElectricity = realEstate.getHasElectricity();
        this.hasGas = realEstate.getHasGas();
        this.hasConstructionPermit = realEstate.getHasConstructionPermit();
        this.hasUsePermit = realEstate.getHasUsePermit();
        this.ownershipType = realEstate.getOwnershipType();
        this.otherOwnershipTypeDescription = realEstate.getOtherOwnershipTypeDescription();
        this.isRegistered = realEstate.getIsRegistered();

        // ===== TYPE-SPECIFIC FIELDS =====
        this.businessType = realEstate.getBusinessType();
        this.hasShowcaseWindow = realEstate.getHasShowcaseWindow();
        this.hasStorageRoom = realEstate.getHasStorageRoom();
        this.employeeCapacity = realEstate.getEmployeeCapacity();
        this.landType = realEstate.getLandType();
        this.hasWaterSource = realEstate.getHasWaterSource();
        this.hasElectricityAccess = realEstate.getHasElectricityAccess();
        this.hasRoadAccess = realEstate.getHasRoadAccess();

        // ===== OWNERSHIP & CONTACT =====
        if (realEstate.getOwner() != null) {
            this.ownerId = realEstate.getOwner().getId();
            this.ownerEmail = realEstate.getOwner().getEmail();
            this.ownerName = realEstate.getEffectiveContactName();
        }
        
        if (realEstate.getAgency() != null) {
            this.agencyId = realEstate.getAgency().getId();
            this.agencyName = realEstate.getAgency().getName();
        }
        
        this.agentName = realEstate.getAgentName();
        this.agentPhone = realEstate.getAgentPhone();
        this.agentLicense = realEstate.getAgentLicense();
        this.contactEmail = realEstate.getEffectiveContactEmail();
        this.preferredContactMethod = realEstate.getPreferredContactMethod();

        // ===== AVAILABILITY =====
        this.availableFrom = realEstate.getAvailableFrom();
        this.minimumRentPeriod = realEstate.getMinimumRentPeriod();
        this.availableNow = realEstate.isAvailableNow();
        this.daysUntilAvailable = realEstate.getDaysUntilAvailable();

        // ===== MEDIA & STATUS =====
        this.features = realEstate.getFeatures();
        this.images = realEstate.getImages();
        this.isActive = realEstate.getIsActive();
        this.isFeatured = realEstate.getIsFeatured();
        this.featuredAt = realEstate.getFeaturedAt();
        this.featuredUntil = realEstate.getFeaturedUntil();
        this.isCurrentlyFeatured = realEstate.isCurrentlyFeatured();

        // ===== TIMESTAMPS & AUDIT =====
        this.createdAt = realEstate.getCreatedAt();
        this.updatedAt = realEstate.getUpdatedAt();
        this.viewCount = realEstate.getViewCount();
        this.contactCount = realEstate.getContactCount();
        this.favoriteCount = realEstate.getFavoriteCount();

        // ===== CALCULATED DISPLAY FIELDS =====
        this.floorDisplay = realEstate.getFloorDisplay();
        this.roomCountDisplay = realEstate.getRoomCountDisplay();
        this.bathroomCountDisplay = realEstate.getBathroomCountDisplay();
        this.propertyAge = realEstate.getPropertyAge();
        this.amenitiesSummary = realEstate.getAmenitiesSummary();
        this.ownershipTypeDisplay = realEstate.getOwnershipTypeDisplay();
        this.totalSizeDisplay = realEstate.getTotalSizeInSqMt() + " m²";
        this.hasRequiredPermits = realEstate.hasRequiredPermits();
    }

    // ===== GETTERS AND SETTERS =====
    
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

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

    public BigDecimal getPricePerSqMt() { return pricePerSqMt; }
    public void setPricePerSqMt(BigDecimal pricePerSqMt) { this.pricePerSqMt = pricePerSqMt; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

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

    public Boolean getIsFurnished() { return isFurnished; }
    public void setIsFurnished(Boolean isFurnished) { this.isFurnished = isFurnished; }

    public Boolean getIsSemiFurnished() { return isSemiFurnished; }
    public void setIsSemiFurnished(Boolean isSemiFurnished) { this.isSemiFurnished = isSemiFurnished; }

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

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

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

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }

    public Integer getMinimumRentPeriod() { return minimumRentPeriod; }
    public void setMinimumRentPeriod(Integer minimumRentPeriod) { this.minimumRentPeriod = minimumRentPeriod; }

    public Boolean getAvailableNow() { return availableNow; }
    public void setAvailableNow(Boolean availableNow) { this.availableNow = availableNow; }

    public Long getDaysUntilAvailable() { return daysUntilAvailable; }
    public void setDaysUntilAvailable(Long daysUntilAvailable) { this.daysUntilAvailable = daysUntilAvailable; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public LocalDateTime getFeaturedAt() { return featuredAt; }
    public void setFeaturedAt(LocalDateTime featuredAt) { this.featuredAt = featuredAt; }

    public LocalDateTime getFeaturedUntil() { return featuredUntil; }
    public void setFeaturedUntil(LocalDateTime featuredUntil) { this.featuredUntil = featuredUntil; }

    public Boolean getIsCurrentlyFeatured() { return isCurrentlyFeatured; }
    public void setIsCurrentlyFeatured(Boolean isCurrentlyFeatured) { this.isCurrentlyFeatured = isCurrentlyFeatured; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Long getContactCount() { return contactCount; }
    public void setContactCount(Long contactCount) { this.contactCount = contactCount; }

    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }

    public String getFloorDisplay() { return floorDisplay; }
    public void setFloorDisplay(String floorDisplay) { this.floorDisplay = floorDisplay; }

    public String getRoomCountDisplay() { return roomCountDisplay; }
    public void setRoomCountDisplay(String roomCountDisplay) { this.roomCountDisplay = roomCountDisplay; }

    public String getBathroomCountDisplay() { return bathroomCountDisplay; }
    public void setBathroomCountDisplay(String bathroomCountDisplay) { this.bathroomCountDisplay = bathroomCountDisplay; }

    public Integer getPropertyAge() { return propertyAge; }
    public void setPropertyAge(Integer propertyAge) { this.propertyAge = propertyAge; }

    public String getAmenitiesSummary() { return amenitiesSummary; }
    public void setAmenitiesSummary(String amenitiesSummary) { this.amenitiesSummary = amenitiesSummary; }

    public String getOwnershipTypeDisplay() { return ownershipTypeDisplay; }
    public void setOwnershipTypeDisplay(String ownershipTypeDisplay) { this.ownershipTypeDisplay = ownershipTypeDisplay; }

    public String getTotalSizeDisplay() { return totalSizeDisplay; }
    public void setTotalSizeDisplay(String totalSizeDisplay) { this.totalSizeDisplay = totalSizeDisplay; }

    public Boolean getHasRequiredPermits() { return hasRequiredPermits; }
    public void setHasRequiredPermits(Boolean hasRequiredPermits) { this.hasRequiredPermits = hasRequiredPermits; }

	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

	public LocalDate getDiscountEndDate() { return discountEndDate; }
	public void setDiscountEndDate(LocalDate discountEndDate) { this.discountEndDate = discountEndDate; }

	public Boolean getIsDiscountActive() { return isDiscountActive; }
	public void setIsDiscountActive(Boolean isDiscountActive) { this.isDiscountActive = isDiscountActive; }

	public BigDecimal getCurrentPrice() { return currentPrice; }
	public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
	
	public FurnitureStatus getFurnitureStatus() { return furnitureStatus; }
    public void setFurnitureStatus(FurnitureStatus furnitureStatus) {  this.furnitureStatus = furnitureStatus; }
	
	private FurnitureStatus mapEntityToFurnitureStatus(RealEstate realEstate) {
        Boolean isFurnished = realEstate.getIsFurnished();
        Boolean isSemiFurnished = realEstate.getIsSemiFurnished();
        
        if (Boolean.TRUE.equals(isFurnished) && Boolean.TRUE.equals(isSemiFurnished)) {
            return FurnitureStatus.PARTIALLY_FURNISHED;
        } else if (Boolean.TRUE.equals(isFurnished)) {
            return FurnitureStatus.FURNISHED;
        } else if (Boolean.TRUE.equals(isSemiFurnished)) {
            return FurnitureStatus.SEMI_FURNISHED;
        } else {
            return FurnitureStatus.UNFURNISHED;
        }
    }
}