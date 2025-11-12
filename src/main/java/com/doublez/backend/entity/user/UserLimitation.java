package com.doublez.backend.entity.user;

import java.math.BigDecimal;

import com.doublez.backend.enums.UserTier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_limitations")
public class UserLimitation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(unique = true, nullable = false)
	private UserTier tier;

	@Column(name = "max_listings", nullable = false)
	private Integer maxListings;

	@Column(name = "max_images", nullable = false)
	private Integer maxImages;

	@Column(name = "max_images_per_listing", nullable = false)
	private Integer maxImagesPerListing;

	@Column(name = "can_feature_listings", nullable = false)
	private Boolean canFeatureListings = false;

	@Column(name = "max_featured_listings")
	private Integer maxFeaturedListings = 0;

	@Column(name = "price_per_month")
	private BigDecimal pricePerMonth;

	// Default constructor
	public UserLimitation() {
	}

	// Constructor for easy creation
	public UserLimitation(UserTier tier, Integer maxListings, Integer maxImages, Integer maxImagesPerListing,
			Boolean canFeatureListings, Integer maxFeaturedListings, BigDecimal pricePerMonth) {
		this.tier = tier;
		this.maxListings = maxListings;
		this.maxImages = maxImages;
		this.maxImagesPerListing = maxImagesPerListing;
		this.canFeatureListings = canFeatureListings;
		this.maxFeaturedListings = maxFeaturedListings;
		this.pricePerMonth = pricePerMonth;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserTier getTier() {
		return tier;
	}

	public void setTier(UserTier tier) {
		this.tier = tier;
	}

	public Integer getMaxListings() {
		return maxListings;
	}

	public void setMaxListings(Integer maxListings) {
		this.maxListings = maxListings;
	}

	public Integer getMaxImages() {
		return maxImages;
	}

	public void setMaxImages(Integer maxImages) {
		this.maxImages = maxImages;
	}

	public Integer getMaxImagesPerListing() {
		return maxImagesPerListing;
	}

	public void setMaxImagesPerListing(Integer maxImagesPerListing) {
		this.maxImagesPerListing = maxImagesPerListing;
	}

	public Boolean getCanFeatureListings() {
		return canFeatureListings;
	}

	public void setCanFeatureListings(Boolean canFeatureListings) {
		this.canFeatureListings = canFeatureListings;
	}

	public Integer getMaxFeaturedListings() {
		return maxFeaturedListings;
	}

	public void setMaxFeaturedListings(Integer maxFeaturedListings) {
		this.maxFeaturedListings = maxFeaturedListings;
	}

	public BigDecimal getPricePerMonth() {
		return pricePerMonth;
	}

	public void setPricePerMonth(BigDecimal pricePerMonth) {
		this.pricePerMonth = pricePerMonth;
	}

}