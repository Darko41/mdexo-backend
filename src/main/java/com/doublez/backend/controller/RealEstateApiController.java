package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.dto.realestate.RemoveImagesRequest;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.service.realestate.RealEstateService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/real-estates")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AGENT')")
public class RealEstateApiController {
    private static final Logger logger = LoggerFactory.getLogger(RealEstateApiController.class);
    
    private final RealEstateService realEstateService;

    public RealEstateApiController(RealEstateService realEstateService) {
        this.realEstateService = realEstateService;
    }

    // === PUBLIC ENDPOINTS (no auth) ===
    
    /**
     * Public search - accessible without authentication
     */
    @GetMapping("/search")
    @PreAuthorize("permitAll()")  // Allow public access
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
            Pageable pageable) {
        
        Page<RealEstateResponseDTO> result = realEstateService.searchRealEstates(
            searchTerm, priceMin, priceMax, propertyType, features,
            city, state, zipCode, listingType, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Public get by ID - accessible without authentication
     */
    @GetMapping("/{propertyId}")
    @PreAuthorize("permitAll()")  // Allow public access
    public ResponseEntity<RealEstateResponseDTO> getRealEstateById(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(realEstateService.getRealEstateById(propertyId));
    }
    
    /**
     * Public features list - accessible without authentication
     */
    @GetMapping("/features")
    @PreAuthorize("permitAll()")  // Allow public access
    public ResponseEntity<List<String>> getAllUniqueFeatures() {
        try {
            List<String> features = realEstateService.getAllUniqueFeatures();
            return ResponseEntity.ok(features);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // === AUTHENTICATED USER ENDPOINTS ===

    /**
     * Get current user's properties
     */
    @GetMapping("/my-properties")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<RealEstateResponseDTO>> getUserProperties(
            Authentication authentication,
            Pageable pageable) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Page<RealEstateResponseDTO> properties = realEstateService.getPropertiesByOwner(userDetails.getId(), pageable);
        return ResponseEntity.ok(properties);
    }

    // === IMAGE MANAGEMENT ENDPOINTS ===
    
    /**
     * REPLACE ALL images for a property
     */
    @PutMapping(value = "/{propertyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> replacePropertyImages(
            @PathVariable Long propertyId,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages) {
        
        try {
            logger.info("🔄 Replacing all images for property {}", propertyId);
            RealEstate updatedProperty = realEstateService.replacePropertyImages(propertyId, newImages);
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);
            logger.info("✅ Successfully replaced images for property {}", propertyId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * REMOVE SPECIFIC images from a property
     */
    @DeleteMapping("/{propertyId}/images")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> removePropertyImages(
            @PathVariable Long propertyId,
            @RequestBody RemoveImagesRequest request) {
        
        try {
            logger.info("🗑️ Removing {} images from property {}", 
                       request.getImageUrls().size(), propertyId);
            RealEstate updatedProperty = realEstateService.removeImagesFromProperty(
                propertyId, request.getImageUrls());
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);
            logger.info("✅ Successfully removed images from property {}", propertyId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ADD images to existing property
     */
    @PostMapping(value = "/{propertyId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> addPropertyImages(
            @PathVariable Long propertyId,
            @RequestPart("images") MultipartFile[] images) {
        
        try {
            logger.info("📤 Adding {} images to property {}", images.length, propertyId);
            RealEstate updatedProperty = realEstateService.addImagesToProperty(propertyId, images);
            RealEstateResponseDTO response = new RealEstateResponseDTO(updatedProperty);
            logger.info("✅ Successfully added images to property {}", propertyId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
 // Unified create endpoint with image processing
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @Valid @RequestPart("realEstate") RealEstateCreateDTO createDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        
        try {
            logger.info("🏠 Creating new real estate listing with {} images", 
                images != null ? images.length : 0);
            
            RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, images);
            
            logger.info("✅ Successfully created real estate with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Agent/Admin creating listing for other users
    @PostMapping(value = "/for-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<RealEstateResponseDTO> createRealEstateForUser(
            @Valid @RequestPart("realEstate") RealEstateCreateDTO createDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        
        try {
            logger.info("👤 Creating real estate for user with {} images", 
                images != null ? images.length : 0);
            
            RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);
            
            logger.info("✅ Successfully created real estate for user with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate for user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // JSON-only endpoint for backward compatibility
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<RealEstateResponseDTO> createRealEstateJson(
            @RequestBody @Valid RealEstateCreateDTO createDto) {
        
        try {
            logger.info("📄 Creating real estate from JSON (no images)");
            
            // Call the same service method but with no images
            RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, null);
            
            logger.info("✅ Successfully created real estate from JSON with ID: {}", response.getPropertyId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/real-estates/" + response.getPropertyId())
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("❌ Failed to create real estate from JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("@realEstateAuthorizationService.canDeleteRealEstate(#propertyId)")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        try {
            logger.info("🗑️ Deleting real estate with ID: {}", propertyId);
            realEstateService.deleteRealEstate(propertyId);
            logger.info("✅ Successfully deleted real estate with ID: {}", propertyId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ Failed to delete real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{propertyId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {
        try {
            logger.info("✏️ Updating real estate with ID: {}", propertyId);
            RealEstateResponseDTO response = realEstateService.updateRealEstate(propertyId, updateDto);
            logger.info("✅ Successfully updated real estate with ID: {}", propertyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to update real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ENHANCED UPDATE with image support
     */
    @PutMapping(value = "/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstateWithImages(
            @PathVariable Long propertyId,
            @RequestPart("realEstate") @Valid RealEstateUpdateDTO updateDto,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages,
            @RequestParam(value = "imagesToRemove", required = false) List<String> imagesToRemove) {
        
        try {
            logger.info("✏️ Updating real estate {} with {} new images, removing {} images", 
                       propertyId, 
                       newImages != null ? newImages.length : 0,
                       imagesToRemove != null ? imagesToRemove.size() : 0);
            
            RealEstateResponseDTO response = realEstateService.updateRealEstate(
                propertyId, updateDto, newImages, imagesToRemove);
            
            logger.info("✅ Successfully updated real estate with ID: {}", propertyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to update real estate {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // JSON-only update for backward compatibility
    @PutMapping(value = "/{propertyId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstateJson(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {
        // This calls the method that doesn't handle images (backward compatibility)
        return updateRealEstate(propertyId, updateDto);
    }
}