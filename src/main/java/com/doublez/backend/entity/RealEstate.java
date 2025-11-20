package com.doublez.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.NumberFormat;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserProfile;
import com.doublez.backend.enums.HeatingType;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyCondition;
import com.doublez.backend.enums.PropertyType;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "real_estates")
public class RealEstate {
	
	// FIELDS
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "property_id")
	private Long propertyId;
	
	@Column(name = "title", nullable = false)
	@NotNull
	@Size(min = 1)
	private String title;
	
	@Column(name = "description", length = 1000)
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "property_type", nullable = false)
	private PropertyType propertyType;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private ListingType listingType;
	
	@NumberFormat(pattern = "#.###,00")
	@Column(name = "price", nullable = false)
	private BigDecimal price;
	
	@Column(name = "address", nullable = false)
	private String address;
	
	@Column(name = "city", nullable = false)
	private String city;
	
	@Column(name = "state", nullable = false)
	private String state;
	
	@Column(name = "zip_code", nullable = false)
	private String zipCode;
	
	@Column(name = "size_in_sqmt", nullable = false)
    private BigDecimal sizeInSqMt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "real_estate_features",
			joinColumns = @JoinColumn(name = "property_id"),
			indexes = @Index(name = "idx_features_property_id", columnList = "property_id")
	)
	@Column(name = "feature_value", length = 100)
	@Size(max = 10)
	private List<String> features = new ArrayList<>();
	
	@ElementCollection
	@CollectionTable(
			name = "real_estate_images",
			joinColumns = @JoinColumn(name = "property_id"),
			indexes = @Index(columnList = "property_id"))
	@Column(name = "image_url", length = 512, columnDefinition = "VARCHAR(512)")
	private List<String> images = new ArrayList<>();
	
	@Column(name = "created_at", nullable = false)
	private LocalDate createdAt;
	
	@Column(name = "updated_at")
	private LocalDate updatedAt;
	
	@Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
	@Column(name = "featured_until")
    private LocalDateTime featuredUntil;
    
	@Column(name = "featured_at")
    private LocalDateTime featuredAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDate.now();
		this.updatedAt = LocalDate.now();
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDate.now();
	}
	
	@Column(name = "agent_name")
	private String agentName;

	@Column(name = "agent_phone")
	private String agentPhone;

	@Column(name = "agent_license")
	private String agentLicense;
	
	// Geographic coordinates
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;
    
    // Room and floor information
    @Column(name = "room_count", precision = 3, scale = 1)
    private BigDecimal roomCount; // Supports 0.5, 1, 1.5, 2, etc.
    
    @Column(name = "floor")
    private Integer floor;
    
    @Column(name = "total_floors")
    private Integer totalFloors;
    
    // Property characteristics
    @Column(name = "construction_year")
    private Integer constructionYear;
    
    @Column(name = "municipality", length = 100)
    private String municipality;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "heating_type", length = 50)
    private HeatingType heatingType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "property_condition", length = 50)
    private PropertyCondition propertyCondition;
    
    //  AGENCY SUPPORT =====
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "agency_id")
 	private Agency agency;  // Nullable - for individual user properties
 	
 	// ACTIVE/INACTIVE STATE =====
 	@Column(name = "is_active", nullable = true)
 	private Boolean isActive = true;
	
	// METHODS
	// GETTERS AND SETTERS
 	
	public Agency getAgency() {
		return agency;
	}

	public void setAgency(Agency agency) {
		this.agency = agency;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentPhone() {
		return agentPhone;
	}

	public void setAgentPhone(String agentPhone) {
		this.agentPhone = agentPhone;
	}

	public String getAgentLicense() {
		return agentLicense;
	}

	public void setAgentLicense(String agentLicense) {
		this.agentLicense = agentLicense;
	}

	public Boolean getIsFeatured() {
		return isFeatured;
	}

	public void setIsFeatured(Boolean isFeatured) {
		this.isFeatured = isFeatured;
	}

	public LocalDateTime getFeaturedUntil() {
		return featuredUntil;
	}

	public void setFeaturedUntil(LocalDateTime featuredUntil) {
		this.featuredUntil = featuredUntil;
	}

	public LocalDateTime getFeaturedAt() {
		return featuredAt;
	}

	public void setFeaturedAt(LocalDateTime featuredAt) {
		this.featuredAt = featuredAt;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

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

	public BigDecimal  getSizeInSqMt() {
		return sizeInSqMt;
	}

	public void setSizeInSqMt(BigDecimal  sizeInSqMt) {
		this.sizeInSqMt = sizeInSqMt;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDate updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}
	
	public ListingType getListingType() {
		return listingType;
	}

	public void setListingType(ListingType listingType) {
		this.listingType = listingType;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

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
	
	
	// Helper methods
	
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
			return agentName; // Agency admin filled this specifically for the listing
		} else if (isIndividualProperty() && owner != null && owner.getUserProfile() != null) {
			// Individual user's name from profile
			UserProfile profile = owner.getUserProfile();
			if (profile.getFirstName() != null && profile.getLastName() != null) {
				return profile.getFirstName() + " " + profile.getLastName();
			} else if (profile.getFirstName() != null) {
				return profile.getFirstName();
			}
		}
		return agentName != null ? agentName : "Contact Owner";
	}
	
	/**
	 * Get effective contact phone
	 */
	public String getEffectiveContactPhone() {
		if (isAgencyProperty() && agentPhone != null) {
			return agentPhone; // Agency admin filled this
		} else if (isIndividualProperty() && owner != null && owner.getUserProfile() != null) {
			// Individual user's phone from profile
			return owner.getUserProfile().getPhone();
		}
		return agentPhone;
	}
	
	/**
	 * Get effective license number
	 */
	public String getEffectiveContactLicense() {
		if (isAgencyProperty() && agentLicense != null) {
			return agentLicense; // Agency admin filled this
		} else if (isAgencyProperty() && agency != null) {
			// Fallback to agency's license number
			return agency.getLicenseNumber();
		}
		return agentLicense;
	}
	
	/**
	 * Get display type for UI
	 */
	public String getPropertyTypeDisplay() {
		if (isAgencyProperty()) {
			return "Agency Listing";
		} else {
			return "Private Listing";
		}
	}
	
	/**
	 * Helper to check if this is a professional listing
	 */
	public boolean isProfessionalListing() {
		return isAgencyProperty();
	}
	
	public String getFloorDisplay() {
        if (floor == null || totalFloors == null) return "N/A";
        
        // Handle ground floor (0) and basement (-1, -2, etc.)
        if (floor == 0) return "Ground floor";
        if (floor < 0) return Math.abs(floor) + ". basement";
        
        return floor + ". floor of " + totalFloors;
    }
    
    public String getRoomCountDisplay() {
        if (roomCount == null) return "N/A";
        
        // Convert 0.5 to "Studio", 1.0 to "1 room", etc.
        if (roomCount.compareTo(BigDecimal.valueOf(0.5)) == 0) {
            return "Studio";
        }
        
        if (roomCount.stripTrailingZeros().scale() <= 0) {
            return roomCount.intValue() + " room" + (roomCount.intValue() > 1 ? "s" : "");
        } else {
            return roomCount + " rooms";
        }
    }
    
    public Integer getPropertyAge() {
        if (constructionYear == null) return null;
        return Year.now().getValue() - constructionYear;
    }
    
    public boolean isCurrentlyFeatured() {
		if (!isFeatured || !isActive) {  
			return false;
		}
		if (featuredUntil == null) {
			return true; // Permanent featuring
		}
		return LocalDateTime.now().isBefore(featuredUntil);
	}
    
    public void setFeatured(boolean featured, Integer featuredDays) {
        this.isFeatured = featured;
        this.featuredAt = featured ? LocalDateTime.now() : null;
        this.featuredUntil = featured && featuredDays != null ? 
            LocalDateTime.now().plusDays(featuredDays) : null;
    }
		
}
