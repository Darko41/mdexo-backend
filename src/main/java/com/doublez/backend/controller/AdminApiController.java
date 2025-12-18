package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.realestate.PortfolioStatsDTO;
import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateFormUpdateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.ListingType;
import com.doublez.backend.enums.property.PropertyType;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.response.ApiResponse;
import com.doublez.backend.service.realestate.AdminRealEstateService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final AdminRealEstateService adminRealEstateService;
    private final RealEstateService realEstateService;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    public AdminApiController(AdminRealEstateService adminRealEstateService, UserService userService,
            RealEstateService realEstateService) {
        this.adminRealEstateService = adminRealEstateService;
        this.userService = userService;
        this.realEstateService = realEstateService;
    }

    // ========================
    // REAL ESTATE ENDPOINTS
    // ========================

//    @PostMapping(value = "/real-estates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<RealEstateResponseDTO> createRealEstate(@RequestPart @Valid RealEstateCreateDTO createDto,
//            @RequestPart(required = false) MultipartFile[] images) {
//
//        logger.info("👑 Admin creating real estate for user: {}",
//                createDto.getOwnerId() != null ? createDto.getOwnerId() : "current user");
//
//        RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .header("Location", "/api/real-estates/" + response.getPropertyId()).body(response);
//    }

    // ENHANCED: Get all real estates with pagination and filters
    @GetMapping("/real-estates")
    public ResponseEntity<Page<RealEstateResponseDTO>> getAllRealEstates(
            @RequestParam(required = false) String searchTerm, 
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) List<String> features, 
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state, 
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) ListingType listingType,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer maxBedrooms,
            @RequestParam(required = false) Boolean hasParking,
            @RequestParam(required = false) Boolean hasElevator,
            @RequestParam(required = false) EnergyEfficiency energyEfficiency,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured,
            Pageable pageable) {

        logger.info("👑 Admin searching real estates - filters: searchTerm={}, propertyType={}, city={}, isActive={}", 
                searchTerm, propertyType, city, isActive);

        // Use enhanced search with more filters
        Page<RealEstateResponseDTO> results = realEstateService.searchRealEstates(
                searchTerm, priceMin, priceMax, propertyType, features, city, state, zipCode, 
                listingType, minBedrooms, maxBedrooms, hasParking, hasElevator, energyEfficiency, pageable);
        
        return ResponseEntity.ok(results);
    }

    // NEW: Get real estates with admin filters
    @GetMapping("/real-estates/filtered")
    public ResponseEntity<Page<RealEstateResponseDTO>> getRealEstatesWithFilters(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) ListingType listingType,
            Pageable pageable) {

        logger.info("👑 Admin filtering real estates - isActive={}, isFeatured={}, propertyType={}", 
                isActive, isFeatured, propertyType);

        Page<RealEstateResponseDTO> results = adminRealEstateService.getRealEstatesWithFilters(
                isActive, isFeatured, propertyType, listingType, pageable);
        
        return ResponseEntity.ok(results);
    }

    /**
     * ENHANCED ADMIN UPDATE with image support
     */
//    @PutMapping(value = "/real-estates/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, Object>> updateRealEstate(@PathVariable Long propertyId,
//            @ModelAttribute @Valid RealEstateFormUpdateDTO formUpdateDto, HttpServletRequest request) {
//
//        if (formUpdateDto == null || formUpdateDto.getUpdateDto() == null) {
//            throw new IllegalArgumentException("Update data cannot be null");
//        }
//
//        try {
//            logger.info("👑 Admin updating real estate {} with image changes", propertyId);
//
//            RealEstateUpdateDTO updateDto = formUpdateDto.getUpdateDto();
//            MultipartFile[] images = formUpdateDto.getImages();
//
//            // Use the enhanced method with image support
//            RealEstateResponseDTO response = adminRealEstateService.updateRealEstate(propertyId, updateDto, images,
//                    null); // 🆕 imagesToRemove can be handled via updateDto
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("message", "Real estate updated successfully");
//            result.put("redirectUrl", "/admin/real-estates/" + propertyId + "/view");
//            result.put("propertyId", propertyId);
//            result.put("property", response);
//
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            logger.error("❌ Admin failed to update real estate {}: {}", propertyId, e.getMessage(), e);
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", false);
//            result.put("message", "Failed to update real estate: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
//        }
//    }

    // NEW: Enhanced update with image removal support
//    @PutMapping(value = "/real-estates/{propertyId}/images")
//    public ResponseEntity<Map<String, Object>> updateRealEstateImages(@PathVariable Long propertyId,
//            @RequestPart(required = false) MultipartFile[] newImages,
//            @RequestParam(required = false) List<String> imagesToRemove,
//            @RequestParam(required = false) Boolean replaceImages) {
//
//        try {
//            logger.info("👑 Admin updating images for real estate {} - new: {}, remove: {}, replace: {}", 
//                    propertyId, 
//                    newImages != null ? newImages.length : 0, 
//                    imagesToRemove != null ? imagesToRemove.size() : 0,
//                    replaceImages);
//
//            // Create a minimal update DTO for image operations
//            RealEstateUpdateDTO updateDto = new RealEstateUpdateDTO();
//            updateDto.setReplaceImages(replaceImages);
//
//            RealEstateResponseDTO response = adminRealEstateService.updateRealEstate(
//                    propertyId, updateDto, newImages, imagesToRemove);
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", true);
//            result.put("message", "Images updated successfully");
//            result.put("property", response);
//
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            logger.error("❌ Admin failed to update images for real estate {}: {}", propertyId, e.getMessage(), e);
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", false);
//            result.put("message", "Failed to update images: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
//        }
//    }

    @DeleteMapping("/real-estates/{propertyId}")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        logger.info("👑 Admin deleting real estate: {}", propertyId);
        adminRealEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }

    // NEW: Bulk delete
    @DeleteMapping("/real-estates/bulk")
    public ResponseEntity<ApiResponse<String>> bulkDeleteRealEstates(@RequestBody List<Long> propertyIds) {
        logger.info("👑 Admin bulk deleting {} real estates", propertyIds.size());
        try {
            adminRealEstateService.bulkDeleteRealEstates(propertyIds);
            return ResponseEntity.ok(ApiResponse.success("Properties deleted successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to bulk delete properties: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete properties: " + e.getMessage()));
        }
    }

    @GetMapping("/real-estates/{propertyId}")
    public ResponseEntity<RealEstateResponseDTO> getRealEstate(@PathVariable Long propertyId) {
        logger.info("👑 Admin fetching real estate: {}", propertyId);
        RealEstateResponseDTO realEstate = adminRealEstateService.getRealEstateById(propertyId);
        return ResponseEntity.ok(realEstate);
    }

    // NEW: Get real estate with detailed analytics
//    @GetMapping("/real-estates/{propertyId}/analytics")
//    public ResponseEntity<RealEstateResponseDTO> getRealEstateWithAnalytics(@PathVariable Long propertyId) {
//        logger.info("👑 Admin fetching real estate with analytics: {}", propertyId);
//        RealEstateResponseDTO realEstate = adminRealEstateService.getRealEstateWithAnalytics(propertyId);
//        return ResponseEntity.ok(realEstate);
//    }

    // ========================
    // PROPERTY STATUS MANAGEMENT
    // ========================

    @PatchMapping("/real-estates/{propertyId}/activate")
    public ResponseEntity<ApiResponse<String>> activateProperty(@PathVariable Long propertyId) {
        logger.info("👑 Admin activating property: {}", propertyId);
        try {
            realEstateService.activateProperty(propertyId);
            return ResponseEntity.ok(ApiResponse.success("Property activated successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to activate property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to activate property: " + e.getMessage()));
        }
    }

    @PatchMapping("/real-estates/{propertyId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateProperty(@PathVariable Long propertyId) {
        logger.info("👑 Admin deactivating property: {}", propertyId);
        try {
            realEstateService.deactivateProperty(propertyId);
            return ResponseEntity.ok(ApiResponse.success("Property deactivated successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to deactivate property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to deactivate property: " + e.getMessage()));
        }
    }

    // NEW: Feature property with duration
    @PatchMapping("/real-estates/{propertyId}/feature")
    public ResponseEntity<ApiResponse<String>> featureProperty(@PathVariable Long propertyId,
            @RequestParam(required = false) Integer days) {
        logger.info("👑 Admin featuring property: {} for {} days", propertyId, days);
        try {
            adminRealEstateService.featureProperty(propertyId, days != null ? days : 7); // Default 7 days
            return ResponseEntity.ok(ApiResponse.success("Property featured successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to feature property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to feature property: " + e.getMessage()));
        }
    }

    // NEW: Unfeature property
    @PatchMapping("/real-estates/{propertyId}/unfeature")
    public ResponseEntity<ApiResponse<String>> unfeatureProperty(@PathVariable Long propertyId) {
        logger.info("👑 Admin unfeaturing property: {}", propertyId);
        try {
            adminRealEstateService.unfeatureProperty(propertyId);
            return ResponseEntity.ok(ApiResponse.success("Property unfeatured successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to unfeature property {}: {}", propertyId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to unfeature property: " + e.getMessage()));
        }
    }

    // NEW: Bulk status update
    @PatchMapping("/real-estates/bulk-status")
    public ResponseEntity<ApiResponse<String>> bulkUpdateStatus(@RequestBody List<Long> propertyIds,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured) {
        logger.info("👑 Admin bulk updating {} properties - active: {}, featured: {}", 
                propertyIds.size(), isActive, isFeatured);
        try {
            adminRealEstateService.bulkUpdateStatus(propertyIds, isActive, isFeatured);
            return ResponseEntity.ok(ApiResponse.success("Properties updated successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to bulk update properties: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update properties: " + e.getMessage()));
        }
    }

    // NEW: Transfer property ownership
    @PatchMapping("/real-estates/{propertyId}/transfer")
    public ResponseEntity<ApiResponse<String>> transferPropertyOwnership(@PathVariable Long propertyId,
            @RequestParam Long newOwnerId) {
        logger.info("👑 Admin transferring property {} to user {}", propertyId, newOwnerId);
        try {
            adminRealEstateService.transferPropertyOwnership(propertyId, newOwnerId);
            return ResponseEntity.ok(ApiResponse.success("Property ownership transferred successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to transfer property ownership: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to transfer ownership: " + e.getMessage()));
        }
    }

    // NEW: Bulk transfer ownership
    @PatchMapping("/real-estates/bulk-transfer")
    public ResponseEntity<ApiResponse<String>> bulkTransferOwnership(@RequestBody List<Long> propertyIds,
            @RequestParam Long newOwnerId) {
        logger.info("👑 Admin bulk transferring {} properties to user {}", propertyIds.size(), newOwnerId);
        try {
            adminRealEstateService.bulkTransferOwnership(propertyIds, newOwnerId);
            return ResponseEntity.ok(ApiResponse.success("Properties transferred successfully"));
        } catch (Exception e) {
            logger.error("❌ Failed to bulk transfer properties: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to transfer properties: " + e.getMessage()));
        }
    }

    // ========================
    // USER MANAGEMENT ENDPOINTS
    // ========================

//    @PostMapping("/users")
//    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO.Create createDto) {
//        logger.info("👑 Admin creating user: {}", createDto.getEmail());
//        UserDTO response = userService.registerUser(createDto, true);
//        return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/api/users/" + response.getId())
//                .body(response);
//    }

//    @PutMapping("/users/{id}")
//    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserDTO.Update updateDto) {
//        logger.info("👑 Admin updating user: {}", id);
//        UserDTO updatedUser = userService.updateUser(id, updateDto);
//        return ResponseEntity.ok(updatedUser);
//    }

//    @GetMapping("/users")
//    public ResponseEntity<List<UserDTO>> getAllUsers() {
//        logger.info("👑 Admin fetching all users");
//        return ResponseEntity.ok(userService.getAllUsers());
//    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        logger.info("👑 Admin deleting user: {}", id);
        try {
            userService.deleteUserAsAdmin(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (UserNotFoundException e) {
            logger.warn("⚠️ User not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("❌ Failed to delete user {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    // ========================
    // ADMIN UTILITY ENDPOINTS
    // ========================

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAdminAccess(Authentication authentication) {
        logger.info("🔐 Admin access verification");
        if (authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // ========================
    // ADMIN ANALYTICS & DASHBOARD
    // ========================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        logger.info("👑 Admin fetching dashboard stats");
        
        // Use the new admin analytics service
        Map<String, Object> stats = adminRealEstateService.getAdminAnalytics();
        
        // Add user stats
        stats.put("totalUsers", userService.getUserCount());
        
        return ResponseEntity.ok(stats);
    }

    // NEW: Get portfolio stats for any user
    @GetMapping("/users/{userId}/portfolio-stats")
    public ResponseEntity<PortfolioStatsDTO> getUserPortfolioStats(@PathVariable Long userId) {
        logger.info("👑 Admin fetching portfolio stats for user: {}", userId);
        try {
            PortfolioStatsDTO stats = realEstateService.getInvestmentPortfolioStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Failed to get portfolio stats for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // NEW: Get admin analytics
    @GetMapping("/analytics/real-estates")
    public ResponseEntity<Map<String, Object>> getRealEstateAnalytics() {
        logger.info("👑 Admin fetching real estate analytics");
        Map<String, Object> analytics = adminRealEstateService.getAdminAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // NEW: Get popular properties
    @GetMapping("/real-estates/popular")
    public ResponseEntity<List<RealEstateResponseDTO>> getPopularProperties(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("👑 Admin fetching {} popular properties", limit);
        List<RealEstateResponseDTO> popular = realEstateService.getPopularProperties(limit);
        return ResponseEntity.ok(popular);
    }

    // NEW: Get recently added properties
    @GetMapping("/real-estates/recent")
    public ResponseEntity<List<RealEstateResponseDTO>> getRecentlyAddedProperties(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("👑 Admin fetching {} recently added properties", limit);
        List<RealEstateResponseDTO> recent = realEstateService.getRecentlyAddedProperties(limit);
        return ResponseEntity.ok(recent);
    }

    // NEW: Get similar properties
    @GetMapping("/real-estates/{propertyId}/similar")
    public ResponseEntity<List<RealEstateResponseDTO>> getSimilarProperties(@PathVariable Long propertyId,
            @RequestParam(defaultValue = "6") int limit) {
        logger.info("👑 Admin fetching {} similar properties for {}", limit, propertyId);
        List<RealEstateResponseDTO> similar = realEstateService.getSimilarProperties(propertyId, limit);
        return ResponseEntity.ok(similar);
    }

    // NEW: Increment contact count (for testing/admin tracking)
//    @PostMapping("/real-estates/{propertyId}/increment-contact")
//    public ResponseEntity<ApiResponse<String>> incrementContactCount(@PathVariable Long propertyId) {
//        logger.info("👑 Admin incrementing contact count for property: {}", propertyId);
//        try {
//            realEstateService.incrementContactCount(propertyId);
//            return ResponseEntity.ok(ApiResponse.success("Contact count incremented"));
//        } catch (Exception e) {
//            logger.error("❌ Failed to increment contact count: {}", e.getMessage());
//            return ResponseEntity.internalServerError()
//                    .body(ApiResponse.error("Failed to increment contact count: " + e.getMessage()));
//        }
//    }
}