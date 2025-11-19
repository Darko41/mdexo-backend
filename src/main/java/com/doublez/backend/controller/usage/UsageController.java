package com.doublez.backend.controller.usage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.realestate.RealEstateAuthorizationService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final RealEstateAuthorizationService authService;
    private final UserService userService;
    private final RealEstateRepository realEstateRepository;

    public UsageController(RealEstateAuthorizationService authService, 
            UserService userService,
            RealEstateRepository realEstateRepository) {
	this.authService = authService;
	this.userService = userService;
	this.realEstateRepository = realEstateRepository;
	}

    private Long getCurrentUserId() {
        return userService.getCurrentUserId();
    }

    // ============================================
    // BASIC USAGE STATS
    // ============================================

    @GetMapping("/stats")
    public ResponseEntity<UsageStatsDTO> getCurrentUserUsageStats() {
        try {
            Long userId = getCurrentUserId();
            UsageStatsDTO stats = authService.getUsageStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedUsage() {
        try {
            Long userId = getCurrentUserId();
            UsageStatsDTO stats = authService.getUsageStats(userId);
            
            // Convert UsageStatsDTO to Map for detailed view
            Map<String, Object> usage = new HashMap<>();
            usage.put("userTier", stats.getTier());
            usage.put("currentListings", stats.getCurrentListings());
            usage.put("maxListings", stats.getMaxListings());
            usage.put("currentImages", stats.getCurrentImages());
            usage.put("maxImages", stats.getMaxImages());
            usage.put("canCreateListing", stats.isCanCreateListing());
            usage.put("canUploadImage", stats.isCanUploadImage());
            usage.put("canFeatureListing", stats.isCanFeatureListing());
            usage.put("currentFeatured", stats.getCurrentFeatured());
            usage.put("maxFeatured", stats.getMaxFeatured());
            usage.put("isBusinessAccount", stats.isBusinessAccount());
            
            return ResponseEntity.ok(usage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================
    // SPECIFIC PERMISSION CHECKS
    // ============================================

    @GetMapping("/can-create-realestate")
    public ResponseEntity<Map<String, Boolean>> canCreateRealEstate() {
        try {
            Long userId = getCurrentUserId();
            boolean canCreate = authService.canCreateRealEstate(userId);
            return ResponseEntity.ok(Collections.singletonMap("canCreate", canCreate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/can-upload-image")
    public ResponseEntity<Map<String, Boolean>> canUploadImage(
            @RequestParam(required = false) Integer countToUpload) {
        try {
            Long userId = getCurrentUserId();
            boolean canUpload = authService.canUploadImages(userId, countToUpload);
            return ResponseEntity.ok(Collections.singletonMap("canUpload", canUpload));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/can-upload-images")
    public ResponseEntity<Map<String, Boolean>> canUploadImages(@RequestParam int imageCount) {
        try {
            Long userId = getCurrentUserId();
            boolean canUpload = authService.canUploadImages(userId, imageCount);
            return ResponseEntity.ok(Collections.singletonMap("canUpload", canUpload));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/can-feature-listing")
    public ResponseEntity<Map<String, Boolean>> canFeatureListing() {
        try {
            Long userId = getCurrentUserId();
            boolean canFeature = authService.canFeatureRealEstate(userId);
            return ResponseEntity.ok(Collections.singletonMap("canFeature", canFeature));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================
    // FEATURED LISTING INFO
    // ============================================

    @GetMapping("/featured-info")
    public ResponseEntity<Map<String, Object>> getFeaturedUsageInfo() {
        try {
            Long userId = getCurrentUserId();
            UsageStatsDTO stats = authService.getUsageStats(userId);
            
            Map<String, Object> featuredInfo = new HashMap<>();
            featuredInfo.put("canFeature", stats.isCanFeatureListing());
            featuredInfo.put("currentFeatured", stats.getCurrentFeatured());
            featuredInfo.put("maxFeatured", stats.getMaxFeatured());
            
            return ResponseEntity.ok(featuredInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================
    // AGENCY SPECIFIC USAGE (if applicable)
    // ============================================

    @GetMapping("/agency-stats")
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public ResponseEntity<?> getAgencyUsageStats() {
        try {
            Long userId = getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            if (!user.isAgencyAdmin() || user.getOwnedAgencies().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User is not an agency admin"));
            }
            
            Agency agency = user.getOwnedAgencies().get(0);
            Long agencyId = agency.getId();
            
            // Get agency-specific stats
            long agencyProperties = realEstateRepository.countActiveRealEstatesByAgency(agencyId);
            long agencyFeatured = realEstateRepository.findByAgencyId(agencyId)
                .stream()
                .filter(RealEstate::getIsFeatured)
                .count();
                
            Map<String, Object> agencyStats = new HashMap<>();
            agencyStats.put("agencyId", agencyId);
            agencyStats.put("agencyName", agency.getName());
            agencyStats.put("totalProperties", agencyProperties);
            agencyStats.put("featuredProperties", agencyFeatured);
            agencyStats.put("activeProperties", agencyProperties); // Assuming all are active for now
            
            return ResponseEntity.ok(agencyStats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve agency stats: " + e.getMessage()));
        }
    }

    // ============================================
    // COMPREHENSIVE USAGE SUMMARY
    // ============================================

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getUsageSummary() {
        try {
            Long userId = getCurrentUserId();
            UsageStatsDTO stats = authService.getUsageStats(userId);
            User user = userService.getUserEntityById(userId);
            
            Map<String, Object> summary = new HashMap<>();
            
            // Basic usage
            summary.put("listings", Map.of(
                "current", stats.getCurrentListings(),
                "max", stats.getMaxListings(),
                "remaining", stats.getMaxListings() - stats.getCurrentListings(),
                "canCreate", stats.isCanCreateListing()
            ));
            
            summary.put("images", Map.of(
                "current", stats.getCurrentImages(),
                "max", stats.getMaxImages(),
                "remaining", stats.getMaxImages() - stats.getCurrentImages(),
                "canUpload", stats.isCanUploadImage()
            ));
            
            summary.put("featured", Map.of(
                "current", stats.getCurrentFeatured(),
                "max", stats.getMaxFeatured(),
                "remaining", stats.getMaxFeatured() - stats.getCurrentFeatured(),
                "canFeature", stats.isCanFeatureListing()
            ));
            
            // User info
            summary.put("user", Map.of(
                "tier", stats.getTier(),
                "isBusinessAccount", stats.isBusinessAccount(),
                "isAgencyAdmin", user.isAgencyAdmin(),
                "isInvestor", user.isInvestor()
            ));
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}