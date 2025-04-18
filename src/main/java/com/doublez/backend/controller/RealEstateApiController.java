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
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.service.RealEstateImageService;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/real-estates")
//@CrossOrigin		TODO TRY THIS!
public class RealEstateApiController {
    private final RealEstateService realEstateService;
    private final RealEstateImageService realEstateImageService;
    private static final Logger logger = LoggerFactory.getLogger(RealEstateApiController.class);

    public RealEstateApiController(RealEstateService realEstateService, 
                                 RealEstateImageService realEstateImageService) {
        this.realEstateService = realEstateService;
        this.realEstateImageService = realEstateImageService;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RealEstateResponseDTO>> searchRealEstates(
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
            priceMin, priceMax, propertyType, features,
            city, state, zipCode, listingType, pageable);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
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

    @PutMapping("/{propertyId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @realEstateSecurityService.isOwner(#propertyId))")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {
        return ResponseEntity.ok(realEstateService.updateRealEstate(propertyId, updateDto));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("hasRole('USER') and @realEstateSecurityService.isOwner(#propertyId)")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        realEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{propertyId}")
    public ResponseEntity<RealEstateResponseDTO> getRealEstateById(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(realEstateService.getRealEstateById(propertyId));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @RequestBody @Valid RealEstateCreateDTO createDto) {
        RealEstateResponseDTO response = realEstateService.createRealEstate(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/real-estates/" + response.getPropertyId())
                .body(response);
    }
}

