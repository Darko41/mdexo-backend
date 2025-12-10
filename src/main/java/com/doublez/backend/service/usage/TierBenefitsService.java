package com.doublez.backend.service.usage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserRole;
import com.doublez.backend.entity.user.UserTier;

@Service
public class TierBenefitsService {

    public TierBenefitsService() {
        // No dependencies needed
    }
    
    public Map<String, Object> getTierBenefits(UserTier tier) {
        // Get benefits directly from UserTier enum
        Map<String, Object> benefits = new HashMap<>();
        benefits.put("displayName", tier.getDisplayName());
        benefits.put("monthlyPrice", tier.getMonthlyPrice());
        benefits.put("maxListings", tier.getMaxListings());
        benefits.put("maxImages", tier.getMaxImages());
        benefits.put("maxImagesPerListing", tier.getMaxImagesPerListing());

        // Add tier-specific features
        switch (tier) {
            case USER_FREE:
                benefits.put("description", "Basic browsing and favorites");
                benefits.put("canListProperties", false);
                break;
            case AGENCY_FREE:
                benefits.put("description", "Basic agency package - 3 listings, 30 images");
                benefits.put("canListProperties", true);
                benefits.put("agencyFeatures", true);
                break;
            case AGENCY_BASIC:
                benefits.put("description", "Professional agency package - 20 listings, 200 images");
                benefits.put("canListProperties", true);
                benefits.put("agencyFeatures", true);
                benefits.put("analytics", true);
                break;
            case AGENCY_PRO:
                benefits.put("description", "Advanced agency package - 60 listings, 500 images");
                benefits.put("canListProperties", true);
                benefits.put("agencyFeatures", true);
                benefits.put("analytics", true);
                benefits.put("prioritySupport", true);
                break;
            case AGENCY_PREMIUM:
                benefits.put("description", "Enterprise agency package - Unlimited listings, 1000 images");
                benefits.put("canListProperties", true);
                benefits.put("agencyFeatures", true);
                benefits.put("analytics", true);
                benefits.put("prioritySupport", true);
                benefits.put("customBranding", true);
                benefits.put("apiAccess", true);
                break;
        }

        return benefits;
    }

    public List<Map<String, Object>> getAllTierBenefits() {
        return Arrays.stream(UserTier.values())
                .map(this::getTierBenefits)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAgencyTiers() {
        return Arrays.stream(UserTier.values())
                .filter(tier -> tier.name().startsWith("AGENCY_"))
                .map(this::getTierBenefits)
                .collect(Collectors.toList());
    }

    // 🆕 FIXED: Use user's effective tier
    public boolean canCreateListing(User user, int currentListings) {
        UserTier effectiveTier = user.getEffectiveTier();
        Integer maxListings = effectiveTier.getMaxListings();
        return maxListings == null || currentListings < maxListings;
    }

    public boolean canUploadImages(User user, int currentImages, int newImageCount) {
        UserTier effectiveTier = user.getEffectiveTier();
        Integer maxImages = effectiveTier.getMaxImages();
        Integer maxPerListing = effectiveTier.getMaxImagesPerListing();
        
        boolean withinTotalLimit = maxImages == null || (currentImages + newImageCount) <= maxImages;
        boolean withinPerListingLimit = maxPerListing == null || newImageCount <= maxPerListing;
        
        return withinTotalLimit && withinPerListingLimit;
    }

    // GET EFFECTIVE TIER FOR USER directly from user
    public UserTier getEffectiveTier(User user) {
        return user.getEffectiveTier();
    }

    // 🆕 GET USER'S LIMITS
    public Map<String, Object> getUserLimits(User user) {
        UserTier effectiveTier = getEffectiveTier(user);
        return getTierBenefits(effectiveTier);
    }

    // 🆕 CHECK IF USER CAN UPGRADE
    public boolean canUpgradeToTier(User user, UserTier targetTier) {
        UserTier currentTier = getEffectiveTier(user);
        
        // Basic upgrade logic - can only upgrade to higher tiers
        return targetTier.ordinal() > currentTier.ordinal();
    }

    // 🆕 GET AVAILABLE UPGRADES FOR USER
    public List<Map<String, Object>> getAvailableUpgrades(User user) {
        UserTier currentTier = getEffectiveTier(user);
        
        return Arrays.stream(UserTier.values())
                .filter(tier -> tier.ordinal() > currentTier.ordinal())
                .filter(tier -> isCompatibleUpgrade(user, tier))
                .map(this::getTierBenefits)
                .collect(Collectors.toList());
    }

    private boolean isCompatibleUpgrade(User user, UserTier targetTier) {
        // Agency users can only upgrade to agency tiers
        if (user.isAgencyAdmin()) {
            return targetTier.name().startsWith("AGENCY_");
        }
        // Regular users can only upgrade to user tiers  
        return targetTier.name().startsWith("USER_");
    }
}