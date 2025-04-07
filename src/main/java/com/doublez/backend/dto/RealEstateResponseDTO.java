package com.doublez.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;

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
    }

	public Long getPropertyId() {
		return propertyId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public PropertyType getPropertyType() {
		return propertyType;
	}

	public ListingType getListingType() {
		return listingType;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getSizeInSqMt() {
		return sizeInSqMt;
	}

	public List<String> getFeatures() {
		return features;
	}

	public List<String> getImages() {
		return images;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public LocalDate getUpdatedAt() {
		return updatedAt;
	}
    
    
	
}
