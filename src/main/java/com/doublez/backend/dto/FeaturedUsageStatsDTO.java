package com.doublez.backend.dto;

public class FeaturedUsageStatsDTO {
	private Long currentFeatured;
    private Integer maxFeatured;
    private boolean canFeatureMore;
    
    public FeaturedUsageStatsDTO(Long currentFeatured, Integer maxFeatured, boolean canFeatureMore) {
        this.currentFeatured = currentFeatured;
        this.maxFeatured = maxFeatured;
        this.canFeatureMore = canFeatureMore;
    }
    
    // Getters and Setters
    public Long getCurrentFeatured() {
        return currentFeatured;
    }
    
    public void setCurrentFeatured(Long currentFeatured) {
        this.currentFeatured = currentFeatured;
    }
    
    public Integer getMaxFeatured() {
        return maxFeatured;
    }
    
    public void setMaxFeatured(Integer maxFeatured) {
        this.maxFeatured = maxFeatured;
    }
    
    public boolean isCanFeatureMore() {
        return canFeatureMore;
    }
    
    public void setCanFeatureMore(boolean canFeatureMore) {
        this.canFeatureMore = canFeatureMore;
    }
}