package com.doublez.backend.dto.analytics;

import com.doublez.backend.entity.user.UserTier;

public class AgencyUsageStats {
	private Long agencyId;
	private UserTier effectiveTier;
	private long currentListings;
	private long maxListings;
	private long currentTotalImages;
	private long maxTotalImages;
	private int maxImagesPerListing;
	private double listingUsagePercentage;
	private double imageUsagePercentage;
	private boolean isInTrial;
	private long trialDaysRemaining;

	// Constructors
	public AgencyUsageStats() {
	}

	public AgencyUsageStats(Long agencyId, UserTier effectiveTier, long currentListings, long maxListings,
			long currentTotalImages, long maxTotalImages, int maxImagesPerListing, double listingUsagePercentage,
			double imageUsagePercentage, boolean isInTrial, long trialDaysRemaining) {
		this.agencyId = agencyId;
		this.effectiveTier = effectiveTier;
		this.currentListings = currentListings;
		this.maxListings = maxListings;
		this.currentTotalImages = currentTotalImages;
		this.maxTotalImages = maxTotalImages;
		this.maxImagesPerListing = maxImagesPerListing;
		this.listingUsagePercentage = listingUsagePercentage;
		this.imageUsagePercentage = imageUsagePercentage;
		this.isInTrial = isInTrial;
		this.trialDaysRemaining = trialDaysRemaining;
	}

	// Builder pattern manually
	public static AgencyUsageStatsBuilder builder() {
		return new AgencyUsageStatsBuilder();
	}

	public boolean isNearLimit() {
		return listingUsagePercentage > 80 || imageUsagePercentage > 80;
	}

	public boolean isAtLimit() {
		return listingUsagePercentage >= 100 || imageUsagePercentage >= 100;
	}

	// Getters and Setters
	public Long getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(Long agencyId) {
		this.agencyId = agencyId;
	}

	public UserTier getEffectiveTier() {
		return effectiveTier;
	}

	public void setEffectiveTier(UserTier effectiveTier) {
		this.effectiveTier = effectiveTier;
	}

	public long getCurrentListings() {
		return currentListings;
	}

	public void setCurrentListings(long currentListings) {
		this.currentListings = currentListings;
	}

	public long getMaxListings() {
		return maxListings;
	}

	public void setMaxListings(long maxListings) {
		this.maxListings = maxListings;
	}

	public long getCurrentTotalImages() {
		return currentTotalImages;
	}

	public void setCurrentTotalImages(long currentTotalImages) {
		this.currentTotalImages = currentTotalImages;
	}

	public long getMaxTotalImages() {
		return maxTotalImages;
	}

	public void setMaxTotalImages(long maxTotalImages) {
		this.maxTotalImages = maxTotalImages;
	}

	public int getMaxImagesPerListing() {
		return maxImagesPerListing;
	}

	public void setMaxImagesPerListing(int maxImagesPerListing) {
		this.maxImagesPerListing = maxImagesPerListing;
	}

	public double getListingUsagePercentage() {
		return listingUsagePercentage;
	}

	public void setListingUsagePercentage(double listingUsagePercentage) {
		this.listingUsagePercentage = listingUsagePercentage;
	}

	public double getImageUsagePercentage() {
		return imageUsagePercentage;
	}

	public void setImageUsagePercentage(double imageUsagePercentage) {
		this.imageUsagePercentage = imageUsagePercentage;
	}

	public boolean isInTrial() {
		return isInTrial;
	}

	public void setInTrial(boolean inTrial) {
		isInTrial = inTrial;
	}

	public long getTrialDaysRemaining() {
		return trialDaysRemaining;
	}

	public void setTrialDaysRemaining(long trialDaysRemaining) {
		this.trialDaysRemaining = trialDaysRemaining;
	}
}