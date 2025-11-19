package com.doublez.backend.dto.user;

import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;

public class UsageStatsDTO {

	// --- CURRENT USAGE ---
	private long currentListings;
	private long currentImages;
	private long currentFeatured;

	// --- LIMITS FROM TIER ---
	private int maxListings;
	private int maxImages;
	private int maxFeatured;

	// --- ACTION PERMISSIONS ---
	private boolean canCreateListing;
	private boolean canUploadImage;
	private boolean canFeatureListing;

	// --- ADDITIONAL CONTEXT ---
	private UserTier tier;
	private boolean isBusinessAccount;
	
	private UserLimitation limitations;
	
	 public UsageStatsDTO() {
	    }

	public UsageStatsDTO(long currentListings, long currentImages, long currentFeatured, UserTier tier,
			boolean isBusinessAccount, int maxListings, int maxImages, int maxFeatured, UserLimitation limitations) {
		this.currentListings = currentListings;
		this.currentImages = currentImages;
		this.currentFeatured = currentFeatured;

		this.tier = tier;
		this.isBusinessAccount = isBusinessAccount;

		this.maxListings = maxListings;
		this.maxImages = maxImages;
		this.maxFeatured = maxFeatured;
		this.limitations = limitations;

		this.canCreateListing = currentListings < maxListings;
		this.canUploadImage = currentImages < maxImages;
		this.canFeatureListing = currentFeatured < maxFeatured;
	}

	// --- GETTERS / SETTERS ---

	public long getCurrentListings() {
		return currentListings;
	}

	public UserLimitation getLimitations() {
		return limitations;
	}

	public void setLimitations(UserLimitation limitations) {
		this.limitations = limitations;
	}

	public void setCurrentListings(long currentListings) {
		this.currentListings = currentListings;
	}

	public long getCurrentImages() {
		return currentImages;
	}

	public void setCurrentImages(long currentImages) {
		this.currentImages = currentImages;
	}

	public long getCurrentFeatured() {
		return currentFeatured;
	}

	public void setCurrentFeatured(long currentFeatured) {
		this.currentFeatured = currentFeatured;
	}

	public int getMaxListings() {
		return maxListings;
	}

	public void setMaxListings(int maxListings) {
		this.maxListings = maxListings;
	}

	public int getMaxImages() {
		return maxImages;
	}

	public void setMaxImages(int maxImages) {
		this.maxImages = maxImages;
	}

	public int getMaxFeatured() {
		return maxFeatured;
	}

	public void setMaxFeatured(int maxFeatured) {
		this.maxFeatured = maxFeatured;
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

	public boolean isCanFeatureListing() {
		return canFeatureListing;
	}

	public void setCanFeatureListing(boolean canFeatureListing) {
		this.canFeatureListing = canFeatureListing;
	}

	public UserTier getTier() {
		return tier;
	}

	public void setTier(UserTier tier) {
		this.tier = tier;
	}

	public boolean isBusinessAccount() {
		return isBusinessAccount;
	}

	public void setBusinessAccount(boolean businessAccount) {
		isBusinessAccount = businessAccount;
	}
}
