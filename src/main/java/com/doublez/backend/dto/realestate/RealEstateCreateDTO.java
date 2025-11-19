package com.doublez.backend.dto.realestate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.enums.HeatingType;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyCondition;
import com.doublez.backend.enums.PropertyType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RealEstateCreateDTO {
	@NotBlank
	private String title;

	private String description;

	@NotNull
	private PropertyType propertyType;

	@NotNull
	private ListingType listingType;

	@NotNull
	@Positive
	private BigDecimal price;

	@NotBlank
	private String address;

	@NotBlank
	private String city;

	@NotBlank
	private String state;

	@NotBlank
	private String zipCode;

	private String sizeInSqMt;

	@Nullable
	private Long ownerId;

	@Nullable
	private Long agencyId; // 🆕 NEW: For agency properties

	private String agentName; // 🆕 NEW: Specific agent for agency listings
	private String agentPhone; // 🆕 NEW: Agent contact
	private String agentLicense; // 🆕 NEW: Agent license

	@Size(max = 10)
	private List<String> features = new ArrayList<>();

	@DecimalMin(value = "-90.0")
	@DecimalMax(value = "90.0")
	private BigDecimal latitude;

	@DecimalMin(value = "-180.0")
	@DecimalMax(value = "180.0")
	private BigDecimal longitude;

	@DecimalMin(value = "0.5")
	@DecimalMax(value = "20.0")
	private BigDecimal roomCount;

	@Min(value = -5)
	@Max(value = 200)
	private Integer floor;

	@Min(value = 1)
	@Max(value = 200)
	private Integer totalFloors;

	@Min(value = 1500)
	@Max(value = 2030)
	private Integer constructionYear;

	private String municipality;
	private HeatingType heatingType;
	private PropertyCondition propertyCondition;

	// Getters and setters for all fields
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

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
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

	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
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
}