package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;
import com.doublez.backend.service.realestate.RealEstateImageService;
import com.doublez.backend.service.realestate.RealEstateService;


@RestController
@RequestMapping("/api/real-estates")
public class RealEstateOpenApiController {
	
	private final RealEstateService realEstateService;
	
	public RealEstateOpenApiController(RealEstateService realEstateService, 
            RealEstateImageService realEstateImageService) {
		this.realEstateService = realEstateService;
	}
	
	// Show all real estates if without parameters
	@GetMapping("/search")
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
	
    
    // Get real estate by id (PathVariable)
    @GetMapping("/{propertyId}")
    public ResponseEntity<RealEstateResponseDTO> getRealEstateById(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(realEstateService.getRealEstateById(propertyId));
    }
    
    @GetMapping("/features")
    public ResponseEntity<List<String>> getAllUniqueFeatures() {
        try {
            List<String> features = realEstateService.getAllUniqueFeatures();
            return ResponseEntity.ok(features);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
