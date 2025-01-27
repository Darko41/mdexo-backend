package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.specification.RealEstateSpecifications;

@RestController
@RequestMapping("/api")
public class RealEstateController {
	
	@Autowired
	private RealEstateRepository realEstateRepository;

	@GetMapping("/real-estates")	// The Pageable object is automatically populated with
									// query parameters like page, size, and sort
									// (e.g., /real-estates?page=0&size=10&sort=createdAt,desc)
	public ResponseEntity<?> getRealEstates(
			@RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
			@RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
			@RequestParam(value = "priceMax", required = false) String propertyType,
			Pageable pageable) {	
		
		try {
			Specification<RealEstate> spec = Specification.where(null);
			
			if(priceMin != null) {
				spec = spec.and(RealEstateSpecifications.hasMinPrice(priceMin));
			}
			if (priceMax != null) {
				spec = spec.and(RealEstateSpecifications.hasMaxPrice(priceMax));
			}
			if (propertyType != null) {
				spec = spec.and(RealEstateSpecifications.hasPropertyType(propertyType));
			}
			
			Page<RealEstate> realEstates = realEstateRepository.findAll(spec, pageable);
			return ResponseEntity.ok(realEstates);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching real estate data");
		}
	}

	@PostMapping("/add")
	public ResponseEntity<RealEstate> createRealEstate(@RequestBody RealEstate realEstate) {
		RealEstate savedRealEstate = realEstateRepository.save(realEstate);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedRealEstate);
	}
	
	@DeleteMapping("/delete/{propertyId}")
	public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
		if (realEstateRepository.existsById(propertyId)) {
			realEstateRepository.deleteById(propertyId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
	
	@PutMapping("/real-estates/{propertyId}")
	public ResponseEntity<RealEstate> updateRealEstate(
			@PathVariable Long propertyId,
			@RequestBody RealEstate realEstateDetails) {
		
		try {
			RealEstate realEstate = realEstateRepository.findById(propertyId)
					.orElseThrow( () -> new RuntimeException("Real estate not found with id: " + propertyId));
			
			if (realEstateDetails.getTitle() != null) {
			realEstate.setTitle(realEstateDetails.getTitle());
			}
			if (realEstateDetails.getDescription() != null) {
			realEstate.setDescription(realEstateDetails.getDescription());
			}
			if (realEstateDetails.getPropertyType() != null) {
			realEstate.setPropertyType(realEstateDetails.getPropertyType());
			}
			if (realEstateDetails.getPrice() != null) {
			realEstate.setPrice(realEstateDetails.getPrice());
			}
			if (realEstateDetails.getAddress() != null) {
			realEstate.setAddress(realEstateDetails.getAddress());
			}
			if (realEstateDetails.getCity() != null) {
			realEstate.setCity(realEstateDetails.getCity());
			}
			if (realEstateDetails.getState() != null) {
			realEstate.setState(realEstateDetails.getState());
			}
			if (realEstateDetails.getZipCode() != null) {
			realEstate.setZipCode(realEstateDetails.getZipCode());
			}
			if (realEstateDetails.getSizeInSqMt() != null) {
			realEstate.setSizeInSqMt(realEstateDetails.getSizeInSqMt());
			}
			
			realEstate.setUpdatedAt(LocalDate.now());
			
			RealEstate updatedRealEstate = realEstateRepository.save(realEstate);
			
			return ResponseEntity.ok(updatedRealEstate);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		
	}
}
