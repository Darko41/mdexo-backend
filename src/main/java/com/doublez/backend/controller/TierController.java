package com.doublez.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.profile.InvestorProfile;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.service.usage.TierBenefitsService;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/tiers")
public class TierController {

    private final TierBenefitsService tierBenefitsService;
    private final TrialService trialService;
    private final UserService userService;
    
    private static final Logger logger = LoggerFactory.getLogger(TierController.class);

    public TierController(TierBenefitsService tierBenefitsService, TrialService trialService, UserService userService) {
        this.tierBenefitsService = tierBenefitsService;
        this.trialService = trialService;
        this.userService = userService;
    }

    /**
     * GET ALL TIER BENEFITS
     * Public endpoint - anyone can view tier benefits
     */
    @GetMapping("/benefits")
    public ResponseEntity<?> getAllTierBenefits() {
        try {
            logger.info("📊 Fetching all tier benefits");
            
            List<Map<String, Object>> benefits = tierBenefitsService.getAllTierBenefits();
            
            logger.info("✅ Found benefits for {} tiers", benefits.size());
            return ResponseEntity.ok(benefits);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch tier benefits", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve tier benefits"));
        }
    }

    /**
     * GET INDIVIDUAL USER TIERS
     * Public endpoint - anyone can view individual user tiers
     */
    @GetMapping("/benefits/individual")
    public ResponseEntity<?> getIndividualTiers() {
        try {
            logger.info("👤 Fetching individual user tiers");
            
            List<Map<String, Object>> tiers = tierBenefitsService.getIndividualTiers();
            
            logger.info("✅ Found {} individual tiers", tiers.size());
            return ResponseEntity.ok(tiers);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch individual tiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve individual tiers"));
        }
    }

    /**
     * GET AGENCY TIERS
     * Public endpoint - anyone can view agency tiers
     */
    @GetMapping("/benefits/agency")
    public ResponseEntity<?> getAgencyTiers() {
        try {
            logger.info("🏢 Fetching agency tiers");
            
            List<Map<String, Object>> tiers = tierBenefitsService.getAgencyTiers();
            
            logger.info("✅ Found {} agency tiers", tiers.size());
            return ResponseEntity.ok(tiers);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch agency tiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve agency tiers"));
        }
    }

    /**
     * GET INVESTOR TIERS
     * Public endpoint - anyone can view investor tiers
     */
    @GetMapping("/benefits/investor")
    public ResponseEntity<?> getInvestorTiers() {
        try {
            logger.info("💰 Fetching investor tiers");
            
            List<Map<String, Object>> tiers = tierBenefitsService.getInvestorTiers();
            
            logger.info("✅ Found {} investor tiers", tiers.size());
            return ResponseEntity.ok(tiers);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch investor tiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve investor tiers"));
        }
    }

    /**
     * GET CURRENT USER'S TIER INFORMATION
     * Authenticated endpoint - returns current user's tier, benefits, and trial status
     */
    @GetMapping("/my-tier")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyTierBenefits() {
        try {
            // ✅ FIX: Use getAuthenticatedUser() instead of getCurrentUserId()
            User user = userService.getAuthenticatedUser();
            logger.info("🎯 Fetching tier information for user: {} (Tier: {})", user.getEmail(), user.getTier());
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentTier", user.getTier());
            response.put("tierName", user.getTier().name());
            response.put("benefits", tierBenefitsService.getTierBenefits(user.getTier()));
            response.put("inTrial", trialService.isInTrial(user));
            response.put("trialDaysRemaining", trialService.getTrialDaysRemaining(user));
            response.put("trialActive", trialService.isInTrial(user));
            
            // Add trial end date if in trial
            if (trialService.isInTrial(user)) {
                response.put("trialEndDate", user.getTrialEndDate());
                response.put("trialStartDate", user.getTrialStartDate());
            }
            
            // Add agency information if user is an agency admin
            if (user.isAgencyAdmin()) {
                try {
                    // ✅ FIX: Use AgencyService to get agency info properly
                    // You might need to inject AgencyService for this
                    // For now, using the existing method if available
                    if (!user.getOwnedAgencies().isEmpty()) {
                        Agency agency = user.getOwnedAgencies().get(0);
                        response.put("agency", Map.of(
                            "id", agency.getId(), 
                            "name", agency.getName(), 
                            "isActive", agency.getIsActive()
                        ));
                        logger.info("🏢 Agency info added for user: {}", user.getEmail());
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Could not fetch agency info for user: {}", user.getEmail(), e);
                }
            }
            
            // Add investor information if user is an investor
            if (user.isInvestor()) {
                response.put("investor", Map.of(
                    "hasProfile", user.getInvestorProfile() != null,
                    "profileComplete", user.getInvestorProfile() != null && 
                                      isInvestorProfileComplete(user.getInvestorProfile())
                ));
            }
            
            // Add usage statistics if available
            try {
                // You might want to add usage stats here from your authorization service
                response.put("usage", Map.of(
                    "propertiesCreated", getCurrentUserPropertyCount(user.getId()),
                    "imagesUploaded", getCurrentUserImageCount(user.getId())
                ));
            } catch (Exception e) {
                logger.debug("Usage statistics not available for user: {}", user.getEmail());
            }
            
            logger.info("✅ Tier information retrieved successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve tier information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve tier information: " + e.getMessage()));
        }
    }

    /**
     * GET TIER BENEFITS FOR SPECIFIC TIER
     * Public endpoint - get benefits for any specific tier
     */
    @GetMapping("/benefits/{tier}")
    public ResponseEntity<?> getTierBenefits(@PathVariable UserTier tier) {
        try {
            logger.info("🔍 Fetching benefits for tier: {}", tier);
            
            Map<String, Object> benefits = tierBenefitsService.getTierBenefits(tier);
            
            logger.info("✅ Benefits retrieved for tier: {}", tier);
            return ResponseEntity.ok(benefits);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch benefits for tier: {}", tier, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve benefits for tier: " + tier));
        }
    }

    /**
     * COMPARE TIERS
     * Public endpoint - compare benefits between multiple tiers
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compareTiers(@RequestParam List<UserTier> tiers) {
        try {
            logger.info("⚖️ Comparing tiers: {}", tiers);
            
            if (tiers.isEmpty() || tiers.size() > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Please provide 1-5 tiers to compare"));
            }
            
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("tiers", tiers);
            
            List<Map<String, Object>> tierBenefits = new ArrayList<>();
            for (UserTier tier : tiers) {
                Map<String, Object> benefits = tierBenefitsService.getTierBenefits(tier);
                Map<String, Object> tierInfo = new HashMap<>();
                tierInfo.put("tier", tier);
                tierInfo.put("benefits", benefits);
                tierBenefits.add(tierInfo);
            }
            
            comparison.put("comparison", tierBenefits);
            
            logger.info("✅ Tier comparison completed for {} tiers", tiers.size());
            return ResponseEntity.ok(comparison);
            
        } catch (Exception e) {
            logger.error("❌ Failed to compare tiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to compare tiers"));
        }
    }

    // 🆕 Helper Methods
    
    /**
     * Check if investor profile is complete
     */
    private boolean isInvestorProfileComplete(InvestorProfile profile) {
        if (profile == null) return false;
        
        // Define what makes an investor profile "complete"
        return profile.getCompanyName() != null && !profile.getCompanyName().trim().isEmpty() &&
               profile.getInvestmentFocus() != null && !profile.getInvestmentFocus().isEmpty() &&
               profile.getMinInvestmentAmount() != null &&
               profile.getMaxInvestmentAmount() != null;
    }
    
    /**
     * Get current user's property count
     */
    private long getCurrentUserPropertyCount(Long userId) {
        try {
            // You might want to implement this in RealEstateService
            // For now, return a placeholder
            return 0L;
        } catch (Exception e) {
            logger.debug("Could not get property count for user: {}", userId);
            return 0L;
        }
    }
    
    /**
     * Get current user's image count
     */
    private long getCurrentUserImageCount(Long userId) {
        try {
            // You might want to implement this in your service
            // For now, return a placeholder
            return 0L;
        } catch (Exception e) {
            logger.debug("Could not get image count for user: {}", userId);
            return 0L;
        }
    }
}