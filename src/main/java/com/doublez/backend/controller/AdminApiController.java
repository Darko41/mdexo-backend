package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.service.UserService;
import com.doublez.backend.specification.RealEstateSpecifications;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private RealEstateService realEstateService;  // Directly inject the service

    @Autowired
    private UserService userService; // For user-related functionality (no changes here)

    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    // Create a new real estate listing
    @PostMapping("/real-estates/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RealEstate> createRealEstate(@RequestBody RealEstate realEstate) {
        try {
            RealEstate savedRealEstate = realEstateService.createRealEstate(realEstate);  // Delegate to service
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRealEstate);
        } catch (Exception e) {
            logger.error("Error creating real estate", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Update an existing real estate listing
    @PutMapping("/real-estates/update/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RealEstate> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody Map<String, Object> updates) {
        try {
            RealEstate updatedRealEstate = realEstateService.updateRealEstate(propertyId, updates);  // Delegate to service
            return ResponseEntity.ok(updatedRealEstate);
        } catch (Exception e) {
            logger.error("Error updating real estate with id: " + propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get all real estate listings with filters
    @GetMapping("/real-estates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRealEstates(
            @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
            @RequestParam(value = "propertyType", required = false) String propertyType,
            @RequestParam(value = "features", required = false) List<String> features,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "zipCode", required = false) String zipCode,
            @RequestParam(value = "listingType", required = false) String listingType,
            Pageable pageable) {
        try {
            // Using the service to get real estate data with filters
            Specification<RealEstate> spec = Specification.where(null);

            if (priceMin != null) {
                spec = spec.and(RealEstateSpecifications.hasMinPrice(priceMin));
            }
            if (priceMax != null) {
                spec = spec.and(RealEstateSpecifications.hasMaxPrice(priceMax));
            }
            if (propertyType != null) {
                spec = spec.and(RealEstateSpecifications.hasPropertyType(propertyType));
            }
            if (features != null && !features.isEmpty()) {
                spec = spec.and(RealEstateSpecifications.hasFeatures(features));
            }
            if (city != null || state != null || zipCode != null) {
                spec = spec.and(RealEstateSpecifications.hasLocation(city, state, zipCode));
            }
            if (listingType != null) {
                spec = spec.and(RealEstateSpecifications.hasListingType(listingType));
            }

            Page<RealEstate> realEstates = realEstateService.getRealEstates(spec, pageable);  // Delegate to service
            return ResponseEntity.ok(realEstates);
        } catch (Exception e) {
            logger.error("Error fetching real estate data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching real estate data");
        }
    }

    // Delete a real estate listing
    @DeleteMapping("/real-estates/delete/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        try {
            realEstateService.deleteRealEstate(propertyId);  // Delegate to service
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting real estate with id: " + propertyId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // User management endpoints (as before)
    @PostMapping("/users/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addUser(@RequestBody UserDetailsDTO userDetailsDTO) {
        try {
            String response = userService.addUser(userDetailsDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/users/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserProfile(@PathVariable Long id, @RequestBody UserDetailsDTO userDetailsDTO) {
        boolean isUpdated = userService.updateProfile(id, userDetailsDTO);
        if (isUpdated) {
            return ResponseEntity.ok("User profile updated successfully!");
        }
        return ResponseEntity.ok("User not found with id: " + id);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDetailsDTO>> getAllUsers() {
        List<UserDetailsDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}

