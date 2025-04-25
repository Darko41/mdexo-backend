package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;

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
import com.doublez.backend.service.RealEstateImageService;
import com.doublez.backend.service.RealEstateService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/real-estates")
@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'AGENT')")
//@CrossOrigin		TODO TRY THIS!
public class RealEstateAuthUserApiController {
    private final RealEstateService realEstateService;
//    private static final Logger logger = LoggerFactory.getLogger(RealEstateAuthUserApiController.class);

    public RealEstateAuthUserApiController(RealEstateService realEstateService, 
                                 RealEstateImageService realEstateImageService) {
        this.realEstateService = realEstateService;
    }

    // TODO Test this
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RealEstateResponseDTO> createWithImages(
            @RequestPart @Valid RealEstateCreateDTO createDto,
            @RequestPart(required = false) MultipartFile[] images) {
        
        RealEstateResponseDTO response = realEstateService.createWithImages(
            createDto, 
            images != null ? images : new MultipartFile[0]
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }

    // Create real estate as authenticated user, agent or admin
    @PostMapping
    @PreAuthorize("@realEstateAuthorizationService.hasRealEstateCreateAccess()")
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @RequestBody @Valid RealEstateCreateDTO createDto) {
        RealEstateResponseDTO response = realEstateService.createRealEstate(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }
    
    /** TODO (deleteRealEstate and updateRealEstate):
     * 1. track assigned agents (many-to-many relationship):
     * private Set<User> assignedAgents = new HashSet<>()... in RealEstate class,
     * 2. Modify RealEstateAuthorizationService to include agent-specific logic,
     * 3. add this method to the service: isAssignedAgent(Long propertyId)
     */

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
}

