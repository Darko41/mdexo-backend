package com.doublez.backend.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.OwnershipType;
import com.doublez.backend.enums.property.PropertyCondition;
import com.doublez.backend.enums.property.PropertySubtype;
import com.doublez.backend.enums.property.PropertyType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "real_estates", indexes = {
    @Index(name = "idx_city", columnList = "city"),
    @Index(name = "idx_property_type", columnList = "property_type"),
    @Index(name = "idx_listing_type", columnList = "listing_type"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_city_property_type", columnList = "city,property_type"),
    @Index(name = "idx_active_featured", columnList = "is_active,is_featured"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_neighborhood", columnList = "neighborhood"),
    @Index(name = "idx_energy_efficiency", columnList = "energy_efficiency")
})
public class RealEstate {
    
    // ===== IDENTIFICATION =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Long propertyId;

    // ===== BASIC INFORMATION =====
    @Column(name = "title", nullable = false)
    @NotNull
    @Size(min = 1, max = 255)
    private String title;	// Property title for listing
    
    @Column(name = "description", length = 2000)
    private String description;	// Detailed property description

    // ===== PROPERTY CLASSIFICATION =====
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;	// Main property category (HOUSE, APARTMENT, etc.)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_subtype")
    private PropertySubtype propertySubtype;	// Detailed subcategory (APARTMENT_STUDIO, etc.)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private ListingType listingType;	// FOR_SALE or FOR_RENT

    // ===== PRICE & FINANCIAL =====
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal price;	// Current asking price
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "RSD";	// Currency code (RSD, EUR, USD)
    
    @Column(name = "original_price", precision = 15, scale = 2)
    private BigDecimal originalPrice;	// Original price before discount 
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
    
    @Column(name = "discount_end_date")
    private LocalDate discountEndDate;
    
    @Column(name = "price_negotiable")
    private Boolean priceNegotiable = true;	// Whether price is negotiable
    
    @Column(name = "includes_utilities")
    private Boolean includesUtilities;	// For rentals: whether utilities included
    
    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;	// Security deposit for rentals

    // ===== LOCATION INFORMATION =====
    @Column(name = "address", nullable = false)
    private String address;	// Street address
    
    @Column(name = "street_number", length = 20)
    private String streetNumber;	// House/building number
    
    @Column(name = "neighborhood", length = 100)
    private String neighborhood;	// Local neighborhood name (Dorćol, Novi Beograd)
    
    @Column(name = "city", nullable = false)
    private String city;	// City name
    
    @Column(name = "municipality", length = 100)
    private String municipality;	// Municipality/opština
    
    @Column(name = "state", nullable = false)
    private String state = "Srbija";	// Country/state
    
    @Column(name = "zip_code")
    private String zipCode;	// Postal code
    
    @Column(name = "location_description", length = 500)
    private String locationDescription;	// Description of location advantages
    
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;	// GPS latitude for maps TODO Implement calculations for these
    
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;	// GPS longitude for maps

    // ===== PROPERTY CHARACTERISTICS - CORE =====
    @Column(name = "size_in_sqmt", nullable = false)
    private BigDecimal sizeInSqMt;	// Total size in square meters
    
    @Column(name = "room_count", precision = 3, scale = 1)
    private BigDecimal roomCount;	// Number of rooms (supports 0.5, 1, 1.5, etc.)
    
    @Column(name = "bathroom_count", precision = 2, scale = 1)
    private BigDecimal bathroomCount;	// Number of bathrooms
    
    @Column(name = "balcony_count")
    private Integer balconyCount;	// Number of balconies
    
    @Column(name = "floor")
    private Integer floor;	// Floor number (0=ground, -1=basement)
    
    @Column(name = "total_floors")
    private Integer totalFloors;	// Total floors in building
    
    @Column(name = "construction_year")
    private Integer constructionYear;	// Year built
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_condition")
    private PropertyCondition propertyCondition;	// Property condition state
    
    @Enumerated(EnumType.STRING)
    @Column(name = "heating_type")
    private HeatingType heatingType;	// Heating system type
    
    @Column(name = "other_heating_type_description", length = 100)
    private String otherHeatingTypeDescription;	// Required when heatingType is OTHER
    
    @Column(name = "is_furnished")
    private Boolean isFurnished;
    
    @Column(name = "is_semi_furnished")
    private Boolean isSemiFurnished;

    // ===== AMENITIES & COMFORT =====
    @Column(name = "has_elevator")
    private Boolean hasElevator;	// Building has elevator
    
    @Column(name = "has_air_conditioning")
    private Boolean hasAirConditioning;	// Air conditioning installed
    
    @Column(name = "has_internet")
    private Boolean hasInternet;	// Internet connection available
    
    @Column(name = "has_cable_tv")
    private Boolean hasCableTV;	// Cable TV installed
    
    @Column(name = "has_security")
    private Boolean hasSecurity;	// Security system/alarm
    
    @Column(name = "has_parking")
    private Boolean hasParking = false;	// Parking space available
    
    @Column(name = "parking_spaces")
    private Integer parkingSpaces;	// Number of parking spaces
    
    @Column(name = "has_garden")
    private Boolean hasGarden;	// Private garden available
    
    @Column(name = "garden_size_sqmt")
    private BigDecimal gardenSizeSqMt;	// Garden size in square meters
    
    @Column(name = "has_terrace")
    private Boolean hasTerrace;	// Terrace available
    
    @Column(name = "has_balcony")
    private Boolean hasBalcony;	// Balcony available (redundant with balconyCount but useful for filtering)

    // ===== ENERGY & UTILITIES =====
    @Enumerated(EnumType.STRING)
    @Column(name = "energy_efficiency")
    private EnergyEfficiency energyEfficiency;	// Energy efficiency rating
    
    @Column(name = "has_water")
    private Boolean hasWater;	// Water connection available
    
    @Column(name = "has_sewage")
    private Boolean hasSewage;	// Sewage system available
    
    @Column(name = "has_electricity")
    private Boolean hasElectricity;	// Electricity connection available
    
    @Column(name = "has_gas")
    private Boolean hasGas;	// Gas connection available

    // ===== LEGAL & DOCUMENTATION =====
    @Column(name = "has_construction_permit")
    private Boolean hasConstructionPermit;	// Has construction permit (građevinska dozvola)
    
    @Column(name = "has_use_permit")
    private Boolean hasUsePermit;	// Has use permit (upotrebna dozvola)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type")
    private OwnershipType ownershipType;	// Type of ownership
    
    @Column(name = "other_ownership_type_description", length = 100)
    private String otherOwnershipTypeDescription;	// Required when ownershipType is OTHER
    
    @Column(name = "is_registered")
    private Boolean isRegistered;	// Property is registered in cadastre (uknjiženo)

    // ===== COMMERCIAL-SPECIFIC FIELDS =====
    @Column(name = "business_type", length = 100)
    private String businessType;	// Type of business (restaurant, store, etc.)
    
    @Column(name = "has_showcase_window")
    private Boolean hasShowcaseWindow;	// Has storefront/showcase window
    
    @Column(name = "has_storage_room")
    private Boolean hasStorageRoom;	// Has separate storage room
    
    @Column(name = "employee_capacity")
    private Integer employeeCapacity;	// Maximum employee capacity

    // ===== LAND-SPECIFIC FIELDS =====
    @Column(name = "land_type", length = 50)
    private String landType;	// Type of land (agricultural, construction, etc.)
    
    @Column(name = "has_water_source")
    private Boolean hasWaterSource;	// Has natural water source
    
    @Column(name = "has_electricity_access")
    private Boolean hasElectricityAccess;	// Electricity access available
    
    @Column(name = "has_road_access")
    private Boolean hasRoadAccess;	// Road access available

    // ===== OWNERSHIP & AGENCY =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;	// User who owns/listed the property
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;	// Agency managing the property (nullable for individual listings)

    // ===== AGENT OVERRIDES =====
    @Column(name = "agent_name")
    private String agentName;	// Specific agent name for this listing

    @Column(name = "agent_phone")
    private String agentPhone;	// Specific agent phone for this listing

    @Column(name = "agent_license")
    private String agentLicense;	// Specific agent license for this listing
    
    @Column(name = "contact_email")
    private String contactEmail;	// Specific contact email for this listing
    
    @Column(name = "preferred_contact_method", length = 20)
    private String preferredContactMethod;	// Preferred contact method (phone/email/both)

    // ===== AVAILABILITY =====
    @Column(name = "available_from")
    private LocalDate availableFrom;	// Date when property becomes available
    
    @Column(name = "minimum_rent_period")
    private Integer minimumRentPeriod;	// Minimum rental period in months

    // ===== MEDIA & FEATURES =====
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "real_estate_features",
        joinColumns = @JoinColumn(name = "property_id")
    )
    @Column(name = "feature_value", length = 100)
    @Size(max = 15)
    private List<String> features = new ArrayList<>();	// Additional features list
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "real_estate_images",
        joinColumns = @JoinColumn(name = "property_id")
    )
    @Column(name = "image_url", length = 512)
    @OrderColumn(name = "image_order")
    private List<String> images = new ArrayList<>();	// Property image URLs
    
    @Column(name = "image_count")
    private Integer imageCount = 0;	// Cached image count for performance

    // ===== STATUS & FEATURING =====
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;	// Whether listing is active
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;	// Whether listing is featured
    
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;	// Featured status expiration
    
    @Column(name = "featured_at")
    private LocalDateTime featuredAt;	// When featuring started

    // ===== TIMESTAMPS =====
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;	// When listing was created
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;	// When listing was last updated

    // ===== AUDIT FIELDS =====
    @Column(name = "view_count")
    private Long viewCount = 0L;	// Number of times viewed
    
    @Column(name = "contact_count")
    private Long contactCount = 0L;	// Number of contact attempts
    
    @Column(name = "favorite_count")
    private Long favoriteCount = 0L;	// Number of times favorited
    
    // ===== BOOSTS =====
    @Column(name = "boosted_until")
    private LocalDateTime boostedUntil;

    @Column(name = "boost_type")
    private String boostType;

    @Column(name = "urgent_badge_until")
    private LocalDateTime urgentBadgeUntil;

    @Column(name = "has_urgent_badge")
    private Boolean hasUrgentBadge = false;

    @Column(name = "highlighted_until")
    private LocalDateTime highlightedUntil;

    @Column(name = "is_highlighted")
    private Boolean isHighlighted = false;

    @Column(name = "category_featured_until")
    private LocalDateTime categoryFeaturedUntil;

    @Column(name = "is_category_featured")
    private Boolean isCategoryFeatured = false;

    @Column(name = "cross_promotion_until")
    private LocalDateTime crossPromotionUntil;
    
    // Agent who created/manages this listing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent listingAgent;  

    // CONSTRUCTORS
    public RealEstate() {
    }

    // Constructor that also accept Agent
    public RealEstate(String title, PropertyType propertyType, ListingType listingType, 
                     BigDecimal price, String address, String city, BigDecimal sizeInSqMt, 
                     User owner, Agent listingAgent) {
        this.title = title;
        this.propertyType = propertyType;
        this.listingType = listingType;
        this.price = price;
        this.address = address;
        this.city = city;
        this.sizeInSqMt = sizeInSqMt;
        this.owner = owner;
        this.listingAgent = listingAgent;
        
        // If listingAgent is provided, set agency from agent
        if (listingAgent != null) {
            this.agency = listingAgent.getAgency();
        }
    }

    // LIFECYCLE CALLBACKS
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Ensure image count is synchronized
        if (this.images != null) {
            this.imageCount = this.images.size();
        } else {
            this.imageCount = 0;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        
        // 🆕 Ensure image count is synchronized
        if (this.images != null) {
            this.imageCount = this.images.size();
        } else {
            this.imageCount = 0;
        }
    }

    // GETTERS AND SETTERS
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

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public LocalDate getDiscountEndDate() { return discountEndDate; }
    public void setDiscountEndDate(LocalDate discountEndDate) { this.discountEndDate = discountEndDate; }

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

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }

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
    public void setImages(List<String> images) {
        this.images = images;
        // Sync the image count
        this.imageCount = images != null ? images.size() : 0;
    }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public LocalDateTime getFeaturedUntil() { return featuredUntil; }
    public void setFeaturedUntil(LocalDateTime featuredUntil) { this.featuredUntil = featuredUntil; }

    public LocalDateTime getFeaturedAt() { return featuredAt; }
    public void setFeaturedAt(LocalDateTime featuredAt) { this.featuredAt = featuredAt; }

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
    
    public LocalDateTime getBoostedUntil() {
		return boostedUntil;
	}

	public void setBoostedUntil(LocalDateTime boostedUntil) {
		this.boostedUntil = boostedUntil;
	}

	public String getBoostType() {
		return boostType;
	}

	public void setBoostType(String boostType) {
		this.boostType = boostType;
	}

	public LocalDateTime getUrgentBadgeUntil() {
		return urgentBadgeUntil;
	}

	public void setUrgentBadgeUntil(LocalDateTime urgentBadgeUntil) {
		this.urgentBadgeUntil = urgentBadgeUntil;
	}

	public Boolean getHasUrgentBadge() {
		return hasUrgentBadge;
	}

	public void setHasUrgentBadge(Boolean hasUrgentBadge) {
		this.hasUrgentBadge = hasUrgentBadge;
	}

	public LocalDateTime getHighlightedUntil() {
		return highlightedUntil;
	}

	public void setHighlightedUntil(LocalDateTime highlightedUntil) {
		this.highlightedUntil = highlightedUntil;
	}

	public Boolean getIsHighlighted() {
		return isHighlighted;
	}

	public void setIsHighlighted(Boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
	}

	public LocalDateTime getCategoryFeaturedUntil() {
		return categoryFeaturedUntil;
	}

	public void setCategoryFeaturedUntil(LocalDateTime categoryFeaturedUntil) {
		this.categoryFeaturedUntil = categoryFeaturedUntil;
	}

	public Boolean getIsCategoryFeatured() {
		return isCategoryFeatured;
	}

	public void setIsCategoryFeatured(Boolean isCategoryFeatured) {
		this.isCategoryFeatured = isCategoryFeatured;
	}

	public LocalDateTime getCrossPromotionUntil() {
		return crossPromotionUntil;
	}

	public void setCrossPromotionUntil(LocalDateTime crossPromotionUntil) {
		this.crossPromotionUntil = crossPromotionUntil;
	}

	public void setDiscountPercentage(BigDecimal discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public void setImageCount(Integer imageCount) {
		this.imageCount = imageCount;
	    // Ensure consistency - if images list exists, trust it over the count field
	    if (this.images != null && imageCount != null && this.images.size() != imageCount) {
	        // If there's a mismatch, trust the actual images list
	        this.imageCount = this.images.size();
	    }
	}
	
    // ===== HELPER METHODS =====
	
	// Boost status checks
	public boolean isCurrentlyBoosted() {
	    return boostedUntil != null && LocalDateTime.now().isBefore(boostedUntil);
	}

	public boolean isUrgent() {
	    return Boolean.TRUE.equals(hasUrgentBadge) && urgentBadgeUntil != null && 
	           LocalDateTime.now().isBefore(urgentBadgeUntil);
	}

	public boolean isHighlighted() {
	    return Boolean.TRUE.equals(isHighlighted) && highlightedUntil != null && 
	           LocalDateTime.now().isBefore(highlightedUntil);
	}

	public boolean isCategoryFeatured() {
	    return Boolean.TRUE.equals(isCategoryFeatured) && categoryFeaturedUntil != null && 
	           LocalDateTime.now().isBefore(categoryFeaturedUntil);
	}

	public boolean isCrossPromoted() {
	    return crossPromotionUntil != null && LocalDateTime.now().isBefore(crossPromotionUntil);
	}

	// Auto-cleanup method
	public void checkAndResetExpiredBoosts() {
	    LocalDateTime now = LocalDateTime.now();
	    
	    if (boostedUntil != null && now.isAfter(boostedUntil)) {
	        boostedUntil = null;
	        boostType = null;
	    }
	    if (urgentBadgeUntil != null && now.isAfter(urgentBadgeUntil)) {
	        urgentBadgeUntil = null;
	        hasUrgentBadge = false;
	    }
	    if (highlightedUntil != null && now.isAfter(highlightedUntil)) {
	        highlightedUntil = null;
	        isHighlighted = false;
	    }
	    if (categoryFeaturedUntil != null && now.isAfter(categoryFeaturedUntil)) {
	        categoryFeaturedUntil = null;
	        isCategoryFeatured = false;
	    }
	    if (crossPromotionUntil != null && now.isAfter(crossPromotionUntil)) {
	        crossPromotionUntil = null;
	    }
	}

	// Get active boost types
	public List<String> getActiveBoostTypes() {
	    List<String> activeBoosts = new ArrayList<>();
	    if (isCurrentlyBoosted()) activeBoosts.add("TOP_POSITIONING");
	    if (isUrgent()) activeBoosts.add("URGENT");
	    if (isHighlighted()) activeBoosts.add("HIGHLIGHTED");
	    if (isCategoryFeatured()) activeBoosts.add("CATEGORY_FEATURED");
	    if (isCrossPromoted()) activeBoosts.add("CROSS_PROMOTION");
	    return activeBoosts;
	}

	/**
     * Returns true if this property belongs to an agency
     */
    public boolean isAgencyProperty() {
        return this.agency != null;
    }
    
    /**
     * Returns true if this property belongs to an individual user
     */
    public boolean isIndividualProperty() {
        return this.agency == null;
    }
    
    /**
     * Get the effective contact info based on property type
     */
    public String getEffectiveContactName() {
        if (isAgencyProperty() && agentName != null) {
            return agentName;
        } else if (isIndividualProperty() && owner != null) {
            // Direct access to User fields (no more UserProfile)
            if (owner.getFirstName() != null && owner.getLastName() != null) {
                return owner.getFirstName() + " " + owner.getLastName();
            } else if (owner.getFirstName() != null) {
                return owner.getFirstName();
            } else if (owner.getLastName() != null) {
                return owner.getLastName();
            }
        }
        return agentName != null ? agentName : "Contact Owner";
    }
    
    /**
     * Get complete contact information display
     */
    public String getCompleteContactInfo() {
        StringBuilder contactInfo = new StringBuilder();
        
        String name = getEffectiveContactName();
        String phone = getEffectiveContactPhone();
        String email = getEffectiveContactEmail();
        
        if (name != null && !name.trim().isEmpty()) {
            contactInfo.append(name);
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            if (contactInfo.length() > 0) contactInfo.append(" | ");
            contactInfo.append("Phone: ").append(phone);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            if (contactInfo.length() > 0) contactInfo.append(" | ");
            contactInfo.append("Email: ").append(email);
        }
        
        return contactInfo.length() > 0 ? contactInfo.toString() : "Contact information not available";
    }
    
    /**
     * Check if property has sufficient contact information
     */
    public boolean hasContactInformation() {
        return (getEffectiveContactPhone() != null && !getEffectiveContactPhone().trim().isEmpty()) ||
               (getEffectiveContactEmail() != null && !getEffectiveContactEmail().trim().isEmpty());
    }

    /**
     * Get contact method preference
     */
    public String getAvailableContactMethods() {
        String phone = getEffectiveContactPhone();
        String email = getEffectiveContactEmail();
        
        if (phone != null && email != null) {
            return "Phone or Email";
        } else if (phone != null) {
            return "Phone";
        } else if (email != null) {
            return "Email";
        } else {
            return "No contact method specified";
        }
    }
    
    /**
     * Get contact summary for quick display
     */
    public String getContactSummary() {
        String name = getEffectiveContactName();
        String methods = getAvailableContactMethods();
        
        if (name != null && methods != null) {
            return name + " - " + methods;
        } else if (name != null) {
            return name;
        } else {
            return "Contact information available";
        }
    }
    
    /**
     * Get effective contact phone
     */
    public String getEffectiveContactPhone() {
        if (isAgencyProperty() && agentPhone != null) {
            return agentPhone;
        } else if (isIndividualProperty() && owner != null) {
            // Direct access to User phone field
            return owner.getPhone();
        }
        return agentPhone;
    }
    
    
    /**
     * Get effective contact email
     */
    public String getEffectiveContactEmail() {
        if (contactEmail != null) {
            return contactEmail;
        } else if (isIndividualProperty() && owner != null) {
            return owner.getEmail();
        } else if (isAgencyProperty() && agency != null) {
            return agency.getContactEmail();
        }
        return null;
    }
    
    /**
     * Get effective license number
     */
    public String getEffectiveContactLicense() {
        if (isAgencyProperty() && agentLicense != null) {
            return agentLicense;
        } else if (isAgencyProperty() && agency != null) {
            return agency.getLicenseNumber();
        }
        return agentLicense;
    }

    /**
     * Get price per square meter
     */
    public BigDecimal getPricePerSqMt() {
        if (price == null || sizeInSqMt == null || sizeInSqMt.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return price.divide(sizeInSqMt, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate discount percentage if original price exists
     */
    public BigDecimal getDiscountPercentage() {
        if (originalPrice == null || price == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        if (originalPrice.compareTo(price) <= 0) {
            return null;
        }
        BigDecimal discount = originalPrice.subtract(price);
        return discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100))
                      .setScale(0, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if discount is currently active
     */
    public boolean isDiscountActive() {
        if (discountEndDate == null) {
            return originalPrice != null && originalPrice.compareTo(price) > 0;
        }
        return originalPrice != null && originalPrice.compareTo(price) > 0 && 
               !LocalDate.now().isAfter(discountEndDate);
    }
    
    /**
     * Get current price (discounted or original)
     */
    public BigDecimal getCurrentPrice() {
        return isDiscountActive() ? price : (originalPrice != null ? originalPrice : price);
    }
    
    /**
     * Get formatted floor display for Serbian market
     */
    public String getFloorDisplay() {
        if (floor == null) return "Nepoznato";
        
        if (floor == 0) return "Prizemlje";
        if (floor == 1) return "1. sprat";
        if (floor > 1) return floor + ". sprat";
        if (floor == -1) return "1. podrum";
        if (floor < -1) return Math.abs(floor) + ". podrum";
        
        return String.valueOf(floor);
    }
    
    /**
     * Get room count display for Serbian market
     */
    public String getRoomCountDisplay() {
        if (roomCount == null) return "Nepoznato";
        
        if (roomCount.compareTo(BigDecimal.valueOf(0.5)) == 0) {
            return "Garsonjera";
        }
        
        if (roomCount.compareTo(BigDecimal.ONE) == 0) {
            return "Jednosoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(1.5)) == 0) {
            return "Jednoiposoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(2)) == 0) {
            return "Dvosoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(2.5)) == 0) {
            return "Dvoiposoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(3)) == 0) {
            return "Trosoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(3.5)) == 0) {
            return "Troiposoban";
        }
        
        if (roomCount.compareTo(BigDecimal.valueOf(4)) == 0) {
            return "Četvorosoban";
        }
        
        return roomCount + " soba";
    }
    
    /**
     * Get bathroom count display
     */
    public String getBathroomCountDisplay() {
        if (bathroomCount == null) return "Nepoznato";
        
        if (bathroomCount.compareTo(BigDecimal.ONE) == 0) {
            return "1 kupatilo";
        }
        
        if (bathroomCount.compareTo(BigDecimal.valueOf(1.5)) == 0) {
            return "1.5 kupatila";
        }
        
        if (bathroomCount.compareTo(BigDecimal.valueOf(2)) == 0) {
            return "2 kupatila";
        }
        
        return bathroomCount + " kupatila";
    }
    
    /**
     * Calculate property age
     */
    public Integer getPropertyAge() {
        if (constructionYear == null) return null;
        return Year.now().getValue() - constructionYear;
    }
    
    /**
     * Check if property is new construction (less than 2 years old)
     */
    public boolean isNewConstruction() {
        Integer age = getPropertyAge();
        return age != null && age <= 2;
    }
    
    /**
     * Check if property is currently featured
     */
    public boolean isCurrentlyFeatured() {
        if (!isFeatured || !isActive) {  
            return false;
        }
        if (featuredUntil == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(featuredUntil);
    }
    
    /**
     * Set featured status with duration
     */
    public void setFeatured(boolean featured, Integer featuredDays) {
        this.isFeatured = featured;
        this.featuredAt = featured ? LocalDateTime.now() : null;
        this.featuredUntil = featured && featuredDays != null ? 
            LocalDateTime.now().plusDays(featuredDays) : null;
    }
    
    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }
    
    /**
     * Increment contact count
     */
    public void incrementContactCount() {
        this.contactCount = (this.contactCount == null) ? 1 : this.contactCount + 1;
    }
    
    /**
     * Increment favorite count
     */
    public void incrementFavoriteCount() {
        this.favoriteCount = (this.favoriteCount == null) ? 1 : this.favoriteCount + 1;
    }
    
    /**
     * Decrement favorite count
     */
    public void decrementFavoriteCount() {
        if (this.favoriteCount != null && this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }
    
    /**
     * Validate property subtype matches property type
     */
    public boolean isSubtypeValid() {
        if (propertySubtype == null) return true;
        return propertySubtype.getParentType() == propertyType;
    }
    
    /**
     * Check if property is available for rent/sale now
     */
    public boolean isAvailableNow() {
        if (availableFrom == null) {
            return true;
        }
        return !availableFrom.isAfter(LocalDate.now());
    }
    
    /**
     * Get days until available (for future availability)
     */
    public Long getDaysUntilAvailable() {
        if (availableFrom == null || availableFrom.isBefore(LocalDate.now())) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), availableFrom);
    }
    
    /**
     * Get total size including garden (if exists)
     */
    public BigDecimal getTotalSizeInSqMt() {
        BigDecimal total = sizeInSqMt;
        if (gardenSizeSqMt != null) {
            total = total.add(gardenSizeSqMt);
        }
        return total;
    }
    
    /**
     * Check if property has all required permits for sale
     */
    public boolean hasRequiredPermits() {
        if (propertyType == PropertyType.LAND) {
            return hasConstructionPermit != null && hasConstructionPermit;
        } else {
            return hasUsePermit != null && hasUsePermit;
        }
    }
    
    /**
     * Get list of amenities as string for search
     */
    public String getAmenitiesSummary() {
        List<String> amenities = new ArrayList<>();
        
        if (Boolean.TRUE.equals(hasElevator)) amenities.add("Lift");
        if (Boolean.TRUE.equals(hasAirConditioning)) amenities.add("Klima");
        if (Boolean.TRUE.equals(hasInternet)) amenities.add("Internet");
        if (Boolean.TRUE.equals(hasParking)) amenities.add("Parking");
        if (Boolean.TRUE.equals(hasGarden)) amenities.add("Bašta");
        if (Boolean.TRUE.equals(hasTerrace)) amenities.add("Terasa");
        if (Boolean.TRUE.equals(hasBalcony) && balconyCount != null && balconyCount > 0) {
            amenities.add(balconyCount + " balkona");
        }
        
        return String.join(", ", amenities);
    }
    
    /**
     * Get complete ownership type display name
     */
    public String getOwnershipTypeDisplay() {
        if (ownershipType == null) return "Nepoznato";
        if (ownershipType == OwnershipType.OTHER && otherOwnershipTypeDescription != null) {
            return otherOwnershipTypeDescription;
        }
        return ownershipType.getDisplayName();
    }
    
    /**
     * Get complete heating type display name
     */
    public String getHeatingTypeDisplay() {
        if (heatingType == null) return "Nepoznato";
        if (heatingType == HeatingType.OTHER && otherHeatingTypeDescription != null) {
            return otherHeatingTypeDescription;
        }
        return heatingType.getDisplayName();
    }
    
    /**
     * Get property type display name
     */
    public String getPropertyTypeDisplay() {
        if (isAgencyProperty()) {
            return "Agency Listed";
        } else if (isIndividualProperty()) {
            return "Owner Listed";
        }
        return "Unknown";
    }
    
    /**
     * Get current image count (synchronized with images list)
     */
    public Integer getImageCount() {
        if (this.images != null) {
            return this.images.size();
        }
        return imageCount != null ? imageCount : 0;
    }

    /**
     * Add image and update count
     */
    public void addImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        if (!this.images.contains(imageUrl)) {
            this.images.add(imageUrl);
            this.imageCount = this.images.size();
        }
    }

    /**
     * Remove image and update count
     */
    public boolean removeImage(String imageUrl) {
        if (this.images != null && this.images.remove(imageUrl)) {
            this.imageCount = this.images.size();
            return true;
        }
        return false;
    }

    /**
     * Clear all images and reset count
     */
    public void clearImages() {
        if (this.images != null) {
            this.images.clear();
        }
        this.imageCount = 0;
    }

    /**
     * Check if can add more images based on limit
     */
    public boolean canAddImages(int maxImagesPerListing) {
        return getImageCount() < maxImagesPerListing;
    }

    /**
     * Get remaining image slots
     */
    public int getRemainingImageSlots(int maxImagesPerListing) {
        return Math.max(0, maxImagesPerListing - getImageCount());
    }
    
    public boolean isManagedByAgent(Long agentId) {
        return listingAgent != null && listingAgent.getId().equals(agentId);
    }

    public Agent getListingAgent() { return listingAgent; }
    public void setListingAgent(Agent listingAgent) { 
        this.listingAgent = listingAgent;
        // Auto-set agency if agent is provided
        if (listingAgent != null) {
            this.agency = listingAgent.getAgency();
        }
    }
}