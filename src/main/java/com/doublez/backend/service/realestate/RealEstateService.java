package com.doublez.backend.service.realestate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.RealEstateNotFoundException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UnauthorizedAccessException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.RealEstateMapper;
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
    private final RealEstateRepository realEstateRepository;
    private final UserService userService;
    private final RealEstateImageService realEstateImageService;
    private final RealEstateMapper realEstateMapper;
    private final UserRepository userRepository;

    public RealEstateService(RealEstateRepository realEstateRepository, 
                           UserService userService,
                           RealEstateImageService realEstateImageService, 
                           RealEstateMapper realEstateMapper,
                           UserRepository userRepository) {
        this.realEstateRepository = realEstateRepository;
        this.userService = userService;
        this.realEstateImageService = realEstateImageService;
        this.realEstateMapper = realEstateMapper;
        this.userRepository = userRepository;
    }

    public Page<RealEstateResponseDTO> searchRealEstates(
            String searchTerm,
            BigDecimal priceMin,
            BigDecimal priceMax,
            PropertyType propertyType,
            List<String> features,
            String city,
            String state,
            String zipCode,
            ListingType listingType,
            Pageable pageable) {
        
        // Build the complete specification
        Specification<RealEstate> spec = buildCompleteSpecification(
            searchTerm, priceMin, priceMax, propertyType, 
            features, city, state, zipCode, listingType);
        
        // Execute the query with proper error handling
        try {
            return realEstateRepository.findAll(spec, pageable)
                .map(realEstateMapper::toResponseDto);
        } catch (Exception e) {
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }
    
    private Specification<RealEstate> buildCompleteSpecification(
            String searchTerm,
            BigDecimal priceMin,
            BigDecimal priceMax,
            PropertyType propertyType,
            List<String> features,
            String city,
            String state,
            String zipCode,
            ListingType listingType) {
        
        return Specification.where(buildTextSearchSpec(searchTerm))
            .and(buildPriceSpec(priceMin, priceMax))
            .and(buildPropertyTypeSpec(propertyType))
            .and(buildFeaturesSpec(features))
            .and(buildLocationSpec(city, state, zipCode))
            .and(buildListingTypeSpec(listingType));
    }
    
    // Text search specification
    private Specification<RealEstate> buildTextSearchSpec(String searchTerm) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(searchTerm)) return null;
            
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
        return (root, query, cb) -> 
            propertyType != null ? cb.equal(root.get("propertyType"), propertyType) : null;
    }

    // Features specification
    private Specification<RealEstate> buildFeaturesSpec(List<String> features) {
        return (root, query, cb) -> {
            if (features == null || features.isEmpty()) return null;
            
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
        return (root, query, cb) -> 
            listingType != null ? cb.equal(root.get("listingType"), listingType) : null;
    }

    public RealEstateResponseDTO createRealEstate(RealEstateCreateDTO createDto, MultipartFile[] images) {
        User currentUser = userService.getAuthenticatedUser();
        User owner = resolveOwner(createDto.getOwnerId(), currentUser);
        
        // Handle image upload if provided
        List<String> imageUrls = Collections.emptyList();
        if (images != null && images.length > 0) {
            imageUrls = realEstateImageService.uploadRealEstateImages(images);
        }
        
        RealEstate entity = realEstateMapper.toEntity(createDto, owner, imageUrls);
        entity.setCreatedAt(LocalDate.now());
        
        RealEstate saved = realEstateRepository.save(entity);
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
        
        return userRepository.findById(ownerId)
               .orElseThrow(() -> new UserNotFoundException(ownerId));
    }
    
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public RealEstateResponseDTO createRealEstateForUser(RealEstateCreateDTO createDto, MultipartFile[] images) {
        User currentUser = userService.getAuthenticatedUser();
        
        User owner = (createDto.getOwnerId() != null && isAdminOrAgent(currentUser))
                ? userService.getUserEntityById(createDto.getOwnerId())
                : currentUser;
        
        // Handle image upload if provided
        List<String> imageUrls = Collections.emptyList();
        if (images != null && images.length > 0) {
            imageUrls = realEstateImageService.uploadRealEstateImages(images);
        }
        
        RealEstate entity = realEstateMapper.toEntity(createDto, owner, imageUrls);
        entity.setCreatedAt(LocalDate.now());
        RealEstate saved = realEstateRepository.save(entity);
        return realEstateMapper.toResponseDto(saved);
    }


	private boolean isAdminOrAgent(User user) {
		return user.getRoles().stream()
				.anyMatch(role ->
						"ROLE_ADMIN".equals(role.getName()) ||
						"ROLE_AGENT".equals(role.getName()));
	}

    @Transactional
    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));
        
        // Handle owner update
        if (updateDto.getOwnerId() != null) {
            User owner = userRepository.findById(updateDto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + updateDto.getOwnerId()));
            realEstate.setOwner(owner);
        }
        
        // Update other fields
        realEstateMapper.updateEntity(updateDto, realEstate);
        realEstate.setUpdatedAt(LocalDate.now());
        
        RealEstate updated = realEstateRepository.save(realEstate);
        return realEstateMapper.toResponseDto(updated);
    }

    public void deleteRealEstate(Long propertyId) {
    	RealEstate entity = getValidatedRealEstate(propertyId);
        
        // Delete associated images first
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            realEstateImageService.deleteImages(entity.getImages());
        }
        
        // Then delete the property
        realEstateRepository.delete(entity);
    }

    public long getRealEstateCount() {
        return realEstateRepository.count();
    }

    public List<RealEstateResponseDTO> getAllRealEstates() {
        return realEstateRepository.findAll().stream()
                .map(realEstateMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private RealEstate getValidatedRealEstate(Long propertyId) {
        RealEstate entity = realEstateRepository.findById(propertyId)
            .orElseThrow(() -> new RealEstateNotFoundException(propertyId));
        
        User currentUser = userService.getAuthenticatedUser();
        if (!entity.getOwner().equals(currentUser)) {
            throw new UnauthorizedAccessException(
                "You don't have permission to modify this property");
        }
        
        return entity;
    }

    public RealEstateResponseDTO getRealEstateById(Long propertyId) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Real estate not found with id: " + propertyId));
        return new RealEstateResponseDTO(realEstate);
    }
    
    public RealEstate addImagesToProperty(Long propertyId, MultipartFile[] files) {
    	RealEstate property = realEstateRepository.findById(propertyId)
    			.orElseThrow(() -> new ResourceNotFoundException("Property not found"));
    	
    	List<String> imageUrls = realEstateImageService.uploadRealEstateImages(files);
    	property.getImages().addAll(imageUrls);
    	
    	return realEstateRepository.save(property);
    }
    
    public void removeImagesFromProperty(Long propertyId, String imageUrl) {
    	RealEstate property = realEstateRepository.findById(propertyId)
    			.orElseThrow(() -> new ResourceNotFoundException("Property not found"));
    	
    	if (property.getImages().remove(imageUrl)) {
    		realEstateImageService.deleteImages(List.of(imageUrl));
    		realEstateRepository.save(property);
    	}
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
    
}