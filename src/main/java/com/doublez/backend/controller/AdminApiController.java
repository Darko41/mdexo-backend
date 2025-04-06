package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponse;
import com.doublez.backend.dto.RealEstateUpdateRequest;
import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.service.UserService;
import com.doublez.backend.specification.RealEstateSpecifications;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private RealEstateService realEstateService;
    private UserService userService;
    
    public AdminApiController(RealEstateService realEstateService, UserService userService) {
		this.realEstateService = realEstateService;
		this.userService = userService;
	}

//	private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

	@PostMapping(value = "/real-estates/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RealEstateResponse> createRealEstateWithImages(
	        @RequestPart @Valid RealEstateRequest request,
	        @RequestPart(required = false) MultipartFile[] images) {
	    
	    RealEstateResponse response = realEstateService.createWithImages(request, images);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/real-estates/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RealEstateResponse> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateRequest updates) {
        RealEstateResponse response = realEstateService.updateRealEstate(propertyId, updates);
        return ResponseEntity.ok(response);
    }

	@GetMapping("/real-estates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RealEstateResponse>> getRealEstates(
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

	@DeleteMapping("/real-estates/{propertyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        realEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }

    // User management endpoints
	@PostMapping("/users")
	public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDto) {
	    UserResponseDTO response = userService.createUser(createDto);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateUser(
	        @PathVariable Long id,
	        @RequestBody @Valid UserUpdateDTO userUpdateDTO) {
	    boolean isUpdated = userService.updateProfile(id, userUpdateDTO);
	    return isUpdated ?
	        ResponseEntity.ok("User updated successfully") :
	        ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
	    return ResponseEntity.ok(
	        userService.getAllUsers().stream()
	            .map(u -> new UserResponseDTO(
	                u.getId(),
	                u.getEmail(),
	                u.getRoles(),
	                u.getCreatedAt(),
	                u.getUpdatedAt()
	            ))
	            .collect(Collectors.toList())
	    );
	}
}

