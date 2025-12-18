package com.doublez.backend.entity.realestate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.geo.Point;

import com.doublez.backend.entity.Lead;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.warning.ActiveWarning;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.FeatureCategory;
import com.doublez.backend.enums.property.FurnitureStatus;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.ListingType;
import com.doublez.backend.enums.property.OwnershipType;
import com.doublez.backend.enums.property.PropertyCondition;
import com.doublez.backend.enums.property.PropertySubtype;
import com.doublez.backend.enums.property.PropertyType;
import com.doublez.backend.enums.property.WaterSourceType;
import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Index(name = "idx_energy_efficiency", columnList = "energy_efficiency"),
    @Index(name = "idx_owner", columnList = "user_id"),
    @Index(name = "idx_agency", columnList = "agency_id"),
    @Index(name = "idx_agent", columnList = "agent_id")
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
    private BigDecimal latitude;	// GPS latitude for maps
    
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;	// GPS longitude for maps
    
    // NEW: Geospatial point for fast queries (MySQL/PostGIS)
    @Column(name = "location_point", columnDefinition = "POINT")
    private Point locationPoint;

    // ===== PROPERTY CHARACTERISTICS - CORE =====
    @Column(name = "size_in_sqmt", nullable = false)
    private BigDecimal sizeInSqMt;	// Total size in square meters
    
    @Column(name = "room_count", precision = 3, scale = 1)
    private BigDecimal roomCount;	// Number of rooms (supports 0.5, 1, 1.5, etc.)
    
    @Column(name = "bathroom_count", precision = 2, scale = 1)
    private BigDecimal bathroomCount;	// Number of bathrooms
    
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
    
    // Water sources
    @ElementCollection(targetClass = WaterSourceType.class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "real_estate_water_sources",
        joinColumns = @JoinColumn(name = "property_id")
    )
    @Column(name = "water_source")
    @Enumerated(EnumType.STRING)
    private Set<WaterSourceType> waterSources = new HashSet<>();

    @Column(name = "other_water_source_description", length = 100)
    private String otherWaterSourceDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "furniture_status")
    private FurnitureStatus furnitureStatus;

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
    
    @Column(name = "garden_size_sqmt", precision = 10, scale = 2)
    private BigDecimal gardenSizeSqMt;	// Garden size in square meters
    
    @Column(name = "has_terrace")
    private Boolean hasTerrace;	// Terrace available
    
    @Column(name = "balcony_count")
    private Integer balconyCount;	// Number of balconies
    
    @Column(name = "has_balcony")
    private Boolean hasBalcony;	// Balcony available (useful for filtering)
    
    @Column(name = "terrace_size_sqmt", precision = 10, scale = 2)
    private BigDecimal terraceSizeSqMt;	// Terrace size
    
    @Column(name = "balcony_size_sqmt", precision = 10, scale = 2)
    private BigDecimal balconySizeSqMt;	// Total balcony size
    
    @Column(name = "has_loggia")
    private Boolean hasLoggia;	// Loggia available
    
    @Column(name = "loggia_size_sqmt", precision = 10, scale = 2)
    private BigDecimal loggiaSizeSqMt;	// Loggia/covered balcony size
    
    @Column(name = "basement_size_sqmt", precision = 10, scale = 2)
    private BigDecimal basementSizeSqMt;	// Basement size
    
    @Column(name = "attic_size_sqmt", precision = 10, scale = 2)
    private BigDecimal atticSizeSqMt;	// Attic/loft size
    
    @Column(name = "plot_size_sqmt", precision = 12, scale = 2)
    private BigDecimal plotSizeSqMt;	// Total plot/land size (for houses)

    // ===== ENERGY & UTILITIES =====
    @Enumerated(EnumType.STRING)
    @Column(name = "energy_efficiency")
    private EnergyEfficiency energyEfficiency;	// Energy efficiency rating
    
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
    @Size(max = 100, message = "Business type cannot exceed 100 characters")
    private String businessType;
    
    @Column(name = "has_showcase_window")
    private Boolean hasShowcaseWindow;	// Has storefront/showcase window
    
    @Column(name = "has_storage_room")
    private Boolean hasStorageRoom;	// Has separate storage room
    
    @Column(name = "employee_capacity")
    @Min(value = 0, message = "Employee capacity cannot be negative")
    @Max(value = 1000, message = "Employee capacity cannot exceed 1000")
    private Integer employeeCapacity;

    // ===== LAND-SPECIFIC FIELDS =====
    
    @Column(name = "land_type", length = 50)
    @Size(max = 50, message = "Land type cannot exceed 50 characters")
    private String landType;
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent listingAgent; // Agent who created/manages this listing

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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "real_estate_features_map",
        joinColumns = @JoinColumn(name = "property_id"),
        inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    @Size(max = 15, message = "Cannot have more than 15 features")
    private Set<PropertyFeature> features = new HashSet<>();

    
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

    // ===== TIMESTAMPS =====
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;	// When listing was created
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;	// When listing was last updated

    // ===== NEW: RELATIONSHIPS TO SEPARATED ENTITIES =====
    
    // REMOVED: viewCount, favoriteCount, contactCount (moved to PropertyMetrics)
    // REMOVED: These are now in separate PropertyMetrics entity
    
    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private PropertyMetrics propertyMetrics; // View/favorite/contact counters
    
    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private LocationMetadata locationMetadata; // Location scores, commute times, amenities
    
    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Lead> leads = new ArrayList<>(); // Leads generated for this property
    
    @OneToMany(mappedBy = "listing", fetch = FetchType.LAZY)
    private List<ActiveWarning> warnings = new ArrayList<>(); // Warnings for this listing

    // ===== NEW: CONFIGURATION & CACHE FIELDS =====
    
    @Column(name = "geocoding_accuracy", length = 50)
    private String geocodingAccuracy; // "ROOFTOP", "STREET", "CITY", "APPROXIMATE"
    
    @Column(name = "geocoded_address", length = 500)
    private String geocodedAddress; // Normalized address from geocoding service
    
    @Column(name = "data_quality_score")
    private Integer dataQualityScore; // 0-100 score of listing completeness
    
    @Column(name = "completeness_json", columnDefinition = "JSON")
    private String completenessJson; // {"basic_info": 85, "photos": 70, "location": 90}
    
    @Column(name = "warning_flags_json", columnDefinition = "JSON")
    private String warningFlagsJson; // {"no_photos": true, "incomplete_address": false}
    
    @Column(name = "last_data_quality_check")
    private LocalDateTime lastDataQualityCheck;
    
    // ===== Admin Check Fields =====
    /**
     * Timestamp of last admin review/check
     */
    private LocalDateTime lastAdminCheck;

    /**
     * ID of admin who performed the last check
     */
    private Long lastAdminCheckedBy;

    // CONSTRUCTORS
    public RealEstate() {
    }
    
    /**
     * Minimal constructor for creating new listings
     */
    public RealEstate(String title, PropertyType propertyType, ListingType listingType, 
                     BigDecimal price, String address, String city, BigDecimal sizeInSqMt, 
                     User owner) {
        this.title = title;
        this.propertyType = propertyType;
        this.listingType = listingType;
        this.price = price;
        this.address = address;
        this.city = city;
        this.sizeInSqMt = sizeInSqMt;
        this.owner = owner;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with Agent (for agency listings)
     */
    public RealEstate(String title, PropertyType propertyType, ListingType listingType, 
                     BigDecimal price, String address, String city, BigDecimal sizeInSqMt, 
                     User owner, Agent listingAgent) {
        this(title, propertyType, listingType, price, address, city, sizeInSqMt, owner);
        this.listingAgent = listingAgent;
        
        // If listingAgent is provided, set agency from agent
        if (listingAgent != null) {
            this.agency = listingAgent.getAgency();
        }
    }
    
    /**
     * Constructor with all required fields
     */
    public RealEstate(String title, PropertyType propertyType, ListingType listingType, 
                     BigDecimal price, String address, String city, BigDecimal sizeInSqMt, 
                     User owner, Agency agency, Agent listingAgent) {
        this(title, propertyType, listingType, price, address, city, sizeInSqMt, owner, listingAgent);
        this.agency = agency;
    }

    // LIFECYCLE CALLBACKS
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateImageCount();
        calculateDataQualityScore();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateImageCount();
        calculateDataQualityScore();
    }

    // ===== HELPER METHODS =====
    
    // Features helper methods
    @Transient
    public boolean hasFeature(String code) {
        return features != null &&
               features.stream().anyMatch(f -> f.getCode().equals(code));
    }

    @Transient
    public Set<PropertyFeature> getFeaturesByCategory(FeatureCategory category) {
        if (features == null) return Set.of();
        return features.stream()
                .filter(f -> f.getCategory() == category)
                .collect(Collectors.toSet());
    }

    @Transient
    public void addFeature(PropertyFeature feature) {
        if (features == null) {
            features = new HashSet<>();
        }
        features.add(feature);
    }

    @Transient
    public void removeFeature(String code) {
        if (features != null) {
            features.removeIf(f -> f.getCode().equals(code));
        }
    }

    @Transient
    public void clearFeatures() {
        if (features != null) {
            features.clear();
        }
    }

    
    // Property type helpers
    @Transient
    public boolean isLand() {
        return propertyType == PropertyType.LAND;
    }

    @Transient
    public boolean isResidential() {
        return propertyType == PropertyType.APARTMENT
            || propertyType == PropertyType.HOUSE;
    }

    @Transient
    public boolean isCommercial() {
        return propertyType == PropertyType.COMMERCIAL;
    }
    
    // FURNITURE STATUS
    @Transient
    public boolean isFurnished() {
        return furnitureStatus == FurnitureStatus.FULLY_FURNISHED;
    }

    @Transient
    public boolean isSemiFurnished() {
        return furnitureStatus == FurnitureStatus.SEMI_FURNISHED;
    }

    @Transient
    public boolean isUnfurnished() {
        return furnitureStatus == FurnitureStatus.UNFURNISHED;
    }

    @Transient
    public String getFurnitureDisplay() {
        return furnitureStatus != null
            ? furnitureStatus.getDisplayName()
            : "Nepoznato";
    }
    
    // ===== WATER SOURCES HELPERS =====

    @Transient
    public String getWaterSourcesDisplay() {
        if (waterSources == null || waterSources.isEmpty()) {
            return "Bez vode";
        }
        return waterSources.stream()
                .map(WaterSourceType::getDisplayName)
                .collect(Collectors.joining(", "));
    }

    @Transient
    public boolean hasAnyWaterSource() {
        return waterSources != null && !waterSources.isEmpty();
    }

    @Transient
    public boolean hasCityNetworkWater() {
        return waterSources != null && waterSources.contains(WaterSourceType.CITY_NETWORK);
    }

    @Transient
    public boolean hasWellWater() {
        return waterSources != null && waterSources.contains(WaterSourceType.WELL);
    }

    @Transient
    public boolean hasSpringWater() {
        return waterSources != null && waterSources.contains(WaterSourceType.SPRING);
    }

    @Transient
    public boolean hasNaturalWaterSource() {
        return waterSources != null &&
               (waterSources.contains(WaterSourceType.SPRING) || 
                waterSources.contains(WaterSourceType.WELL));
    }

    @Transient
    public boolean hasAlternativeWaterSource() {
        return waterSources != null &&
               (waterSources.contains(WaterSourceType.RAINWATER) ||
                waterSources.contains(WaterSourceType.TANK));
    }

    @Transient
    public boolean hasOtherWaterSource() {
        return waterSources != null && waterSources.contains(WaterSourceType.OTHER);
    }

    // ===== Admin Check Helper Methods =====

    /**
     * Mark this listing as checked by an admin
     * @param adminId ID of the admin performing the check
     */
    public void markAdminChecked(Long adminId) {
        this.lastAdminCheck = LocalDateTime.now();
        this.lastAdminCheckedBy = adminId;
    }

    /**
     * Check if the listing was checked within the given number of days
     * @param days number of days to consider as "recently checked"
     * @return true if listing was checked in the last `days` days
     */
    @Transient
    public boolean isRecentlyChecked(int days) {
        if (lastAdminCheck == null) return false;
        return lastAdminCheck.isAfter(LocalDateTime.now().minusDays(days));
    }

    /**
     * Get display-friendly string for last admin check
     */
    @Transient
    public String getLastAdminCheckDisplay() {
        if (lastAdminCheck == null) return "Not checked";
        return lastAdminCheck.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * Check if listing is overdue for admin review
     * @param maxDays max allowed days since last check
     * @return true if the listing was last checked more than maxDays ago or never checked
     */
    @Transient
    public boolean isAdminCheckOverdue(int maxDays) {
        if (lastAdminCheck == null) return true;
        return lastAdminCheck.isBefore(LocalDateTime.now().minusDays(maxDays));
    }

    /**
     * Get admin ID who last checked this listing
     */
    @Transient
    public Long getLastAdminCheckedBy() {
        return lastAdminCheckedBy;
    }

    /**
     * Update image count from images list
     */
    public void updateImageCount() {
        if (this.images != null) {
            this.imageCount = this.images.size();
        } else {
            this.imageCount = 0;
        }
    }
    
    /**
     * Calculate data quality score (0-100)
     */
    public void calculateDataQualityScore() {
        int score = 0;
        int maxScore = 0;
        Map<String, Integer> completeness = new HashMap<>();

        // ===== BASIC INFO (20 pts) =====
        if (title != null && !title.trim().isEmpty()) {
            score += 10;
            completeness.put("title", 10);
        }
        if (description != null && !description.trim().isEmpty()) {
            score += 10;
            completeness.put("description", 10);
        }
        maxScore += 20;

        // ===== IMAGES (20 pts) =====
        if (imageCount != null && imageCount > 0) {
            int photoScore = Math.min(imageCount * 5, 20); // max 20 points
            score += photoScore;
            completeness.put("photos", photoScore);
        }
        maxScore += 20;

        // ===== LOCATION (20 pts) =====
        if (latitude != null && longitude != null) { score += 10; completeness.put("coordinates", 10); }
        if (address != null && !address.trim().isEmpty()) { score += 5; completeness.put("address", 5); }
        if (city != null && !city.trim().isEmpty()) { score += 5; completeness.put("city", 5); }
        maxScore += 20;

        // ===== PROPERTY DETAILS (30 pts) =====
        if (sizeInSqMt != null) { score += 5; completeness.put("size", 5); }
        if (roomCount != null) { score += 5; completeness.put("rooms", 5); }
        if (propertyCondition != null) { score += 5; completeness.put("condition", 5); }
        if (constructionYear != null) { score += 5; completeness.put("year", 5); }
        if (floor != null && totalFloors != null) { score += 5; completeness.put("floor_info", 5); }
        if (heatingType != null) { score += 2; completeness.put("heating_type", 2); }
        if (heatingType == HeatingType.OTHER && otherHeatingTypeDescription != null && !otherHeatingTypeDescription.trim().isEmpty()) {
            score += 1; // extra point for OTHER description
            completeness.put("other_heating_description", 1);
        }
        maxScore += 30;

        // ===== PRICE (10 pts) =====
        if (price != null) { score += 10; completeness.put("price", 10); }
        maxScore += 10;

        // ===== UTILITIES / WATER SOURCES (10 pts) =====
        if (waterSources != null && !waterSources.isEmpty()) {
            score += 5;
            completeness.put("water_sources", 5);
        }
        if (hasElectricity != null && hasElectricity) { score += 1; completeness.put("electricity", 1); }
        if (hasSewage != null && hasSewage) { score += 1; completeness.put("sewage", 1); }
        if (hasGas != null && hasGas) { score += 1; completeness.put("gas", 1); }
        maxScore += 10;

        // ===== ENERGY / COMFORT (10 pts) =====
        if (energyEfficiency != null) { score += 5; completeness.put("energy_efficiency", 5); }
        if (hasElevator != null && hasElevator) { score += 1; completeness.put("elevator", 1); }
        if (hasAirConditioning != null && hasAirConditioning) { score += 1; completeness.put("air_conditioning", 1); }
        if (hasInternet != null && hasInternet) { score += 1; completeness.put("internet", 1); }
        if (hasParking != null && hasParking) { score += 2; completeness.put("parking", 2); }
        maxScore += 10;

        // ===== FINALIZE =====
        this.dataQualityScore = maxScore > 0 ? (score * 100) / maxScore : 0;
        this.completenessJson = JsonUtils.toJson(completeness);
        this.lastDataQualityCheck = LocalDateTime.now();
    }

    
    /**
     * Get completeness as map
     */
    @Transient
    public Map<String, Integer> getCompleteness() {
        return JsonUtils.parseStringIntegerMap(completenessJson);
    }
    
    /**
     * Get warning flags as map
     */
    @Transient
    public Map<String, Boolean> getWarningFlags() {
        return JsonUtils.parseStringBooleanMap(warningFlagsJson);
    }
    
    /**
     * Set warning flags
     */
    public void setWarningFlags(Map<String, Boolean> flags) {
        this.warningFlagsJson = JsonUtils.toJson(flags);
    }
    
    /**
     * Add warning flag
     */
    public void addWarningFlag(String flag, Boolean value) {
        Map<String, Boolean> flags = getWarningFlags();
        flags.put(flag, value);
        setWarningFlags(flags);
    }
	
    /**
     * Check if property is currently boosted
     */
    @Transient
    public boolean isCurrentlyBoosted() {
        return boostedUntil != null && LocalDateTime.now().isBefore(boostedUntil);
    }
    
    /**
     * Check if property has urgent badge
     */
    @Transient
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
     * Check if property belongs to agency
     */
    @Transient
    public boolean isAgencyProperty() {
        return this.agency != null;
    }
    
    /**
     * Check if property is individual listing. Returns true if this property belongs to an individual user
     */
    @Transient
    public boolean isIndividualProperty() {
        return this.agency == null;
    }
    
    /**
     * Get effective contact name (agent or owner)
     */
    @Transient
    public String getEffectiveContactName() {
        if (agency != null && agentName != null) {
            return agentName;
        } else if (owner != null) {
            return owner.getFirstName() + " " + owner.getLastName();
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
    @Transient
    public String getEffectiveContactPhone() {
        if (agency != null && agentPhone != null) {
            return agentPhone;
        } else if (owner != null) {
            return owner.getPhone();
        }
        return agentPhone;
    }
    
    /**
     * Get effective contact email
     */
    @Transient
    public String getEffectiveContactEmail() {
        if (contactEmail != null) {
            return contactEmail;
        } else if (owner != null) {
            return owner.getEmail();
        } else if (agency != null) {
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
    @Transient
    public BigDecimal getPricePerSqMt() {
        if (price == null || sizeInSqMt == null || sizeInSqMt.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return price.divide(sizeInSqMt, 2, RoundingMode.HALF_UP);
    }
    
    /**Internal size = Living space inside the building
		*Usable size = Internal + terraces/balconies/loggias (covered areas)
		*Property size = Everything including land/garden
    */
    
    
    /**
     * Get price per square meter based on internal size
     */
    @Transient
    public BigDecimal getPricePerInternalSqMt() {
        if (price == null || sizeInSqMt == null || sizeInSqMt.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return price.divide(sizeInSqMt, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get price per square meter based on total usable size
     */
    @Transient
    public BigDecimal getPricePerUsableSqMt() {
        if (price == null) return null;
        
        BigDecimal totalUsable = getTotalUsableSizeInSqMt();
        if (totalUsable.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return price.divide(totalUsable, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Helper method to check if property has basement
     */
    @Transient
    public Boolean hasBasement() {
        return floor != null && floor < 0;
    }
    
    /**
     * Helper method to check if property has attic
     */
    @Transient
    public Boolean hasAttic() {
        return floor != null && totalFloors != null && floor.equals(totalFloors);
    }
    
    /**
     * Get size breakdown for display
     */
    @Transient
    public Map<String, BigDecimal> getSizeBreakdown() {
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        
        if (sizeInSqMt != null) breakdown.put("Internal", sizeInSqMt);
        if (terraceSizeSqMt != null) breakdown.put("Terrace", terraceSizeSqMt);
        if (balconySizeSqMt != null) breakdown.put("Balcony", balconySizeSqMt);
        if (loggiaSizeSqMt != null) breakdown.put("Loggia", loggiaSizeSqMt);
        if (basementSizeSqMt != null && hasBasement()) breakdown.put("Basement", basementSizeSqMt);
        if (atticSizeSqMt != null && hasAttic()) breakdown.put("Attic", atticSizeSqMt);
        if (gardenSizeSqMt != null) breakdown.put("Garden", gardenSizeSqMt);
        if (plotSizeSqMt != null) breakdown.put("Plot", plotSizeSqMt);
        
        return breakdown;
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
     * Check if discount is active
     */
    @Transient
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
    @Transient
    public String getFloorDisplay() {
        if (floor == null) return "Nepoznato";
        
        if (floor == 0) return "Prizemlje";
        if (floor == 1) return "1. sprat";
        if (floor > 1) return floor + ". sprat";
        if (floor == -1) return "Suteren, 1. nivo";
        if (floor < -1) return "Suteren, " + Math.abs(floor) + ". nivo";
        
        return String.valueOf(floor);
    }
    
    /**
     * Get room count display for Serbian market
     */
    @Transient
    public String getRoomCountDisplay() {
        if (roomCount == null) return "Nepoznato";
        
        if (roomCount.compareTo(BigDecimal.valueOf(0.5)) == 0) return "Garsonjera";
        if (roomCount.compareTo(BigDecimal.ONE) == 0) return "Jednosoban";
        if (roomCount.compareTo(BigDecimal.valueOf(1.5)) == 0) return "Jednoiposoban";
        if (roomCount.compareTo(BigDecimal.valueOf(2)) == 0) return "Dvosoban";
        if (roomCount.compareTo(BigDecimal.valueOf(2.5)) == 0) return "Dvoiposoban";
        if (roomCount.compareTo(BigDecimal.valueOf(3)) == 0) return "Trosoban";
        if (roomCount.compareTo(BigDecimal.valueOf(3.5)) == 0) return "Troiposoban";
        if (roomCount.compareTo(BigDecimal.valueOf(4)) == 0) return "Četvorosoban";
        if (roomCount.compareTo(BigDecimal.valueOf(5)) == 0) return "Petosoban";
        if (roomCount.compareTo(BigDecimal.valueOf(5)) == 0) return "Preko 5 soba";
        
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
     * Get property age in years
     */
    @Transient
    public Integer getPropertyAge() {
        if (constructionYear == null) return null;
        return Year.now().getValue() - constructionYear;
    }
    
    /**
     * Check if property is new construction (< 2 years)
     */
    @Transient
    public boolean isNewConstruction() {
        Integer age = getPropertyAge();
        return age != null && age <= 2;
    }
    
    /**
     * Check if property is currently featured
     */
    @Transient
    public boolean isCurrentlyFeatured() {
        if (!Boolean.TRUE.equals(isFeatured) || !Boolean.TRUE.equals(isActive)) {
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
     * Validate property subtype matches property type
     */
    public boolean isSubtypeValid() {
        if (propertySubtype == null) return true;
        return propertySubtype.getParentType() == propertyType;
    }
    
    /**
     * Check if property is available for rent/sale now
     */
    @Transient
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
     * Get total usable size including all areas (internal + terraces + balconies + loggias)
     */
    @Transient
    public BigDecimal getTotalUsableSizeInSqMt() {
        BigDecimal total = sizeInSqMt != null ? sizeInSqMt : BigDecimal.ZERO;
        
        if (terraceSizeSqMt != null) total = total.add(terraceSizeSqMt);
        if (balconySizeSqMt != null) total = total.add(balconySizeSqMt);
        if (loggiaSizeSqMt != null) total = total.add(loggiaSizeSqMt);
        if (basementSizeSqMt != null && Boolean.TRUE.equals(hasBasement())) total = total.add(basementSizeSqMt);
        if (atticSizeSqMt != null && Boolean.TRUE.equals(hasAttic())) total = total.add(atticSizeSqMt);
        
        return total;
    }
    
    /**
     * Get total property size including everything (usable + garden + plot)
     */
    @Transient
    public BigDecimal getTotalPropertySizeInSqMt() {
        BigDecimal total = getTotalUsableSizeInSqMt();
        
        if (gardenSizeSqMt != null) total = total.add(gardenSizeSqMt);
        if (plotSizeSqMt != null) total = total.add(plotSizeSqMt);
        
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
    
    @Transient
    public boolean hasHeating() {
        return heatingType != null && heatingType != HeatingType.NONE;
    }

    @Transient
    public boolean isHeatingOther() {
        return heatingType == HeatingType.OTHER;
    }

    @Transient
    public boolean requiresHeatingDescription() {
        return heatingType == HeatingType.OTHER;
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
            updateImageCount();
        }
    }

    /**
     * Remove image and update count
     */
    public boolean removeImage(String imageUrl) {
        if (this.images != null && this.images.remove(imageUrl)) {
            updateImageCount();
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
    
    /**
     * Set location coordinates and update point
     */
    public void setLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (latitude != null && longitude != null) {
            // Create Point for spatial queries (requires MySQL/PostGIS)
            this.locationPoint = createPoint(latitude.doubleValue(), longitude.doubleValue());
        }
    }
    
    /**
     * Create Point object from coordinates
     */
    private Point createPoint(double lat, double lng) {
        // TODO: Implementation depends on your JPA provider
        // For Hibernate Spatial: return new Point(lng, lat);
        // For regular JPA: return null or implement custom type
        return null; // You'll need to implement based on your setup
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

    public Boolean getHasShowcaseWindow() { return hasShowcaseWindow; }
    public void setHasShowcaseWindow(Boolean hasShowcaseWindow) { this.hasShowcaseWindow = hasShowcaseWindow; }

    public Boolean getHasStorageRoom() { return hasStorageRoom; }
    public void setHasStorageRoom(Boolean hasStorageRoom) { this.hasStorageRoom = hasStorageRoom; }

    public Integer getEmployeeCapacity() { return employeeCapacity; }
    public void setEmployeeCapacity(Integer employeeCapacity) { this.employeeCapacity = employeeCapacity; }

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

	public Point getLocationPoint() {
		return locationPoint;
	}

	public void setLocationPoint(Point locationPoint) {
		this.locationPoint = locationPoint;
	}

	public PropertyMetrics getPropertyMetrics() {
		return propertyMetrics;
	}

	public void setPropertyMetrics(PropertyMetrics propertyMetrics) {
		this.propertyMetrics = propertyMetrics;
	}

	public LocationMetadata getLocationMetadata() {
		return locationMetadata;
	}

	public void setLocationMetadata(LocationMetadata locationMetadata) {
		this.locationMetadata = locationMetadata;
	}

	public List<Lead> getLeads() {
		return leads;
	}

	public void setLeads(List<Lead> leads) {
		this.leads = leads;
	}

	public List<ActiveWarning> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<ActiveWarning> warnings) {
		this.warnings = warnings;
	}

	public String getGeocodingAccuracy() {
		return geocodingAccuracy;
	}

	public void setGeocodingAccuracy(String geocodingAccuracy) {
		this.geocodingAccuracy = geocodingAccuracy;
	}

	public String getGeocodedAddress() {
		return geocodedAddress;
	}

	public void setGeocodedAddress(String geocodedAddress) {
		this.geocodedAddress = geocodedAddress;
	}

	public Integer getDataQualityScore() {
		return dataQualityScore;
	}

	public void setDataQualityScore(Integer dataQualityScore) {
		this.dataQualityScore = dataQualityScore;
	}

	public String getCompletenessJson() {
		return completenessJson;
	}

	public void setCompletenessJson(String completenessJson) {
		this.completenessJson = completenessJson;
	}

	public String getWarningFlagsJson() {
		return warningFlagsJson;
	}

	public void setWarningFlagsJson(String warningFlagsJson) {
		this.warningFlagsJson = warningFlagsJson;
	}

	public LocalDateTime getLastDataQualityCheck() {
		return lastDataQualityCheck;
	}

	public void setLastDataQualityCheck(LocalDateTime lastDataQualityCheck) {
		this.lastDataQualityCheck = lastDataQualityCheck;
	}

	public BigDecimal getTerraceSizeSqMt() {
		return terraceSizeSqMt;
	}

	public void setTerraceSizeSqMt(BigDecimal terraceSizeSqMt) {
		this.terraceSizeSqMt = terraceSizeSqMt;
	}

	public BigDecimal getBalconySizeSqMt() {
		return balconySizeSqMt;
	}

	public void setBalconySizeSqMt(BigDecimal balconySizeSqMt) {
		this.balconySizeSqMt = balconySizeSqMt;
	}

	public BigDecimal getLoggiaSizeSqMt() {
		return loggiaSizeSqMt;
	}

	public void setLoggiaSizeSqMt(BigDecimal loggiaSizeSqMt) {
		this.loggiaSizeSqMt = loggiaSizeSqMt;
	}

	public BigDecimal getBasementSizeSqMt() {
		return basementSizeSqMt;
	}

	public void setBasementSizeSqMt(BigDecimal basementSizeSqMt) {
		this.basementSizeSqMt = basementSizeSqMt;
	}

	public BigDecimal getAtticSizeSqMt() {
		return atticSizeSqMt;
	}

	public void setAtticSizeSqMt(BigDecimal atticSizeSqMt) {
		this.atticSizeSqMt = atticSizeSqMt;
	}

	public BigDecimal getPlotSizeSqMt() {
		return plotSizeSqMt;
	}

	public void setPlotSizeSqMt(BigDecimal plotSizeSqMt) {
		this.plotSizeSqMt = plotSizeSqMt;
	}

	public FurnitureStatus getFurnitureStatus() {
		return furnitureStatus;
	}

	public void setFurnitureStatus(FurnitureStatus furnitureStatus) {
		this.furnitureStatus = furnitureStatus;
	}

	public Set<WaterSourceType> getWaterSources() {
		return waterSources;
	}

	public void setWaterSources(Set<WaterSourceType> waterSources) {
		this.waterSources = waterSources;
	}

	public String getOtherWaterSourceDescription() {
		return otherWaterSourceDescription;
	}

	public void setOtherWaterSourceDescription(String otherWaterSourceDescription) {
		this.otherWaterSourceDescription = otherWaterSourceDescription;
	}

	public LocalDateTime getLastAdminCheck() {
		return lastAdminCheck;
	}

	public void setLastAdminCheck(LocalDateTime lastAdminCheck) {
		this.lastAdminCheck = lastAdminCheck;
	}

	public void setLastAdminCheckedBy(Long lastAdminCheckedBy) {
		this.lastAdminCheckedBy = lastAdminCheckedBy;
	}

	public Boolean getHasLoggia() {
		return hasLoggia;
	}

	public void setHasLoggia(Boolean hasLoggia) {
		this.hasLoggia = hasLoggia;
	}

	public Set<PropertyFeature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<PropertyFeature> features) {
		this.features = features;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getLandType() {
		return landType;
	}

	public void setLandType(String landType) {
		this.landType = landType;
	}
}