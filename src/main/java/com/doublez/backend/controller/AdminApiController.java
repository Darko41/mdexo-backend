package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.request.RealEstateRequest;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
    private final RealEstateService realEstateService;
    private final UserService userService;
    
    public AdminApiController(RealEstateService realEstateService, 
                            UserService userService) {
        this.realEstateService = realEstateService;
        this.userService = userService;
    }

    // Real Estate Endpoints
    
    @PostMapping(value = "/real-estates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @RequestPart @Valid RealEstateCreateDTO createDto,
            @RequestPart(required = false) MultipartFile[] images) {
        
        // Use the new unified method instead of the deleted createWithImages
        RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/real-estates/" + response.getPropertyId())
            .body(response);
    }

    @GetMapping("/real-estates")
    public ResponseEntity<Page<RealEstateResponseDTO>> getAllRealEstates(
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
        
        return ResponseEntity.ok(realEstateService.searchRealEstates(
            searchTerm, 
            priceMin, 
            priceMax, 
            propertyType, 
            features,
            city, 
            state, 
            zipCode, 
            listingType, 
            pageable));
    }

    @PutMapping("/real-estates/{propertyId}")
    public ResponseEntity<RealEstateResponseDTO> updateRealEstate(
            @PathVariable Long propertyId,
            @RequestBody @Valid RealEstateUpdateDTO updateDto) {
    	RealEstateResponseDTO response = realEstateService.updateRealEstate(propertyId, updateDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/real-estates/{propertyId}")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        realEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }
    
    

    // User Management Endpoints
    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestBody @Valid UserCreateDTO createDto) {
        UserResponseDTO response = userService.createUser(createDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/users/" + response.getId())
            .body(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDTO updateDto) {
        UserResponseDTO updatedUser = userService.updateUserProfile(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    
}

