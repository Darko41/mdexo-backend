package com.doublez.backend.dto.user;

import com.doublez.backend.entity.user.UserTier;

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
    
    public UsageStatsDTO() {
    }

    public UsageStatsDTO(long currentListings, long currentImages, long currentFeatured, UserTier tier,
            boolean isBusinessAccount, int maxListings, int maxImages, int maxFeatured) {
        this.currentListings = currentListings;
        this.currentImages = currentImages;
        this.currentFeatured = currentFeatured;

        this.tier = tier;
        this.isBusinessAccount = isBusinessAccount;

        this.maxListings = maxListings;
        this.maxImages = maxImages;
        this.maxFeatured = maxFeatured;

        // 🆕 REMOVED: this.limitations = limitations;

        this.canCreateListing = currentListings < maxListings;
        this.canUploadImage = currentImages < maxImages;
        this.canFeatureListing = currentFeatured < maxFeatured;
    }

    // --- GETTERS / SETTERS ---

    public long getCurrentListings() {
        return currentListings;
    }

    public void setCurrentListings(long currentListings) {
        this.currentListings = currentListings;
        // Update permissions when current listings change
        this.canCreateListing = currentListings < maxListings;
    }

    public long getCurrentImages() {
        return currentImages;
    }

    public void setCurrentImages(long currentImages) {
        this.currentImages = currentImages;
        // Update permissions when current images change
        this.canUploadImage = currentImages < maxImages;
    }

    public long getCurrentFeatured() {
        return currentFeatured;
    }

    public void setCurrentFeatured(long currentFeatured) {
        this.currentFeatured = currentFeatured;
        // Update permissions when current featured change
        this.canFeatureListing = currentFeatured < maxFeatured;
    }

    public int getMaxListings() {
        return maxListings;
    }

    public void setMaxListings(int maxListings) {
        this.maxListings = maxListings;
        // Update permissions when max listings change
        this.canCreateListing = currentListings < maxListings;
    }

    public int getMaxImages() {
        return maxImages;
    }

    public void setMaxImages(int maxImages) {
        this.maxImages = maxImages;
        // Update permissions when max images change
        this.canUploadImage = currentImages < maxImages;
    }

    public int getMaxFeatured() {
        return maxFeatured;
    }

    public void setMaxFeatured(int maxFeatured) {
        this.maxFeatured = maxFeatured;
        // Update permissions when max featured change
        this.canFeatureListing = currentFeatured < maxFeatured;
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

    // 🆕 HELPER METHODS
    public double getListingUsagePercentage() {
        if (maxListings == 0) return 0;
        return (double) currentListings / maxListings * 100;
    }

    public double getImageUsagePercentage() {
        if (maxImages == 0) return 0;
        return (double) currentImages / maxImages * 100;
    }

    public double getFeaturedUsagePercentage() {
        if (maxFeatured == 0) return 0;
        return (double) currentFeatured / maxFeatured * 100;
    }

    public boolean isNearListingLimit() {
        return getListingUsagePercentage() > 80;
    }

    public boolean isNearImageLimit() {
        return getImageUsagePercentage() > 80;
    }
}
