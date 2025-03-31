package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.config.security.SecurityConfig;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.specification.RealEstateSpecifications;

import jakarta.validation.Valid;

@RestController
//@CrossOrigin(origins = "https://mdexo-frontend.onrender.com")	// was "http://localhost:5173"
@RequestMapping("/api/real-estates")
public class RealEstateApiController {

    @Autowired
    private RealEstateService realEstateService;

    private static final Logger logger = LoggerFactory.getLogger(RealEstateApiController.class);

    @GetMapping("/")
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

            Page<RealEstate> realEstates = realEstateService.getRealEstates(spec, pageable);
            return ResponseEntity.ok(realEstates);
        } catch (Exception e) {
            logger.error("Error fetching real estate data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching real estate data");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<RealEstate> createRealEstate(@RequestBody RealEstate realEstate) {
        RealEstate savedRealEstate = realEstateService.createRealEstate(realEstate);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRealEstate);
    }

    @DeleteMapping("/delete/{propertyId}")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        try {
            realEstateService.deleteRealEstate(propertyId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting real estate with id: " + propertyId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/update/{propertyId}")
    public ResponseEntity<RealEstate> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody Map<String, Object> updates) {

        try {
            RealEstate updatedRealEstate = realEstateService.updateRealEstate(propertyId, updates);
            return ResponseEntity.ok(updatedRealEstate);
        } catch (Exception e) {
            logger.error("Error updating real estate with id: " + propertyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
