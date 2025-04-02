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
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.RealEstateService;
import com.doublez.backend.service.S3Service;
import com.doublez.backend.specification.RealEstateSpecifications;

import jakarta.validation.Valid;

@RestController
//@CrossOrigin(origins = "https://mdexo-frontend.onrender.com")	// was "http://localhost:5173"
@RequestMapping("/api/real-estates")
public class RealEstateApiController {

    @Autowired
    private RealEstateService realEstateService;
    
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private UserRepository userRepository;

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
    	
//    	try {
//			Page<RealEstate> realEstates = realEstateService.get		
//		} catch (Exception e) {
//			// TODO: handle exception
//		}

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
    
    @PostMapping(value = "/create-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createRealEstateWithImages(
    		@RequestPart ("realEstate") @Valid RealEstateDTO realEstateDTO,
    		@RequestPart (value = "images", required = false) MultipartFile[] images,
    		@AuthenticationPrincipal UserDetails userDetails) {
    	
    	try {
    		// Convert DTO to Entity
			RealEstate realEstate = convertToEntity(realEstateDTO);
			
			// Set owner from authenticated user
			if (userDetails != null) {
				User owner = userRepository.findByEmail(userDetails.getUsername())
						.orElseThrow( () -> new RuntimeException("User not found"));
				realEstate.setOwner(owner);
			}
			
			if (images != null && images.length > 0) {
				List<String> imageUrls = uploadImagesToS3(images);
				realEstate.setImages(imageUrls);
			}
			
			RealEstate savedRealEstate = realEstateService.createRealEstate(realEstate);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedRealEstate);
			
		} catch (Exception e) {
			logger.error("Error creating real estate with images", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating listing: " + e.getMessage());
		}
    	
    }

	private List<String> uploadImagesToS3(MultipartFile[] images) throws IOException{
		
		List<String> imageUrls = new ArrayList<>();
		
		for (MultipartFile image : images) {
			String fileName = "real-estates/" + UUID.randomUUID() + "-" + image.getOriginalFilename();
			String presignedUrl = s3Service.generatePresignedUrl(fileName);
			imageUrls.add(presignedUrl.split("\\?")[0]);
		}
				
		return imageUrls;
	}
	
	private void uploadToS3(String presignedUrl, MultipartFile file) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(presignedUrl).openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", file.getContentType());
		connection.getOutputStream().write(file.getBytes());
		
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Failed to upload file to S3");
		}
	}

	private RealEstate convertToEntity(RealEstateDTO realEstateDTO) {
		RealEstate realEstate = new RealEstate();
		realEstate.setTitle(realEstateDTO.getTitle());
		realEstate.setDescription(realEstateDTO.getDescription());
		realEstate.setPropertyType(realEstateDTO.getPropertyType());
		realEstate.setListingType(realEstateDTO.getListingType());
        realEstate.setPrice(realEstateDTO.getPrice());
        realEstate.setAddress(realEstateDTO.getAddress());
        realEstate.setCity(realEstateDTO.getCity());
        realEstate.setState(realEstateDTO.getState());
        realEstate.setZipCode(realEstateDTO.getZipCode());
        realEstate.setSizeInSqMt(realEstateDTO.getSizeInSqMt());
        realEstate.setFeatures(realEstateDTO.getFeatures());
        realEstate.setCreatedAt(LocalDate.now());
		return realEstate;
	}
    
}




//
//@PostMapping(value = "/create-with-images", consumes = MULTIPART_FORM_DATA_VALUE)
//public ResponseEntity<ApiResponse<RealEstate>> createWithImages(
//        @RequestPart @Valid RealEstateDTO dto,
//        @RequestPart(required = false) MultipartFile[] images,
//        @AuthenticationPrincipal UserDetails userDetails) {
//    
//    try {
//        User owner = userService.getAuthenticatedUser(userDetails);
//        List<String> imageUrls = realEstateImageService.uploadRealEstateImages(images);
//        
//        RealEstate realEstate = realEstateService.createFromDto(dto, owner, imageUrls);
//        return ResponseEntity.ok(ApiResponse.success(realEstate));
//        
//    } catch (InvalidImageException e) {
//        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//    } catch (ImageUploadException e) {
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//            .body(ApiResponse.error("Image upload failed. Please try again later."));
//    }
//}


















