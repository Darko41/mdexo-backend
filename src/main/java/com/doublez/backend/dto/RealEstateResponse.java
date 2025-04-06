package com.doublez.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;

public class RealEstateResponse {
	
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
	
	public RealEstateResponse() {
		super();
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
	public Long getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
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
	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	
}
