package com.doublez.backend.dto.trial;

public class TierLimitsDTO {
    private Integer maxListings;
    private Integer maxImages;
    private Integer maxImagesPerListing;
    private Integer currentListings;
    private Integer currentImages;

    // Constructors
    public TierLimitsDTO() {}

    public TierLimitsDTO(Integer maxListings, Integer maxImages, Integer maxImagesPerListing) {
        this.maxListings = maxListings;
        this.maxImages = maxImages;
        this.maxImagesPerListing = maxImagesPerListing;
    }

    // Helper methods
    public boolean canCreateListing() {
        return maxListings == null || currentListings < maxListings;
    }

    public boolean canUploadImages(Integer imageCount) {
        if (maxImages == null) return true;
        return (currentImages + imageCount) <= maxImages;
    }

    public boolean isValidImageCountPerListing(Integer imageCount) {
        return maxImagesPerListing == null || imageCount <= maxImagesPerListing;
    }

    public Integer getRemainingListings() {
        return maxListings == null ? null : maxListings - currentListings;
    }

    public Integer getRemainingImages() {
        return maxImages == null ? null : maxImages - currentImages;
    }

    // Getters and Setters
    public Integer getMaxListings() { return maxListings; }
    public void setMaxListings(Integer maxListings) { this.maxListings = maxListings; }
    public Integer getMaxImages() { return maxImages; }
    public void setMaxImages(Integer maxImages) { this.maxImages = maxImages; }
    public Integer getMaxImagesPerListing() { return maxImagesPerListing; }
    public void setMaxImagesPerListing(Integer maxImagesPerListing) { this.maxImagesPerListing = maxImagesPerListing; }
    public Integer getCurrentListings() { return currentListings; }
    public void setCurrentListings(Integer currentListings) { this.currentListings = currentListings; }
    public Integer getCurrentImages() { return currentImages; }
    public void setCurrentImages(Integer currentImages) { this.currentImages = currentImages; }
}
