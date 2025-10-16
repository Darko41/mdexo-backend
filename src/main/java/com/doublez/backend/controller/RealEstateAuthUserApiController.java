package com.doublez.backend.controller;

//import org.slf4j.Logger;
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

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.service.realestate.RealEstateAgentAssignmentService;
import com.doublez.backend.service.realestate.RealEstateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/real-estates")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AGENT')")
public class RealEstateAuthUserApiController {
    private final RealEstateService realEstateService;
    private final RealEstateAgentAssignmentService assignmentService;

    public RealEstateAuthUserApiController(RealEstateService realEstateService,
                                 RealEstateAgentAssignmentService assignmentService) {
        this.realEstateService = realEstateService;
        this.assignmentService = assignmentService;
    }

    // Unified create endpoint - handles both with and without images
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @RequestPart @Valid RealEstateCreateDTO createDto,
            @RequestPart(required = false) MultipartFile[] images) {
        
        RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }
    
 // Agent/Admin creating listing for other users
    @PostMapping(value = "/for-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<RealEstateResponseDTO> createRealEstateForUser(
            @RequestPart @Valid RealEstateCreateDTO createDto,
            @RequestPart(required = false) MultipartFile[] images) {
        
        RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }

    // Keep the JSON-only endpoint for backward compatibility
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<RealEstateResponseDTO> createRealEstateJson(
            @RequestBody @Valid RealEstateCreateDTO createDto) {
        // Call the same service method but with no images
        RealEstateResponseDTO response = realEstateService.createRealEstate(createDto, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }

    // Remove the old createWithImages method entirely

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("@realEstateAuthorizationService.canDeleteRealEstate(#propertyId)")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        realEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{propertyId}")
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateUpdateAccess(#propertyId)")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {
        return ResponseEntity.ok(realEstateService.updateRealEstate(propertyId, updateDto));
    }
    
    @PostMapping("/{propertyId}/assign-agent/{agentId}")
    @PreAuthorize("hasRole('ADMIN') or @realEstateAuthorizationService.isOwner(#propertyId)")
    public ResponseEntity<Void> assignAgentToProperty(
            @PathVariable Long propertyId,
            @PathVariable Long agentId) {
        assignmentService.assignAgentToProperty(propertyId, agentId);
        return ResponseEntity.ok().build();
    }
}

