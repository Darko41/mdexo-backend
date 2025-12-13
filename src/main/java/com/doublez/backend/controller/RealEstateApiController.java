package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.dto.realestate.RemoveImagesRequest;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.PropertyType;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.service.realestate.FeaturedListingService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/real-estates")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AGENT')")
public class RealEstateApiController {
    private static final Logger logger = LoggerFactory.getLogger(RealEstateApiController.class);

    private final RealEstateService realEstateService;
    private final UserService userService;
    private final FeaturedListingService featuredListingService;

    public RealEstateApiController(RealEstateService realEstateService, UserService userService,
            FeaturedListingService featuredListingService) {
        this.realEstateService = realEstateService;
        this.userService = userService;
        this.featuredListingService = featuredListingService;
    }

    // === PUBLIC ENDPOINTS (no auth) ===

    /**
     * Public search - accessible without authentication
     */
    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<RealEstateResponseDTO>> searchRealEstates(
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
            Pageable pageable) {

        try {
            logger.info("🔍 Public search - term: {}, type: {}, city: {}, bedrooms: {}-{}", 
                    searchTerm, propertyType, city, minBedrooms, maxBedrooms);

            Page<RealEstateResponseDTO> result = realEstateService.searchRealEstates(
                    searchTerm, priceMin, priceMax, propertyType, features, city, state, zipCode, 
                    listingType, minBedrooms, maxBedrooms, hasParking, hasElevator, energyEfficiency, pageable);

            logger.info("✅ Public search completed - {} results", result.getTotalElements());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Public search failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Public get by ID - accessible without authentication
     */
    @GetMapping("/{propertyId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getRealEstateById(@PathVariable Long propertyId) {
        try {
            logger.info("🔍 Public fetching property: {}", propertyId);

            RealEstateResponseDTO property = realEstateService.getRealEstateById(propertyId);

            logger.info("✅ Public property retrieved: {}", propertyId);
            return ResponseEntity.ok(property);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to fetch property: {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch property"));
        }
    }

    // NEW: Get real estate with detailed analytics
    @GetMapping("/{propertyId}/analytics")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getRealEstateWithAnalytics(@PathVariable Long propertyId) {
        try {
            logger.info("📊 Public fetching property with analytics: {}", propertyId);

            RealEstateResponseDTO property = realEstateService.getRealEstateWithAnalytics(propertyId);

            logger.info("✅ Public property with analytics retrieved: {}", propertyId);
            return ResponseEntity.ok(property);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to fetch property analytics: {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch property analytics"));
        }
    }

    /**
     * Public features list - accessible without authentication
     */
    @GetMapping("/features")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllUniqueFeatures() {
        try {
            logger.info("🔍 Fetching all unique features");

            List<String> features = realEstateService.getAllUniqueFeatures();

            logger.info("✅ Found {} unique features", features.size());
            return ResponseEntity.ok(features);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch features", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch features"));
        }
    }

    // NEW: Get features by property type
    @GetMapping("/features/by-type")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getFeaturesByPropertyType() {
        try {
            logger.info("🔍 Fetching features by property type");

            Map<PropertyType, List<String>> featuresByType = realEstateService.getUniqueFeaturesByPropertyType();

            logger.info("✅ Found features for {} property types", featuresByType.size());
            return ResponseEntity.ok(featuresByType);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch features by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch features by type"));
        }
    }

    // NEW: Get popular properties
    @GetMapping("/popular")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getPopularProperties(@RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("🔥 Fetching {} popular properties", limit);

            List<RealEstateResponseDTO> popular = realEstateService.getPopularProperties(limit);

            logger.info("✅ Found {} popular properties", popular.size());
            return ResponseEntity.ok(popular);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch popular properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch popular properties"));
        }
    }

    // NEW: Get recently added properties
    @GetMapping("/recent")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getRecentlyAddedProperties(@RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("🆕 Fetching {} recently added properties", limit);

            List<RealEstateResponseDTO> recent = realEstateService.getRecentlyAddedProperties(limit);

            logger.info("✅ Found {} recent properties", recent.size());
            return ResponseEntity.ok(recent);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch recent properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch recent properties"));
        }
    }

    // NEW: Get similar properties
    @GetMapping("/{propertyId}/similar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getSimilarProperties(@PathVariable Long propertyId,
            @RequestParam(defaultValue = "6") int limit) {
        try {
            logger.info("🔍 Fetching {} similar properties for {}", limit, propertyId);

            List<RealEstateResponseDTO> similar = realEstateService.getSimilarProperties(propertyId, limit);

            logger.info("✅ Found {} similar properties for {}", similar.size(), propertyId);
            return ResponseEntity.ok(similar);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch similar properties for {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch similar properties"));
        }
    }

    // === AUTHENTICATED USER ENDPOINTS ===

    /**
     * Get current user's properties
     */
    @GetMapping("/my-properties")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserProperties(Pageable pageable) {
        try {
            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            logger.info("📋 Fetching properties for user: {}", userId);

            Page<RealEstateResponseDTO> properties = realEstateService.getPropertiesByOwner(userId, pageable);

            logger.info("✅ Found {} properties for user: {}", properties.getTotalElements(), userId);
            return ResponseEntity.ok(properties);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch user properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch properties"));
        }
    }

    // NEW: Get user properties with filters
    @GetMapping("/my-properties/filtered")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserPropertiesWithFilters(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) ListingType listingType,
            Pageable pageable) {
        try {
            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            logger.info("📋 Fetching filtered properties for user: {}, active: {}, listingType: {}", 
                    userId, isActive, listingType);

            Page<RealEstateResponseDTO> properties = realEstateService.getPropertiesByOwnerWithFilters(
                    userId, isActive, listingType, pageable);

            logger.info("✅ Found {} filtered properties for user: {}", properties.getTotalElements(), userId);
            return ResponseEntity.ok(properties);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch filtered user properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch filtered properties"));
        }
    }

    // NEW: Get investor portfolio stats
    @GetMapping("/my-portfolio/stats")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyPortfolioStats() {
        try {
            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            logger.info("📊 Fetching portfolio stats for user: {}", userId);

            // Check if user is an investor
            if (!currentUser.isInvestor()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only investors can access portfolio stats"));
            }

            PortfolioStatsDTO stats = realEstateService.getInvestmentPortfolioStats(userId);

            logger.info("✅ Portfolio stats retrieved for user: {}", userId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch portfolio stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch portfolio stats"));
        }
    }

    // === IMAGE MANAGEMENT ENDPOINTS ===

    /**
     * REPLACE ALL images for a property
     */
    @PutMapping(value = "/{propertyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<?> replacePropertyImages(@PathVariable Long propertyId,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages) {

        try {
            logger.info("🔄 Replacing all images for property {}", propertyId);

            RealEstate updatedProperty = realEstateService.replacePropertyImages(propertyId, newImages);
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);

            logger.info("✅ Successfully replaced images for property {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for image replacement: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (LimitationExceededException e) {
            logger.warn("🚫 Image limit exceeded for property: {}", propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to replace images for property: {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to replace images"));
        }
    }

    /**
     * REMOVE SPECIFIC images from a property
     */
    @DeleteMapping("/{propertyId}/images")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<?> removePropertyImages(@PathVariable Long propertyId,
            @RequestBody RemoveImagesRequest request) {

        try {
            logger.info("🗑️ Removing {} images from property {}", request.getImageUrls().size(), propertyId);

            RealEstate updatedProperty = realEstateService.removeImagesFromProperty(propertyId, request.getImageUrls());
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);

            logger.info("✅ Successfully removed images from property {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for image removal: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to remove images from property: {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove images"));
        }
    }

    /**
     * ADD images to existing property
     */
    @PostMapping(value = "/{propertyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<?> addPropertyImages(@PathVariable Long propertyId,
            @RequestPart("images") MultipartFile[] images) {

        try {
            logger.info("📤 Adding {} images to property {}", images.length, propertyId);

            RealEstate updatedProperty = realEstateService.addImagesToProperty(propertyId, images);
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);

            logger.info("✅ Successfully added images to property {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for image addition: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (LimitationExceededException e) {
            logger.warn("🚫 Image limit exceeded for property: {}", propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to add images to property: {}", propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add images"));
        }
    }

    // === CREATE ENDPOINTS ===

    /**
     * Unified create endpoint with image processing
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<?> createRealEstate(@Valid @RequestPart("realEstate") RealEstateCreateDTO createDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            logger.info("🏠 Creating new real estate listing with {} images", images != null ? images.length : 0);

            RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, images);

            logger.info("✅ Successfully created real estate with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId()).body(response);

        } catch (LimitationExceededException e) {
            logger.warn("🚫 Real estate creation limit exceeded");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create real estate"));
        }
    }

    // NEW: Create investment property
    @PostMapping(value = "/investment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<?> createInvestmentProperty(@Valid @RequestPart("realEstate") RealEstateCreateDTO createDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            logger.info("💰 Creating investment property with {} images", images != null ? images.length : 0);

            RealEstateResponseDTO response = realEstateService.createInvestmentProperty(createDto, images);

            logger.info("✅ Successfully created investment property with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId()).body(response);

        } catch (LimitationExceededException e) {
            logger.warn("🚫 Investment property creation limit exceeded");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalOperationException e) {
            logger.warn("🚫 User is not an investor");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to create investment property: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create investment property"));
        }
    }

    /**
     * Agent/Admin creating listing for other users
     */
    @PostMapping(value = "/for-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<?> createRealEstateForUser(@Valid @RequestPart("realEstate") RealEstateCreateDTO createDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {

        try {
            logger.info("👤 Creating real estate for user with {} images", images != null ? images.length : 0);

            RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);

            logger.info("✅ Successfully created real estate for user with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId()).body(response);

        } catch (LimitationExceededException e) {
            logger.warn("🚫 Real estate creation limit exceeded for target user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create real estate for user"));
        }
    }

    /**
     * JSON-only endpoint for backward compatibility
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<?> createRealEstateJson(@RequestBody @Valid RealEstateCreateDTO createDto) {
        try {
            logger.info("📄 Creating real estate from JSON (no images)");

            RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, null);

            logger.info("✅ Successfully created real estate from JSON with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId()).body(response);

        } catch (LimitationExceededException e) {
            logger.warn("🚫 Real estate creation limit exceeded");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate from JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create real estate"));
        }
    }

    // === UPDATE & DELETE ENDPOINTS ===

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateDeleteAccess(#propertyId)")
    public ResponseEntity<?> deleteRealEstate(@PathVariable Long propertyId) {
        try {
            logger.info("🗑️ Deleting real estate with ID: {}", propertyId);

            realEstateService.deleteRealEstate(propertyId);

            logger.info("✅ Successfully deleted real estate with ID: {}", propertyId);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for deletion: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to delete real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete property"));
        }
    }

    /**
     * ENHANCED UPDATE with image support
     */
    @PutMapping(value = "/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<?> updateRealEstateWithImages(@PathVariable Long propertyId,
            @RequestPart("realEstate") @Valid RealEstateUpdateDTO updateDto,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages,
            @RequestParam(value = "imagesToRemove", required = false) List<String> imagesToRemove) {

        try {
            logger.info("✏️ Updating real estate {} with {} new images, removing {} images", propertyId,
                    newImages != null ? newImages.length : 0, imagesToRemove != null ? imagesToRemove.size() : 0);

            RealEstateResponseDTO response = realEstateService.updateRealEstate(propertyId, updateDto, newImages,
                    imagesToRemove);

            logger.info("✅ Successfully updated real estate with ID: {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for update: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to update real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update property"));
        }
    }

    /**
     * JSON-only update for backward compatibility
     */
    @PutMapping(value = "/{propertyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<?> updateRealEstateJson(@PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {

        try {
            logger.info("✏️ Updating real estate {} (JSON only)", propertyId);

            RealEstateResponseDTO response = realEstateService.updateRealEstate(propertyId, updateDto);

            logger.info("✅ Successfully updated real estate with ID: {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for update: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to update real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update property"));
        }
    }

    // NEW: Increment contact count
    @PostMapping("/{propertyId}/increment-contact")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> incrementContactCount(@PathVariable Long propertyId) {
        try {
            logger.info("📞 Incrementing contact count for property: {}", propertyId);

            realEstateService.incrementContactCount(propertyId);

            logger.info("✅ Contact count incremented for property: {}", propertyId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Contact count incremented"));

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for contact count: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to increment contact count for property {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to increment contact count"));
        }
    }

    // 🆕 ADDITIONAL ENDPOINTS

    /**
     * Get active featured listings (public) - using FeaturedListingService method
     */
    @GetMapping("/featured/active")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getActiveFeaturedListings(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            logger.info("⭐ Fetching {} active featured listings", limit);

            // ✅ Now using the proper service method
            List<RealEstate> featuredRealEstates = featuredListingService.getActiveFeaturedListings(limit);

            // Convert to DTOs
            List<RealEstateResponseDTO> featuredList = featuredRealEstates.stream().map(RealEstateResponseDTO::new)
                    .collect(Collectors.toList());

            logger.info("✅ Found {} active featured listings", featuredList.size());
            return ResponseEntity.ok(featuredList);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch featured listings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch featured listings"));
        }
    }

    // NEW: Get featured properties using service method
    @GetMapping("/featured")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getFeaturedProperties(@RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("⭐ Fetching {} featured properties", limit);

            List<RealEstateResponseDTO> featured = realEstateService.getFeaturedRealEstates(limit);

            logger.info("✅ Found {} featured properties", featured.size());
            return ResponseEntity.ok(featured);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch featured properties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch featured properties"));
        }
    }

    // === FEATURE MANAGEMENT ENDPOINTS ===

    /**
     * Feature a real estate listing
     */
    @PostMapping("/{propertyId}/feature")
    @PreAuthorize("hasRole('USER') or hasRole('AGENT')")
    public ResponseEntity<?> featureRealEstate(@PathVariable Long propertyId, @RequestParam Integer featuredDays) {
        try {
            logger.info("⭐ Featuring property {} for {} days", propertyId, featuredDays);

            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            RealEstate featuredRealEstate = featuredListingService.featureRealEstate(userId, propertyId, featuredDays);
            RealEstateResponseDTO response = new RealEstateResponseDTO(featuredRealEstate);

            logger.info("✅ Successfully featured property {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for featuring: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (LimitationExceededException e) {
            logger.warn("🚫 Feature limit exceeded for user");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalOperationException e) {
            logger.warn("🚫 User doesn't own property: {}", propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to feature property {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to feature property"));
        }
    }

    /**
     * Unfeature a real estate listing
     */
    @PostMapping("/{propertyId}/unfeature")
    @PreAuthorize("hasRole('USER') or hasRole('AGENT')")
    public ResponseEntity<?> unfeatureRealEstate(@PathVariable Long propertyId) {
        try {
            logger.info("❌ Unfeaturing property {}", propertyId);

            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            RealEstate unfeaturedRealEstate = featuredListingService.unfeatureRealEstate(userId, propertyId);
            RealEstateResponseDTO response = new RealEstateResponseDTO(unfeaturedRealEstate);

            logger.info("✅ Successfully unfeatured property {}", propertyId);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            logger.warn("❌ Property not found for unfeaturing: {}", propertyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Property not found"));
        } catch (IllegalOperationException e) {
            logger.warn("🚫 User doesn't own property: {}", propertyId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to unfeature property {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unfeature property"));
        }
    }

    /**
     * Check if user can feature a specific property
     */
    @GetMapping("/{propertyId}/can-feature")
    @PreAuthorize("hasRole('USER') or hasRole('AGENT')")
    public ResponseEntity<?> canFeatureRealEstate(@PathVariable Long propertyId) {
        try {
            User currentUser = userService.getAuthenticatedUser();
            Long userId = currentUser.getId();

            boolean canFeature = featuredListingService.canFeatureRealEstate(userId, propertyId);

            return ResponseEntity.ok(Map.of("canFeature", canFeature));

        } catch (Exception e) {
            logger.error("❌ Failed to check feature eligibility for property {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check feature eligibility"));
        }
    }
}