package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.enums.HeatingType;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyCondition;
import com.doublez.backend.enums.PropertyType;

public class RealEstateResponseDTO {

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
	private String sizeInSqMt;
	private List<String> features;
	private List<String> images;
	private Long ownerId;
	private String ownerEmail;
	private LocalDate createdAt;
	private LocalDate updatedAt;

	private BigDecimal latitude;
	private BigDecimal longitude;
	private BigDecimal roomCount;
	private Integer floor;
	private Integer totalFloors;
	private Integer constructionYear;
	private String municipality;
	private HeatingType heatingType;
	private PropertyCondition propertyCondition;
	private String floorDisplay;
	private String roomCountDisplay;
	private Integer propertyAge;
	private Boolean isFeatured;

	// 🆕 AGENCY FIELDS
	private Long agencyId;
	private String agencyName;
	private String agentName;
	private String agentPhone;
	private String agentLicense;
	private Boolean isActive;

	public RealEstateResponseDTO(RealEstate realEstate) {
		if (realEstate == null) {
			throw new IllegalArgumentException("RealEstate cannot be null");
		}
		this.propertyId = realEstate.getPropertyId();
		this.title = realEstate.getTitle();
		this.description = realEstate.getDescription();
		this.propertyType = realEstate.getPropertyType();
		this.listingType = realEstate.getListingType();
		this.price = realEstate.getPrice();
		this.address = realEstate.getAddress();
		this.city = realEstate.getCity();
		this.state = realEstate.getState();
		this.zipCode = realEstate.getZipCode();
		this.sizeInSqMt = realEstate.getSizeInSqMt();
		this.features = realEstate.getFeatures();
		this.images = realEstate.getImages();
		if (realEstate.getOwner() != null) {
			this.ownerId = realEstate.getOwner().getId();
			this.ownerEmail = realEstate.getOwner().getEmail();
		} else {
			this.ownerId = null;
			this.ownerEmail = null;
		}
		this.createdAt = realEstate.getCreatedAt();
		this.updatedAt = realEstate.getUpdatedAt();

		this.latitude = realEstate.getLatitude();
		this.longitude = realEstate.getLongitude();
		this.roomCount = realEstate.getRoomCount();
		this.floor = realEstate.getFloor();
		this.totalFloors = realEstate.getTotalFloors();
		this.constructionYear = realEstate.getConstructionYear();
		this.municipality = realEstate.getMunicipality();
		this.heatingType = realEstate.getHeatingType();
		this.propertyCondition = realEstate.getPropertyCondition();
		this.floorDisplay = realEstate.getFloorDisplay();
		this.roomCountDisplay = realEstate.getRoomCountDisplay();
		this.propertyAge = realEstate.getPropertyAge();
		this.isFeatured = realEstate.getIsFeatured();

		// 🆕 INITIALIZE AGENCY FIELDS
		this.isActive = realEstate.getIsActive();
		if (realEstate.isAgencyProperty()) {
			this.agencyId = realEstate.getAgency().getId();
			this.agencyName = realEstate.getAgency().getName();
			this.agentName = realEstate.getAgentName();
			this.agentPhone = realEstate.getAgentPhone();
			this.agentLicense = realEstate.getAgentLicense();
		}
	}

	// 🆕 DEFAULT CONSTRUCTOR
	public RealEstateResponseDTO() {
	}

	// GETTERS AND SETTERS
	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}
	
	public Boolean getIsFeatured() {
		return isFeatured;
	}

	public void setIsFeatured(Boolean isFeatured) {
		this.isFeatured = isFeatured;
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

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
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

	public String getFloorDisplay() {
		return floorDisplay;
	}

	public void setFloorDisplay(String floorDisplay) {
		this.floorDisplay = floorDisplay;
	}

	public String getRoomCountDisplay() {
		return roomCountDisplay;
	}

	public void setRoomCountDisplay(String roomCountDisplay) {
		this.roomCountDisplay = roomCountDisplay;
	}

	public Integer getPropertyAge() {
		return propertyAge;
	}

	public void setPropertyAge(Integer propertyAge) {
		this.propertyAge = propertyAge;
	}

	// 🆕 AGENCY FIELD GETTERS/SETTERS
	public Long getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
}