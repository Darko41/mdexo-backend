package com.doublez.backend.service.realestate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.RealEstateNotFoundException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UnauthorizedAccessException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.exception.image.ImageOperationException;
import com.doublez.backend.exception.image.ImageValidationException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.UserService;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class RealEstateService {
	private static final Logger logger = LoggerFactory.getLogger(RealEstateService.class);

	private final RealEstateRepository realEstateRepository;
    private final UserService userService;
    private final RealEstateImageService realEstateImageService;
    private final RealEstateMapper realEstateMapper;
    private final UserRepository userRepository;
    private final RealEstateAuthorizationService authService;
    private final AgencyRepository agencyRepository;

    public RealEstateService(RealEstateRepository realEstateRepository, 
                           UserService userService,
                           RealEstateImageService realEstateImageService, 
                           RealEstateMapper realEstateMapper,
                           UserRepository userRepository,
                           RealEstateAuthorizationService authService, 
                           AgencyRepository agencyRepository) { 
        this.realEstateRepository = realEstateRepository;
        this.userService = userService;
        this.realEstateImageService = realEstateImageService;
        this.realEstateMapper = realEstateMapper;
        this.userRepository = userRepository;
        this.authService = authService;
        this.agencyRepository = agencyRepository;
    }

	public Page<RealEstateResponseDTO> searchRealEstates(String searchTerm, BigDecimal priceMin, BigDecimal priceMax,
			PropertyType propertyType, List<String> features, String city, String state, String zipCode,
			ListingType listingType, Pageable pageable) {

		// Build the complete specification
		Specification<RealEstate> spec = buildCompleteSpecification(searchTerm, priceMin, priceMax, propertyType,
				features, city, state, zipCode, listingType);

		// Execute the query with proper error handling
		try {
			return realEstateRepository.findAll(spec, pageable).map(realEstateMapper::toResponseDto);
		} catch (Exception e) {
			throw new RuntimeException("Search failed: " + e.getMessage(), e);
		}
	}

	private Specification<RealEstate> buildCompleteSpecification(String searchTerm, BigDecimal priceMin,
			BigDecimal priceMax, PropertyType propertyType, List<String> features, String city, String state,
			String zipCode, ListingType listingType) {

		return Specification.where(buildTextSearchSpec(searchTerm)).and(buildPriceSpec(priceMin, priceMax))
				.and(buildPropertyTypeSpec(propertyType)).and(buildFeaturesSpec(features))
				.and(buildLocationSpec(city, state, zipCode)).and(buildListingTypeSpec(listingType));
	}

	// Text search specification
	private Specification<RealEstate> buildTextSearchSpec(String searchTerm) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(searchTerm))
				return null;

			String searchTermLower = searchTerm.toLowerCase();
			query.distinct(true);

			// Create predicates for all searchable fields
			List<Predicate> predicates = new ArrayList<>();

			// Standard text fields
			predicates.add(cb.like(cb.lower(root.get("title")), "%" + searchTermLower + "%"));
			predicates.add(cb.like(cb.lower(root.get("description")), "%" + searchTermLower + "%"));
			predicates.add(cb.like(cb.lower(root.get("city")), "%" + searchTermLower + "%"));
			predicates.add(cb.like(cb.lower(root.get("address")), "%" + searchTermLower + "%"));

			// For features, we'll use a subquery approach
			if (root.get("features") != null) {
				Subquery<String> featureSubquery = query.subquery(String.class);
				Root<RealEstate> subRoot = featureSubquery.correlate(root);
				var featureJoin = subRoot.join("features", JoinType.LEFT);

				featureSubquery.select(featureJoin.as(String.class))
						.where(cb.like(cb.lower(featureJoin.as(String.class)), "%" + searchTermLower + "%"));

				predicates.add(cb.exists(featureSubquery));
			}

			return cb.or(predicates.toArray(new Predicate[0]));
		};
	}

	// Price range specification
	private Specification<RealEstate> buildPriceSpec(BigDecimal min, BigDecimal max) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (min != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min));
			}
			if (max != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("price"), max));
			}

			return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	// Property type specification
	private Specification<RealEstate> buildPropertyTypeSpec(PropertyType propertyType) {
		return (root, query, cb) -> propertyType != null ? cb.equal(root.get("propertyType"), propertyType) : null;
	}

	// Features specification
	private Specification<RealEstate> buildFeaturesSpec(List<String> features) {
		return (root, query, cb) -> {
			if (features == null || features.isEmpty())
				return null;

			List<Predicate> featurePredicates = new ArrayList<>();
			for (String feature : features) {
				featurePredicates.add(cb.isMember(feature, root.get("features")));
			}

			return cb.and(featurePredicates.toArray(new Predicate[0]));
		};
	}

	// Location specification
	private Specification<RealEstate> buildLocationSpec(String city, String state, String zipCode) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.hasText(city)) {
				predicates.add(cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
			}
			if (StringUtils.hasText(state)) {
				predicates.add(cb.equal(cb.lower(root.get("state")), state.toLowerCase()));
			}
			if (StringUtils.hasText(zipCode)) {
				predicates.add(cb.equal(root.get("zipCode"), zipCode));
			}

			return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	// Listing type specification
	private Specification<RealEstate> buildListingTypeSpec(ListingType listingType) {
		return (root, query, cb) -> listingType != null ? cb.equal(root.get("listingType"), listingType) : null;
	}

	// CREATE METHOD WITH USAGE TRACKING
	public RealEstateResponseDTO createRealEstate(RealEstateCreateDTO createDto, MultipartFile[] images) {
        User currentUser = userService.getAuthenticatedUser();
        User owner = resolveOwner(createDto.getOwnerId(), currentUser);

        // 🆕 CHECK IF USER CAN CREATE MORE REAL ESTATES USING AUTH SERVICE
        if (!authService.canCreateRealEstate(owner.getId())) {
            throw new LimitationExceededException("Real estate limit exceeded");
        }

        // Handle image upload if provided
        List<String> imageUrls = Collections.emptyList();
        if (images != null && images.length > 0) {
            // 🆕 CHECK IMAGE UPLOAD LIMITS USING AUTH SERVICE
            if (!authService.canUploadImages(owner.getId(), images.length)) {
                throw new LimitationExceededException("Image upload limit exceeded");
            }
            imageUrls = realEstateImageService.uploadRealEstateImages(images);
        }

        // 🆕 HANDLE AGENCY PROPERTY CREATION
        RealEstate entity = realEstateMapper.toEntity(createDto, owner, imageUrls);
        
        if (currentUser.isAgencyAdmin() && createDto.getAgencyId() != null) {
            Agency agency = agencyRepository.findById(createDto.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
            
            // Verify user owns this agency
            if (!agency.getAdmin().getId().equals(currentUser.getId())) {
                throw new IllegalOperationException("You don't have permission to create properties for this agency");
            }
            
            entity.setAgency(agency);
            entity.setAgentName(createDto.getAgentName());
            entity.setAgentPhone(createDto.getAgentPhone());
            entity.setAgentLicense(createDto.getAgentLicense());
        }

        entity.setCreatedAt(LocalDate.now());
        entity.setIsActive(true);

        RealEstate saved = realEstateRepository.save(entity);
        logger.info("✅ Real estate created successfully for user {} (ID: {})", owner.getEmail(), owner.getId());
        return realEstateMapper.toResponseDto(saved);
    }

	private User resolveOwner(@Nullable Long ownerId, User currentUser) {
		// Default to current user if no override
		if (ownerId == null) {
			return currentUser;
		}

		// Verify admin/agent privileges
		if (!currentUser.hasAnyRole("ROLE_ADMIN", "ROLE_AGENT")) {
			throw new AccessDeniedException("Only admins/agents can assign properties to others");
		}

		return userRepository.findById(ownerId).orElseThrow(() -> new UserNotFoundException(ownerId));
	}

	// METHOD FOR ADMIN WITH USAGE TRACKING
	@PreAuthorize("hasRole('ADMIN') or hasRole('AGENCY_ADMIN')")
	public RealEstateResponseDTO createRealEstateForUser(RealEstateCreateDTO createDto, MultipartFile[] images) {
	    User currentUser = userService.getAuthenticatedUser();

	    User owner = (createDto.getOwnerId() != null && isAdminOrAgent(currentUser))
	            ? userService.getUserEntityById(createDto.getOwnerId())
	            : currentUser;

	    // ✅ REPLACE: usageTrackingService.validateCanCreateRealEstate(owner.getId());
	    if (!authService.canCreateRealEstate(owner.getId())) {
	        throw new LimitationExceededException("Real estate limit exceeded");
	    }

	    // Handle image upload if provided
	    List<String> imageUrls = Collections.emptyList();
	    if (images != null && images.length > 0) {
	        // ✅ REPLACE: usageTrackingService.validateCanUploadImages(owner.getId(), images.length);
	        if (!authService.canUploadImages(owner.getId(), images.length)) {
	            throw new LimitationExceededException("Image upload limit exceeded");
	        }
	        imageUrls = realEstateImageService.uploadRealEstateImages(images);
	    }

	    // 🆕 HANDLE AGENCY PROPERTY CREATION (if applicable)
	    RealEstate entity = realEstateMapper.toEntity(createDto, owner, imageUrls);
	    
	    if (currentUser.isAgencyAdmin() && createDto.getAgencyId() != null) {
	        Agency agency = agencyRepository.findById(createDto.getAgencyId())
	                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
	        
	        // Verify user owns this agency
	        if (!agency.getAdmin().getId().equals(currentUser.getId())) {
	            throw new IllegalOperationException("You don't have permission to create properties for this agency");
	        }
	        
	        entity.setAgency(agency);
	        entity.setAgentName(createDto.getAgentName());
	        entity.setAgentPhone(createDto.getAgentPhone());
	        entity.setAgentLicense(createDto.getAgentLicense());
	    }

	    entity.setCreatedAt(LocalDate.now());
	    entity.setIsActive(true);

	    RealEstate saved = realEstateRepository.save(entity);

	    logger.info("✅ Real estate created by admin/agent for user {} (ID: {})", owner.getEmail(), owner.getId());
	    return realEstateMapper.toResponseDto(saved);
	}

	private boolean isAdminOrAgent(User user) {
	    return user.getRoles().stream()
	            .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_AGENCY_ADMIN".equals(role.getName()));
	}

	@Transactional
	public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto,
			MultipartFile[] newImages, List<String> imagesToRemove) {
		RealEstate realEstate = realEstateRepository.findById(propertyId)
				.orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));

		// Handle image removal first
		if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
			removeImagesFromProperty(propertyId, imagesToRemove);
		}

		// Handle new image uploads (add to existing)
		if (newImages != null && newImages.length > 0) {
			addImagesToProperty(propertyId, newImages);
		}

		// Handle owner update
		if (updateDto.getOwnerId() != null) {
			User owner = userRepository.findById(updateDto.getOwnerId()).orElseThrow(
					() -> new ResourceNotFoundException("User not found with id: " + updateDto.getOwnerId()));
			realEstate.setOwner(owner);
		}

		// Update other fields
		realEstateMapper.updateEntity(updateDto, realEstate);
		realEstate.setUpdatedAt(LocalDate.now());

		RealEstate updated = realEstateRepository.save(realEstate);
		return realEstateMapper.toResponseDto(updated);
	}

	// Method for backward compatibility
	@Transactional
	public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto) {
		return updateRealEstate(propertyId, updateDto, null, null);
	}

	public void deleteRealEstate(Long propertyId) {
		RealEstate entity = getValidatedRealEstate(propertyId);

		// Delete associated images from S3 FIRST
		if (entity.getImages() != null && !entity.getImages().isEmpty()) {
			logger.info("🗑️ Deleting {} images from S3 for property {}", entity.getImages().size(), propertyId);
			realEstateImageService.deleteImages(entity.getImages());
		}

		// Then delete the property from database
		realEstateRepository.delete(entity);
		logger.info("✅ Successfully deleted real estate with ID: {}", propertyId);
	}

	public long getRealEstateCount() {
		return realEstateRepository.count();
	}

	public List<RealEstateResponseDTO> getAllRealEstates() {
		return realEstateRepository.findAll().stream().map(realEstateMapper::toResponseDto)
				.collect(Collectors.toList());
	}

	private RealEstate getValidatedRealEstate(Long propertyId) {
		RealEstate entity = realEstateRepository.findById(propertyId)
				.orElseThrow(() -> new RealEstateNotFoundException(propertyId));

		User currentUser = userService.getAuthenticatedUser();
		if (!entity.getOwner().equals(currentUser)) {
			throw new UnauthorizedAccessException("You don't have permission to modify this property");
		}

		return entity;
	}

	public RealEstateResponseDTO getRealEstateById(Long propertyId) {
		RealEstate realEstate = realEstateRepository.findById(propertyId)
				.orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
		return new RealEstateResponseDTO(realEstate);
	}

	// ADD IMAGES METHOD WITH USAGE TRACKING
	public RealEstate addImagesToProperty(Long propertyId, MultipartFile[] files) {
        RealEstate property = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        User owner = property.getOwner();

        // 🆕 CHECK IMAGE UPLOAD LIMITS USING AUTH SERVICE
        if (files != null && files.length > 0) {
            if (!authService.canUploadImages(owner.getId(), files.length)) {
                throw new LimitationExceededException("Image upload limit exceeded");
            }

            // 🆕 CHECK PER-LISTING IMAGE LIMIT
            UserLimitation limits = authService.getEffectiveLimitations(owner);
            int currentImageCount = property.getImages() != null ? property.getImages().size() : 0;
            if (currentImageCount + files.length > limits.getMaxImagesPerListing()) {
                throw new LimitationExceededException("Per-listing image limit exceeded");
            }
        }

        List<String> imageUrls = realEstateImageService.uploadRealEstateImages(files);
        property.getImages().addAll(imageUrls);

        logger.info("✅ {} images added to property {} for user {}", files.length, propertyId, owner.getId());
        return realEstateRepository.save(property);
    }

	public List<String> getAllUniqueFeatures() {
		List<RealEstate> allRealEstates = realEstateRepository.findAll();
		Set<String> uniqueFeatures = new TreeSet<>();

		for (RealEstate realEstate : allRealEstates) {
			if (realEstate.getFeatures() != null) {
				uniqueFeatures.addAll(realEstate.getFeatures());
			}
		}

		return new ArrayList<>(uniqueFeatures);
	}

	public Page<RealEstateResponseDTO> getPropertiesByOwner(Long ownerId, Pageable pageable) {
		Specification<RealEstate> spec = (root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId);

		return realEstateRepository.findAll(spec, pageable).map(realEstateMapper::toResponseDto);
	}

	// REPLACE IMAGES METHOD WITH USAGE TRACKING
	@Transactional
	public RealEstate replacePropertyImages(Long propertyId, MultipartFile[] newImages) {
	    User currentUser = userService.getAuthenticatedUser();

	    logger.info("🔐 User {} attempting to replace all images for property {}", currentUser.getId(), propertyId);

	    try {
	        RealEstate property = realEstateRepository.findById(propertyId)
	                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

	        User owner = property.getOwner();

	        // Safety check - don't allow replacing with empty array
	        if (newImages != null && newImages.length == 0) {
	            logger.warn("⚠️ User {} attempted to replace all images with empty array for property {}",
	                    currentUser.getId(), propertyId);
	            throw new ImageValidationException(
	                    "Cannot replace images with empty array. Use remove operation instead.");
	        }

	        // 🆕 FIX ONLY THESE 2 LINES - REPLACE DELETED DEPENDENCIES
	        if (newImages != null && newImages.length > 0) {
	            // ✅ REPLACE: usageTrackingService.validateCanUploadImages(owner.getId(), newImages.length);
	            if (!authService.canUploadImages(owner.getId(), newImages.length)) {
	                throw new LimitationExceededException("Image upload limit exceeded");
	            }

	            // ✅ REPLACE: UserLimitation limits = getLimitationsForUser(owner);
	            UserLimitation limits = authService.getEffectiveLimitations(owner);
	            if (newImages.length > limits.getMaxImagesPerListing()) {
	                throw new LimitationExceededException("Per-listing image limit exceeded for replacement",
	                        (long) newImages.length, limits.getMaxImagesPerListing().longValue());
	            }
	        }

	        // 1. Delete old images from S3
	        List<String> oldImageUrls = property.getImages();
	        if (oldImageUrls != null && !oldImageUrls.isEmpty()) {
	            logger.info("🗑️ User {} deleting {} old images from S3 for property {}", currentUser.getId(),
	                    oldImageUrls.size(), propertyId);
	            try {
	                realEstateImageService.deleteImages(oldImageUrls);
	            } catch (Exception e) {
	                logger.error("❌ User {} failed to delete old images from S3 for property {}: {}",
	                        currentUser.getId(), propertyId, e.getMessage());
	                throw new ImageOperationException("Failed to delete old images from storage", e);
	            }
	        }

	        // 2. Upload new images
	        List<String> newImageUrls = Collections.emptyList();
	        if (newImages != null && newImages.length > 0) {
	            try {
	                newImageUrls = realEstateImageService.uploadRealEstateImages(newImages);
	                logger.info("📤 User {} uploaded {} new images to S3 for property {}", currentUser.getId(),
	                        newImageUrls.size(), propertyId);
	            } catch (Exception e) {
	                logger.error("❌ User {} failed to upload new images for property {}: {}", currentUser.getId(),
	                        propertyId, e.getMessage());
	                throw new ImageOperationException("Failed to upload new images to storage", e);
	            }
	        }

	        // 3. Update property with new image URLs
	        property.setImages(newImageUrls);
	        property.setUpdatedAt(LocalDate.now());

	        RealEstate saved = realEstateRepository.save(property);
	        logger.info("🔐 User {} successfully replaced all images for property {}", currentUser.getId(), propertyId);
	        return saved;

	    } catch (ResourceNotFoundException e) {
	        throw e;
	    } catch (ImageValidationException | ImageOperationException e) {
	        throw e;
	    } catch (Exception e) {
	        logger.error("❌ User {} - unexpected error replacing images for property {}: {}", currentUser.getId(),
	                propertyId, e.getMessage(), e);
	        throw new RuntimeException("Failed to replace property images", e);
	    }
	}

	@Transactional
	public RealEstate removeImagesFromProperty(Long propertyId, List<String> imageUrlsToRemove) {
		// Get current user for audit logging
		User currentUser = userService.getAuthenticatedUser();

		logger.info("🔐 User {} attempting to remove {} images from property {}", currentUser.getId(),
				imageUrlsToRemove.size(), propertyId); // 🆕 Use getId() instead of getUsername()

		if (imageUrlsToRemove == null || imageUrlsToRemove.isEmpty()) {
			throw new ImageValidationException("Image URLs to remove cannot be null or empty");
		}

		RealEstate property = realEstateRepository.findById(propertyId)
				.orElseThrow(() -> new ResourceNotFoundException("Property not found"));

		List<String> currentImages = new ArrayList<>(property.getImages());

		// Validate that all images to remove actually exist in the property
		List<String> nonExistentImages = imageUrlsToRemove.stream().filter(url -> !currentImages.contains(url))
				.collect(Collectors.toList());

		if (!nonExistentImages.isEmpty()) {
			logger.warn("⚠️ User {} attempted to remove non-existent images from property {}: {}", currentUser.getId(),
					propertyId, nonExistentImages);
			throw new ImageValidationException(
					"Cannot remove images that don't exist in this property: " + nonExistentImages);
		}

		// Remove from local list
		boolean removed = currentImages.removeAll(imageUrlsToRemove);

		if (removed) {
			try {
				// Delete from S3
				realEstateImageService.deleteImages(imageUrlsToRemove);

				// Update property
				property.setImages(currentImages);
				property.setUpdatedAt(LocalDate.now());

				logger.info("🔐 User {} successfully removed {} images from property {}", currentUser.getId(),
						imageUrlsToRemove.size(), propertyId);
				return realEstateRepository.save(property);

			} catch (Exception e) {
				logger.error("❌ User {} failed to delete images from S3 for property {}: {}", currentUser.getId(),
						propertyId, e.getMessage());
				throw new ImageOperationException("Failed to delete images from storage", e);
			}
		}

		logger.info("ℹ️ User {} - no images removed from property {} (none matched)", currentUser.getId(), propertyId);
		return property;
	}
	
	private UserLimitation getLimitationsForUser(User user) {
	    // Use the auth service which already has this logic
	    return authService.getEffectiveLimitations(user);
	}
	
	// Get agency properties
	public List<RealEstateResponseDTO> getAgencyProperties(Long agencyId) {
	    Agency agency = agencyRepository.findById(agencyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
	    
	    User currentUser = userService.getAuthenticatedUser();
	    if (!agency.getAdmin().getId().equals(currentUser.getId()) && !authService.hasRole("ADMIN")) {
	        throw new IllegalOperationException("You don't have permission to view these properties");
	    }
	    
	    List<RealEstate> properties = realEstateRepository.findByAgencyId(agencyId);
	    return properties.stream()
	            .map(realEstateMapper::toResponseDto)
	            .collect(Collectors.toList());
	}

	// Admin activate/deactivate
	public void activateProperty(Long propertyId) {
	    RealEstate property = realEstateRepository.findById(propertyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
	    
	    if (!authService.hasRole("ADMIN")) {
	        throw new IllegalOperationException("Only administrators can activate/deactivate properties");
	    }
	    
	    property.setIsActive(true);
	    property.preUpdate();
	    realEstateRepository.save(property);
	}

	public void deactivateProperty(Long propertyId) {
	    RealEstate property = realEstateRepository.findById(propertyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
	    
	    if (!authService.hasRole("ADMIN")) {
	        throw new IllegalOperationException("Only administrators can activate/deactivate properties");
	    }
	    
	    property.setIsActive(false);
	    property.preUpdate();
	    realEstateRepository.save(property);
	}
	
	// Investor-specific property creation
	public RealEstateResponseDTO createInvestmentProperty(RealEstateCreateDTO createDto, MultipartFile[] images) {
	    User currentUser = userService.getAuthenticatedUser();
	    
	    // Verify user is an investor
	    if (!currentUser.isInvestor()) {
	        throw new IllegalOperationException("Only investors can create investment properties");
	    }

	    // Check permission and limits
	    if (!authService.hasRealEstateCreateAccess()) {
	        throw new IllegalOperationException("You don't have permission to create properties");
	    }

	    if (!authService.canCreateRealEstate(currentUser.getId())) {
	        throw new LimitationExceededException("Real estate limit exceeded");
	    }

	    // Handle images
	    List<String> imageUrls = Collections.emptyList();
	    if (images != null && images.length > 0) {
	        // ✅ REPLACE: usageTrackingService.validateCanUploadImages(currentUser.getId(), images.length);
	        if (!authService.canUploadImages(currentUser.getId(), images.length)) {
	            throw new LimitationExceededException("Image upload limit exceeded");
	        }
	        imageUrls = realEstateImageService.uploadRealEstateImages(images);
	    }

	    // Create property with investor context
	    RealEstate entity = realEstateMapper.toEntity(createDto, currentUser, imageUrls);
	    entity.setCreatedAt(LocalDate.now());
	    entity.setIsActive(true);

	    // Mark as investment property (you might want to add this field to RealEstate entity)
	    // entity.setInvestmentProperty(true);

	    RealEstate saved = realEstateRepository.save(entity);
	    logger.info("✅ Investment property created for investor: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
	    
	    return realEstateMapper.toResponseDto(saved);
	}
    
    // Get investor properties
    public List<RealEstateResponseDTO> getInvestorProperties(Long investorId) {
        User currentUser = userService.getAuthenticatedUser();
        User investor = userRepository.findById(investorId)
                .orElseThrow(() -> new UserNotFoundException(investorId));

        // Verify the user is viewing their own properties or is admin
        if (!currentUser.getId().equals(investorId) && !authService.hasRole("ADMIN")) {
            throw new IllegalOperationException("You can only view your own investment properties");
        }

        // Verify the user is actually an investor
        if (!investor.isInvestor()) {
            throw new IllegalOperationException("User is not an investor");
        }

        List<RealEstate> properties = realEstateRepository.findByUserId(investorId);
        return properties.stream()
                .map(realEstateMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    // Investment property analytics
    public Map<String, Object> getInvestmentPortfolioStats(Long investorId) {
        User currentUser = userService.getAuthenticatedUser();
        
        // Authorization check
        if (!currentUser.getId().equals(investorId) && !authService.hasRole("ADMIN")) {
            throw new IllegalOperationException("You can only view your own portfolio stats");
        }

        List<RealEstateResponseDTO> properties = getInvestorProperties(investorId);
        
        long totalProperties = properties.size();
        BigDecimal totalPortfolioValue = properties.stream()
                .map(RealEstateResponseDTO::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long activeProperties = properties.stream()
                .filter(property -> property.getIsActive() != null && property.getIsActive())
                .count();

        // Group by property type
        Map<PropertyType, Long> propertiesByType = properties.stream()
                .collect(Collectors.groupingBy(RealEstateResponseDTO::getPropertyType, Collectors.counting()));

        // Group by city
        Map<String, Long> propertiesByCity = properties.stream()
                .collect(Collectors.groupingBy(RealEstateResponseDTO::getCity, Collectors.counting()));

        return Map.of(
            "totalProperties", totalProperties,
            "activeProperties", activeProperties,
            "inactiveProperties", totalProperties - activeProperties,
            "totalPortfolioValue", totalPortfolioValue,
            "averagePropertyValue", totalProperties > 0 ? 
                totalPortfolioValue.divide(BigDecimal.valueOf(totalProperties), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
            "propertiesByType", propertiesByType,
            "propertiesByCity", propertiesByCity,
            "portfolioHealth", calculatePortfolioHealth(properties)
        );
    }
    
    private String calculatePortfolioHealth(List<RealEstateResponseDTO> properties) {
        if (properties.isEmpty()) return "EMPTY";
        
        long activeCount = properties.stream()
                .filter(property -> property.getIsActive() != null && property.getIsActive())
                .count();
        
        double activeRatio = (double) activeCount / properties.size();
        
        if (activeRatio >= 0.8) return "EXCELLENT";
        if (activeRatio >= 0.6) return "GOOD";
        if (activeRatio >= 0.4) return "FAIR";
        return "NEEDS_ATTENTION";
    }
	
	/* TODO - test these scenarios:
	 * Try creating more than 3 real estates as a FREE_USER
	 * Try uploading more than 20 images total as a FREE_USER
	 * Try uploading more than 5 images to a single listing as a FREE_USER
	 * */
}