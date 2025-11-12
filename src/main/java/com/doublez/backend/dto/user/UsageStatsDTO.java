package com.doublez.backend.dto.user;

import com.doublez.backend.dto.FeaturedUsageStatsDTO;
import com.doublez.backend.entity.user.UserLimitation;

public class UsageStatsDTO {

	private Long currentListings;
	private Long currentImages;
	private UserLimitation limitations;
	private boolean canCreateListing;
	private boolean canUploadImage;
	private FeaturedUsageStatsDTO featuredStats;

	public UsageStatsDTO(Long currentListings, Long currentImages, UserLimitation limitations,
			FeaturedUsageStatsDTO featuredStats) {
		this.currentListings = currentListings;
		this.currentImages = currentImages;
		this.limitations = limitations;
		this.canCreateListing = currentListings < limitations.getMaxListings();
		this.canUploadImage = currentImages < limitations.getMaxImages();
		this.featuredStats = featuredStats;
	}

	public Long getCurrentListings() {
		return currentListings;
	}

	public void setCurrentListings(Long currentListings) {
		this.currentListings = currentListings;
	}

	public Long getCurrentImages() {
		return currentImages;
	}

	public void setCurrentImages(Long currentImages) {
		this.currentImages = currentImages;
	}

	public UserLimitation getLimitations() {
		return limitations;
	}

	public void setLimitations(UserLimitation limitations) {
		this.limitations = limitations;
	}

	public boolean isCanCreateListing() {
		return canCreateListing;
	}

	public void setCanCreateListing(boolean canCreateListing) {
		this.canCreateListing = canCreateListing;
	}

	public boolean isCanUploadImage() {
		return canUploadImage;
	}

	public void setCanUploadImage(boolean canUploadImage) {
		this.canUploadImage = canUploadImage;
	}

	public void setFeaturedStats(FeaturedUsageStatsDTO featuredStats) {
		this.featuredStats = featuredStats;
	}

	public FeaturedUsageStatsDTO getFeaturedStats() {
		return featuredStats;
	}

}