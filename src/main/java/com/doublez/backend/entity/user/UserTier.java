package com.doublez.backend.entity.user;

import java.math.BigDecimal;

public enum UserTier {
    // Agency Tiers
    AGENCY_FREE("Besplatna Agencija", new BigDecimal("0.00"), 3, 30, 20),
    AGENCY_BASIC("Osnovna Agencija", new BigDecimal("2400.00"), 20, 200, 20),
    AGENCY_PRO("Profesionalna Agencija", new BigDecimal("3500.00"), 60, 500, 20),
    AGENCY_PREMIUM("Premium Agencija", new BigDecimal("8400.00"), null, 1000, 30),
    
    // Individual User (no tiers - for consistency)
    USER_FREE("Besplatni Korisnik", null, 0, 0, 0);
    
    private final String displayName;
    private final BigDecimal monthlyPrice;
    private final Integer maxListings;
    private final Integer maxImages;
    private final Integer maxImagesPerListing;
    
    private UserTier(String displayName, BigDecimal monthlyPrice, Integer maxListings, Integer maxImages,
            Integer maxImagesPerListing) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.maxListings = maxListings;
        this.maxImages = maxImages;
        this.maxImagesPerListing = maxImagesPerListing;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public Integer getMaxListings() {
        return maxListings;
    }

    public Integer getMaxImages() {
        return maxImages;
    }

    public Integer getMaxImagesPerListing() {
        return maxImagesPerListing;
    }
    
    // ADD BUSINESS METHODS FOR LIMITATION SYSTEM
    public boolean isAgencyTier() {
        return name().startsWith("AGENCY_");
    }
    
    public boolean isUnlimitedListings() {
        return maxListings == null;
    }
    
    public int getMaxListingsSafe() {
        return maxListings != null ? maxListings : Integer.MAX_VALUE;
    }
    
    public int getMaxImagesSafe() {
        return maxImages != null ? maxImages : Integer.MAX_VALUE;
    }
}