package com.doublez.backend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;

import jakarta.annotation.Nullable;
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
    
    @Size(max = 10)
    private List<String> features = new ArrayList<>();
    
    private List<String> images = new ArrayList<>();
    
    public RealEstateCreateDTO() {
    }

    public RealEstateCreateDTO(
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
            List<String> images,
            Long ownerId) {
        
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
        this.images = images != null ? images : new ArrayList<>();
        this.ownerId = ownerId;
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
    
}
