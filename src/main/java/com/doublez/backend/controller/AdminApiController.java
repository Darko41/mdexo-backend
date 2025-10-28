package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.doublez.backend.dto.RealEstateFormUpdateDTO;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.SelfDeletionException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.response.ApiResponse;
import com.doublez.backend.service.realestate.AdminRealEstateService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
	private final AdminRealEstateService adminRealEstateService;
    private final RealEstateService realEstateService;
    private final UserService userService;
    
    public AdminApiController(AdminRealEstateService adminRealEstateService, 
            UserService userService,
            RealEstateService realEstateService) {
			this.adminRealEstateService = adminRealEstateService;
			this.userService = userService;
			this.realEstateService = realEstateService;
}

    // Real Estate Endpoints
    
    @PostMapping(value = "/real-estates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RealEstateResponseDTO> createRealEstate(
            @RequestPart @Valid RealEstateCreateDTO createDto,
            @RequestPart(required = false) MultipartFile[] images) {
        
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

    @PutMapping(value = "/real-estates/{propertyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RealEstateResponseDTO> updateRealEstate(
            @PathVariable Long propertyId,
            @ModelAttribute RealEstateUpdateDTO updateDto,
            @RequestParam(required = false) MultipartFile[] images,
            HttpServletRequest request) {
        
        System.out.println("=== FORM DATA DEBUG ===");
        System.out.println("UpdateDTO: " + updateDto);
        System.out.println("Is updateDto null? " + (updateDto == null));
        
        // Log all form parameters
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("Form parameter '" + key + "': " + Arrays.toString(values));
        });
        
        if (updateDto == null) {
            throw new IllegalArgumentException("UpdateDTO cannot be null");
        }
        
        RealEstateResponseDTO response = adminRealEstateService.updateRealEstate(propertyId, updateDto, images);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/real-estates/{propertyId}")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long propertyId) {
        adminRealEstateService.deleteRealEstate(propertyId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/real-estates/{propertyId}")
    public ResponseEntity<RealEstateResponseDTO> getRealEstate(@PathVariable Long propertyId) {
        RealEstateResponseDTO realEstate = adminRealEstateService.getRealEstateById(propertyId);
        return ResponseEntity.ok(realEstate);
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
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (IllegalOperationException | SelfDeletionException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAdminAccess(Authentication authentication) {
        if (authentication != null && 
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    
    
}

