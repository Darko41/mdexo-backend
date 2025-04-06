package com.doublez.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;

public class RealEstateRequest {
	
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
    private List<String> imageUrl;
    
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
	public List<String> getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	}
    
}
