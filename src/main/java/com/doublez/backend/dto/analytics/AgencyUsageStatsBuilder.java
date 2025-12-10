package com.doublez.backend.dto.analytics;

import com.doublez.backend.entity.user.UserTier;

public class AgencyUsageStatsBuilder {
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
    
    public AgencyUsageStatsBuilder agencyId(Long agencyId) {
        this.agencyId = agencyId;
        return this;
    }
    
    public AgencyUsageStatsBuilder effectiveTier(UserTier effectiveTier) {
        this.effectiveTier = effectiveTier;
        return this;
    }
    
    public AgencyUsageStatsBuilder currentListings(long currentListings) {
        this.currentListings = currentListings;
        return this;
    }
    
    public AgencyUsageStatsBuilder maxListings(long maxListings) {
        this.maxListings = maxListings;
        return this;
    }
    
    public AgencyUsageStatsBuilder currentTotalImages(long currentTotalImages) {
        this.currentTotalImages = currentTotalImages;
        return this;
    }
    
    public AgencyUsageStatsBuilder maxTotalImages(long maxTotalImages) {
        this.maxTotalImages = maxTotalImages;
        return this;
    }
    
    public AgencyUsageStatsBuilder maxImagesPerListing(int maxImagesPerListing) {
        this.maxImagesPerListing = maxImagesPerListing;
        return this;
    }
    
    public AgencyUsageStatsBuilder listingUsagePercentage(double listingUsagePercentage) {
        this.listingUsagePercentage = listingUsagePercentage;
        return this;
    }
    
    public AgencyUsageStatsBuilder imageUsagePercentage(double imageUsagePercentage) {
        this.imageUsagePercentage = imageUsagePercentage;
        return this;
    }
    
    public AgencyUsageStatsBuilder isInTrial(boolean isInTrial) {
        this.isInTrial = isInTrial;
        return this;
    }
    
    public AgencyUsageStatsBuilder trialDaysRemaining(long trialDaysRemaining) {
        this.trialDaysRemaining = trialDaysRemaining;
        return this;
    }
    
    public AgencyUsageStats build() {
        return new AgencyUsageStats(agencyId, effectiveTier, currentListings, maxListings,
                                  currentTotalImages, maxTotalImages, maxImagesPerListing,
                                  listingUsagePercentage, imageUsagePercentage, 
                                  isInTrial, trialDaysRemaining);
    }
}