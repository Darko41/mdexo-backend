package com.doublez.backend.service.usage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.repository.UserLimitationRepository;

@Service
public class TierBenefitsService {

    private final UserLimitationRepository limitationRepository;

    public TierBenefitsService(UserLimitationRepository limitationRepository) {
        this.limitationRepository = limitationRepository;
    }
    
    public Map<String, Object> getTierBenefits(UserTier tier) {
        UserLimitation limits = limitationRepository.findByTier(tier)
                .orElseThrow(() -> new IllegalArgumentException("Limits not found for tier: " + tier));

        // Base benefits from database
        Map<String, Object> benefits = new HashMap<>();
        benefits.put("maxListings", limits.getMaxListings());
        benefits.put("maxImages", limits.getMaxImages());
        benefits.put("maxImagesPerListing", limits.getMaxImagesPerListing());
        benefits.put("canFeatureListings", limits.getCanFeatureListings());
        benefits.put("maxFeaturedListings", limits.getMaxFeaturedListings());
        benefits.put("pricePerMonth", limits.getPricePerMonth());

        // Add tier-specific features
        switch (tier) {
            case FREE_USER:
                benefits.put("description", "Basic listing capabilities for individual users");
                break;
            case BASIC_USER:
                benefits.put("description", "Enhanced limits with featured listing capability");
                break;
            case PREMIUM_USER:
                benefits.put("description", "Professional features for serious individual sellers");
                break;
            case AGENCY_BASIC:
                benefits.put("description", "Professional agency package for small to medium agencies");
                benefits.put("agentManagement", true);
                benefits.put("analytics", true);
                break;
            case AGENCY_PREMIUM:
                benefits.put("description", "Enterprise package for large agencies");
                benefits.put("agentManagement", true);
                benefits.put("analytics", true);
                benefits.put("prioritySupport", true);
                benefits.put("customBranding", true);
                break;
            case FREE_INVESTOR:
                benefits.put("description", "Basic investment property listing");
                benefits.put("investmentAnalytics", false);
                break;
            case BASIC_INVESTOR:
                benefits.put("description", "Professional investor tools with analytics");
                benefits.put("investmentAnalytics", true);
                benefits.put("portfolioTracking", true);
                break;
            case PREMIUM_INVESTOR:
                benefits.put("description", "Advanced investment platform with full analytics");
                benefits.put("investmentAnalytics", true);
                benefits.put("portfolioTracking", true);
                benefits.put("marketReports", true);
                benefits.put("dealAlerts", true);
                break;
        }

        return benefits;
    }

    public List<Map<String, Object>> getAllTierBenefits() {
        return Arrays.stream(UserTier.values())
                .map(this::getTierBenefits)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getIndividualTiers() {
        return List.of(
            getTierBenefits(UserTier.FREE_USER),
            getTierBenefits(UserTier.BASIC_USER),
            getTierBenefits(UserTier.PREMIUM_USER)
        );
    }

    public List<Map<String, Object>> getAgencyTiers() {
        return List.of(
            getTierBenefits(UserTier.AGENCY_BASIC),
            getTierBenefits(UserTier.AGENCY_PREMIUM)
        );
    }

    public List<Map<String, Object>> getInvestorTiers() {
        return List.of(
            getTierBenefits(UserTier.FREE_INVESTOR),
            getTierBenefits(UserTier.BASIC_INVESTOR),
            getTierBenefits(UserTier.PREMIUM_INVESTOR)
        );
    }
}
