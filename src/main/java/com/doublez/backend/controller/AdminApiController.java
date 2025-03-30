package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.doublez.backend.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {
	
	@Autowired
	private RealEstateApiController realEstateController;

	@Autowired
	private UserService userService;
	
	private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

	
	@PostMapping("/real-estates/add")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RealEstate> createRealEstate(RealEstate realEstate) {
		return realEstateController.createRealEstate(realEstate);
	}
	
	@PutMapping("/real-estates/update/{propertyId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RealEstate> updateRealEstate(
			@PathVariable Long propertyId,
			@RequestBody RealEstate realEstateDetails) {
		return realEstateController.updateRealEstate(propertyId, realEstateDetails);
	}
	
	@GetMapping("/real-estates")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getRealEstate(
			@RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
            @RequestParam(value = "propertyType", required = false) String propertyType,
            @RequestParam(value = "features", required = false) List<String> features,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "zipCode", required = false) String zipCode,
            Pageable pageable) {
		
		return realEstateController.getRealEstates(priceMin, priceMax, propertyType, features, city, state, zipCode, pageable);
	}
	
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
