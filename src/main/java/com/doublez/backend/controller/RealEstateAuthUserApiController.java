package com.doublez.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.service.realestate.RealEstateAgentAssignmentService;
import com.doublez.backend.service.realestate.RealEstateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/real-estates")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AGENT')")
public class RealEstateAuthUserApiController {
    private static final Logger logger = LoggerFactory.getLogger(RealEstateAuthUserApiController.class);
    
    private final RealEstateService realEstateService;
    private final RealEstateAgentAssignmentService assignmentService;

    public RealEstateAuthUserApiController(RealEstateService realEstateService,
                                 RealEstateAgentAssignmentService assignmentService) {
        this.realEstateService = realEstateService;
        this.assignmentService = assignmentService;
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
    
    @PostMapping("/{propertyId}/assign-agent/{agentId}")
    @PreAuthorize("hasRole('ADMIN') or @realEstateAuthorizationService.isOwner(#propertyId)")
    public ResponseEntity<Void> assignAgentToProperty(
            @PathVariable Long propertyId,
            @PathVariable Long agentId) {
        try {
            logger.info("👨‍💼 Assigning agent {} to property {}", agentId, propertyId);
            assignmentService.assignAgentToProperty(propertyId, agentId);
            logger.info("✅ Successfully assigned agent {} to property {}", agentId, propertyId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Failed to assign agent {} to property {}: {}", agentId, propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}