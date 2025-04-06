package com.doublez.backend.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import com.doublez.backend.dto.RealEstateDTO;
import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponse;
import com.doublez.backend.dto.RealEstateUpdateRequest;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.ImageUploadException;
import com.doublez.backend.exception.InvalidImageException;
import com.doublez.backend.exception.RealEstateGetAllException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.response.ApiResponse;
import com.doublez.backend.service.RealEstateImageService;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.service.S3Service;
import com.doublez.backend.service.UserService;
import com.doublez.backend.specification.RealEstateSpecifications;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
//@CrossOrigin(origins = "https://mdexo-frontend.onrender.com")	// was "http://localhost:5173"
@RequestMapping("/api/real-estates")
public class RealEstateApiController {

    private final RealEstateService realEstateService;
    private final RealEstateImageService realEstateImageService;
    private final UserService userService;
    private final RealEstateMapper realEstateMapper;
    
    
    public RealEstateApiController(RealEstateService realEstateService, RealEstateImageService realEstateImageService,
			UserService userService, RealEstateMapper realEstateMapper) {
		this.realEstateService = realEstateService;
		this.realEstateImageService = realEstateImageService;
		this.userService = userService;
		this.realEstateMapper = realEstateMapper;
	}

	private static final Logger logger = LoggerFactory.getLogger(RealEstateApiController.class);

    @GetMapping("/search")
    public ResponseEntity<Page<RealEstateResponse>> searchRealEstates(
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) List<String> features,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) ListingType listingType,
            Pageable pageable) {
        
        Page<RealEstateResponse> result = realEstateService.searchRealEstates(
            priceMin, priceMax, propertyType, features,
            city, state, zipCode, listingType, pageable);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RealEstateResponse> createRealEstate(
            @RequestBody @Valid RealEstateRequest request) {
        RealEstateResponse response = realEstateService.createRealEstate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/delete/{propertyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        realEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{propertyId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RealEstateResponse> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateRequest updates) {
        
        RealEstateResponse response = realEstateService.updateRealEstate(propertyId, updates);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RealEstateResponse> createWithImages(
            @RequestPart @Valid RealEstateRequest request,
            @RequestPart(required = false) MultipartFile[] images) {
        
        RealEstateResponse response = realEstateService.createWithImages(
            request, 
            images != null ? images : new MultipartFile[0]
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
	
    
}

