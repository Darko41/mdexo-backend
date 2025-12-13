package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.realestate.FeaturedListingService;
import com.doublez.backend.service.realestate.RealEstateAuthorizationService;
import com.doublez.backend.service.user.UserService;
import com.doublez.backend.utils.SecurityUtils;

@RestController
@RequestMapping("/api/featured")
public class FeaturedListingController {

    private final FeaturedListingService featuredListingService;
    private final SecurityUtils securityUtils;
    private final RealEstateAuthorizationService authService;
    private final UserService userService;
    private final RealEstateRepository realEstateRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(FeaturedListingController.class);

    public FeaturedListingController(
            FeaturedListingService featuredListingService, 
            SecurityUtils securityUtils,
            RealEstateAuthorizationService authService,
            UserService userService,
            RealEstateRepository realEstateRepository) { // 🆕 ADDED
        this.featuredListingService = featuredListingService;
        this.securityUtils = securityUtils;
        this.authService = authService;
        this.userService = userService;
        this.realEstateRepository = realEstateRepository; // 🆕 ADDED
    }

    private Long getCurrentUserId() {
        try {
            return securityUtils.getCurrentUserId();
        } catch (Exception e) {
            logger.error("Failed to get current user ID", e);
            throw new UnsupportedOperationException("Unable to retrieve current user ID");
        }
    }

    /**
     * FEATURE A LISTING
     * Allows users to feature their property for a specified number of days
     */
    @PostMapping("/{realEstateId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateFeatureAccess(#realEstateId)")
    public ResponseEntity<?> featureListing(
            @PathVariable Long realEstateId,
            @RequestParam(defaultValue = "30") Integer featuredDays) {
        
        try {
            logger.info("⭐ Featuring listing ID: {} for {} days", realEstateId, featuredDays);
            
            Long userId = getCurrentUserId();
            RealEstate featured = featuredListingService.featureRealEstate(userId, realEstateId, featuredDays);
            
            logger.info("✅ Listing featured successfully - ID: {}, Days: {}", realEstateId, featuredDays);
            return ResponseEntity.ok(featured);
            
        } catch (LimitationExceededException e) {
            logger.warn("🚫 Feature limit exceeded for user: {}, listing: {}", getCurrentUserId(), realEstateId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
                    
        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Listing not found for featuring: {}", realEstateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Real estate not found"));
                    
        } catch (IllegalOperationException e) {
            logger.warn("🚫 Unauthorized featuring attempt - User: {}, Listing: {}", getCurrentUserId(), realEstateId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to feature this property"));
                    
        } catch (Exception e) {
            logger.error("❌ Failed to feature listing: {}", realEstateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to feature listing"));
        }
    }

    /**
     * UNFEATURE A LISTING
     * Remove a property from featured status
     */
    @DeleteMapping("/{realEstateId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateFeatureAccess(#realEstateId)")
    public ResponseEntity<?> unfeatureListing(@PathVariable Long realEstateId) {
        try {
            logger.info("❌ Unfeaturing listing ID: {}", realEstateId);
            
            Long userId = getCurrentUserId();
            RealEstate unfeatured = featuredListingService.unfeatureRealEstate(userId, realEstateId);
            
            logger.info("✅ Listing unfeatured successfully - ID: {}", realEstateId);
            return ResponseEntity.ok(unfeatured);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Listing not found for unfeaturing: {}", realEstateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Real estate not found"));
                    
        } catch (IllegalOperationException e) {
            logger.warn("🚫 Unauthorized unfeaturing attempt - User: {}, Listing: {}", getCurrentUserId(), realEstateId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to unfeature this property"));
                    
        } catch (Exception e) {
            logger.error("❌ Failed to unfeature listing: {}", realEstateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unfeature listing"));
        }
    }

    /**
     * CHECK IF USER CAN FEATURE A LISTING
     * Returns whether the user has available featured slots and permission
     */
    @GetMapping("/can-feature/{realEstateId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#realEstateId)")
    public ResponseEntity<Map<String, Object>> canFeatureListing(@PathVariable Long realEstateId) {
        try {
            logger.info("🔍 Checking if user can feature listing: {}", realEstateId);
            
            Long userId = getCurrentUserId();
            boolean canFeature = featuredListingService.canFeatureRealEstate(userId, realEstateId);
            
            // Get detailed limitation info
            var usageStats = authService.getUsageStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canFeature", canFeature);
            response.put("currentFeatured", usageStats.getCurrentFeatured());
            response.put("maxFeatured", usageStats.getMaxFeatured());
            response.put("availableSlots", Math.max(0, usageStats.getMaxFeatured() - usageStats.getCurrentFeatured()));
            response.put("hasPermission", usageStats.isCanFeatureListing());
            
            logger.info("✅ Feature check completed - canFeature: {}, availableSlots: {}", 
                canFeature, response.get("availableSlots"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to check feature eligibility for listing: {}", realEstateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check feature eligibility"));
        }
    }

    /**
     * GET ACTIVE FEATURED LISTINGS
     * Public endpoint - anyone can view featured listings
     */
    @GetMapping("/active")
    public ResponseEntity<List<RealEstate>> getActiveFeaturedListings(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            logger.info("📋 Fetching {} active featured listings", limit);
            
            List<RealEstate> featured = featuredListingService.getActiveFeaturedListings(limit);
            
            logger.info("✅ Found {} active featured listings", featured.size());
            return ResponseEntity.ok(featured);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch featured listings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET USER'S FEATURED LISTINGS
     * Returns all featured listings for the current user
     */
    @GetMapping("/my-featured")
    public ResponseEntity<List<RealEstate>> getMyFeaturedListings() {
        try {
            Long userId = getCurrentUserId();
            logger.info("📋 Fetching featured listings for user: {}", userId);
            
            // ✅ Use existing repository method
            List<RealEstate> featured = realEstateRepository.findFeaturedRealEstatesByUser(userId);
            
            logger.info("✅ User {} has {} featured listings", userId, featured.size());
            return ResponseEntity.ok(featured);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch user's featured listings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET FEATURED LISTINGS STATISTICS
     * Admin-only endpoint for featured listings analytics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFeaturedStatistics() {
        try {
            logger.info("📊 Admin fetching featured listings statistics");
            
            // ✅ Use existing repository methods
            List<RealEstate> allFeatured = realEstateRepository.findAll().stream()
                .filter(RealEstate::getIsFeatured)
                .collect(Collectors.toList());
                
            List<RealEstate> activeFeatured = realEstateRepository.findActiveFeaturedRealEstates(Pageable.unpaged());
            List<RealEstate> expiredFeatured = realEstateRepository.findExpiredFeaturedRealEstates();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFeatured", allFeatured.size());
            stats.put("activeFeatured", activeFeatured.size());
            stats.put("expiredFeatured", expiredFeatured.size());
            stats.put("activePercentage", allFeatured.size() > 0 ? 
                (double) activeFeatured.size() / allFeatured.size() * 100 : 0);
            
            logger.info("✅ Featured statistics: {} active, {} total", activeFeatured.size(), allFeatured.size());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch featured statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics"));
        }
    }
}