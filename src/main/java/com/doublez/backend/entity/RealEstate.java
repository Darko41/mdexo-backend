package com.doublez.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.NumberFormat;

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
	
	@Column(name = "size_in_sqmt")
	private String sizeInSqMt;
	
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
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDate.now();
		this.updatedAt = LocalDate.now();
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDate.now();
	}

	public Long getPropertyId() {
		return propertyId;
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

	public String getSizeInSqMt() {
		return sizeInSqMt;
	}

	public void setSizeInSqMt(String sizeInSqMt) {
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
		
}
