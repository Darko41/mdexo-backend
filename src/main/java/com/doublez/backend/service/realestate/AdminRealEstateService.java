package com.doublez.backend.service.realestate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.FurnitureStatus;
import com.doublez.backend.enums.property.PropertyType;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminRealEstateService {
    
    private final RealEstateRepository realEstateRepository;
    private final RealEstateMapper realEstateMapper;
    private final UserRepository userRepository;
    private final RealEstateImageService realEstateImageService;
    private static final Logger logger = LoggerFactory.getLogger(AdminRealEstateService.class);

    public AdminRealEstateService(RealEstateRepository realEstateRepository,
                                RealEstateMapper realEstateMapper,
                                UserRepository userRepository,
                                RealEstateImageService realEstateImageService) {
        this.realEstateRepository = realEstateRepository;
        this.realEstateMapper = realEstateMapper;
        this.userRepository = userRepository;
        this.realEstateImageService = realEstateImageService;
    }

    // ENHANCED: Update with proper validation and furniture status support
    @Transactional
    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto, 
                                                 MultipartFile[] newImages, List<String> imagesToRemove) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        // Validate update DTO
        validateRealEstateUpdateDTO(updateDto);
        
        // Handle image removal
        if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
            removeImagesFromProperty(realEstate, imagesToRemove);
        }
        
        // Handle new image uploads
        if (newImages != null && newImages.length > 0) {
            addImagesToProperty(realEstate, newImages, updateDto.getReplaceImages());
        }
        
        // Handle owner update
        if (updateDto.getOwnerId() != null) {
            User owner = userRepository.findById(updateDto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + updateDto.getOwnerId()));
            realEstate.setOwner(owner);
        }
        
        // Update other fields
        realEstateMapper.updateEntity(updateDto, realEstate);
        
        // Handle furniture status update
        if (updateDto.getFurnitureStatus() != null) {
            mapFurnitureStatus(updateDto.getFurnitureStatus(), realEstate);
        }
        
        realEstate.setUpdatedAt(LocalDateTime.now());
        
        RealEstate updated = realEstateRepository.save(realEstate);
        logger.info("✅ Admin updated real estate ID: {}", propertyId);
        return realEstateMapper.toResponseDto(updated);
    }
    
    // NEW: Validation method for update DTO
    private void validateRealEstateUpdateDTO(RealEstateUpdateDTO updateDto) {
        if (!updateDto.hasValidOtherDescriptions()) {
            throw new ValidationException("Other descriptions are required when selecting 'OTHER' for enum fields");
        }
        
        if (!updateDto.isSubtypeValid()) {
            throw new ValidationException("Property subtype does not match property type");
        }
        
        if (!updateDto.isDiscountValid()) {
            throw new ValidationException("Invalid discount configuration");
        }
        
        if (!updateDto.isDiscountEndDateValid()) {
            throw new ValidationException("Discount end date must be in the future");
        }
    }
    
    // NEW: Furniture status mapping
    private void mapFurnitureStatus(FurnitureStatus furnitureStatus, RealEstate entity) {
        if (furnitureStatus != null) {
            switch (furnitureStatus) {
                case FURNISHED:
                    entity.setIsFurnished(true);
                    entity.setIsSemiFurnished(false);
                    break;
                case SEMI_FURNISHED:
                    entity.setIsFurnished(false);
                    entity.setIsSemiFurnished(true);
                    break;
                case PARTIALLY_FURNISHED:
                    entity.setIsFurnished(true);
                    entity.setIsSemiFurnished(true);
                    break;
                case UNFURNISHED:
                    entity.setIsFurnished(false);
                    entity.setIsSemiFurnished(false);
                    break;
            }
        }
    }
    
    // NEW: Enhanced image removal
    private void removeImagesFromProperty(RealEstate realEstate, List<String> imagesToRemove) {
        List<String> currentImages = new ArrayList<>(realEstate.getImages());
        List<String> imagesRemoved = new ArrayList<>();
        
        for (String imageUrl : imagesToRemove) {
            if (currentImages.remove(imageUrl)) {
                imagesRemoved.add(imageUrl);
            }
        }
        
        if (!imagesRemoved.isEmpty()) {
            realEstate.setImages(currentImages);
            // Delete from S3
            realEstateImageService.deleteImages(imagesRemoved);
            logger.info("✅ Admin removed {} images from property ID: {}", imagesRemoved.size(), realEstate.getPropertyId());
        }
    }
    
    // NEW: Enhanced image addition
    private void addImagesToProperty(RealEstate realEstate, MultipartFile[] newImages, Boolean replaceImages) {
        boolean shouldReplace = Boolean.TRUE.equals(replaceImages);
        
        if (shouldReplace) {
            // Replace all images
            List<String> oldImages = new ArrayList<>(realEstate.getImages());
            List<String> newImageUrls = realEstateImageService.uploadRealEstateImages(newImages);
            realEstate.setImages(newImageUrls);
            
            // Delete old images from S3
            if (!oldImages.isEmpty()) {
                realEstateImageService.deleteImages(oldImages);
            }
            logger.info("✅ Admin replaced all images for property ID: {}", realEstate.getPropertyId());
        } else {
            // Add to existing images
            List<String> newImageUrls = realEstateImageService.uploadRealEstateImages(newImages);
            List<String> allImages = new ArrayList<>(realEstate.getImages());
            allImages.addAll(newImageUrls);
            realEstate.setImages(allImages);
            logger.info("✅ Admin added {} images to property ID: {}", newImages.length, realEstate.getPropertyId());
        }
    }

    @Transactional
    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto, MultipartFile[] images) {
        return updateRealEstate(propertyId, updateDto, images, null);
    }
    
    // NEW: Bulk update methods
    @Transactional
    public void bulkUpdateStatus(List<Long> propertyIds, Boolean isActive, Boolean isFeatured) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            throw new IllegalArgumentException("Property IDs cannot be null or empty");
        }
        
        List<RealEstate> properties = realEstateRepository.findAllById(propertyIds);
        
        if (properties.size() != propertyIds.size()) {
            throw new ResourceNotFoundException("Some properties not found");
        }
        
        for (RealEstate property : properties) {
            if (isActive != null) {
                property.setIsActive(isActive);
            }
            if (isFeatured != null) {
                property.setIsFeatured(isFeatured);
                if (isFeatured) {
                    property.setFeaturedAt(LocalDateTime.now());
                } else {
                    property.setFeaturedAt(null);
                    property.setFeaturedUntil(null);
                }
            }
            property.preUpdate();
        }
        
        realEstateRepository.saveAll(properties);
        logger.info("✅ Admin bulk updated {} properties", properties.size());
    }
    
    // NEW: Feature property with duration
    @Transactional
    public void featureProperty(Long propertyId, Integer featuredDays) {
        RealEstate property = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        property.setFeatured(true, featuredDays);
        property.preUpdate();
        realEstateRepository.save(property);
        
        logger.info("✅ Admin featured property ID: {} for {} days", propertyId, featuredDays);
    }
    
    // NEW: Unfeature property
    @Transactional
    public void unfeatureProperty(Long propertyId) {
        RealEstate property = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        property.setIsFeatured(false);
        property.setFeaturedAt(null);
        property.setFeaturedUntil(null);
        property.preUpdate();
        realEstateRepository.save(property);
        
        logger.info("✅ Admin unfeatured property ID: {}", propertyId);
    }

    // ENHANCED: Delete with better logging and validation
    public void deleteRealEstate(Long propertyId) {
        RealEstate entity = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        // Log property details before deletion
        logger.info("🗑️ Admin deleting property ID: {}, Title: {}, Owner: {}", 
                   propertyId, entity.getTitle(), 
                   entity.getOwner() != null ? entity.getOwner().getEmail() : "Unknown");
        
        // Delete associated images from S3 FIRST
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            logger.info("🗑️ Deleting {} images from S3 for property {}", entity.getImages().size(), propertyId);
            realEstateImageService.deleteImages(entity.getImages());
        }
        
        // Then delete the property from database
        realEstateRepository.delete(entity);
        logger.info("✅ Admin successfully deleted real estate with ID: {}", propertyId);
    }
    
    // NEW: Bulk delete
    @Transactional
    public void bulkDeleteRealEstates(List<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            throw new IllegalArgumentException("Property IDs cannot be null or empty");
        }
        
        List<RealEstate> properties = realEstateRepository.findAllById(propertyIds);
        
        if (properties.size() != propertyIds.size()) {
            throw new ResourceNotFoundException("Some properties not found");
        }
        
        // Delete images first
        for (RealEstate property : properties) {
            if (property.getImages() != null && !property.getImages().isEmpty()) {
                realEstateImageService.deleteImages(property.getImages());
            }
        }
        
        // Then delete properties
        realEstateRepository.deleteAll(properties);
        logger.info("✅ Admin bulk deleted {} properties", properties.size());
    }

    // ENHANCED: Get all with pagination
    public Page<RealEstateResponseDTO> getAllRealEstates(Pageable pageable) {
        return realEstateRepository.findAll(pageable)
                .map(realEstateMapper::toResponseDto);
    }
    
    // Keep existing method for backward compatibility
    public List<RealEstateResponseDTO> getAllRealEstates() {
        return realEstateRepository.findAll().stream()
                .map(realEstateMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    // NEW: Get with filters
    public Page<RealEstateResponseDTO> getRealEstatesWithFilters(Boolean isActive, Boolean isFeatured, 
                                                                PropertyType propertyType, ListingType listingType,
                                                                Pageable pageable) {
        Specification<RealEstate> spec = Specification.where(null);
        
        if (isActive != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isActive"), isActive));
        }
        if (isFeatured != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isFeatured"), isFeatured));
        }
        if (propertyType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("propertyType"), propertyType));
        }
        if (listingType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("listingType"), listingType));
        }
        
        return realEstateRepository.findAll(spec, pageable)
                .map(realEstateMapper::toResponseDto);
    }

    public RealEstateResponseDTO getRealEstateById(Long propertyId) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Real estate not found with id: " + propertyId));
        return realEstateMapper.toResponseDto(realEstate);
    }
    
    // NEW: Get real estate with detailed analytics
    public RealEstateResponseDTO getRealEstateWithAnalytics(Long propertyId) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        // Increment view count for admin views too
        realEstate.incrementViewCount();
        realEstateRepository.save(realEstate);
        
        return realEstateMapper.toResponseDto(realEstate);
    }
    
    // NEW: Admin analytics
    public Map<String, Object> getAdminAnalytics() {
        Map<String, Object> analytics = new LinkedHashMap<>();
        
        // Basic counts
        analytics.put("totalProperties", realEstateRepository.count());
        analytics.put("activeProperties", realEstateRepository.countByIsActive(true));
        analytics.put("inactiveProperties", realEstateRepository.countByIsActive(false));
        analytics.put("featuredProperties", realEstateRepository.countByIsFeatured(true));
        analytics.put("forSaleProperties", realEstateRepository.countByListingType(ListingType.FOR_SALE));
        analytics.put("forRentProperties", realEstateRepository.countByListingType(ListingType.FOR_RENT));
        
        // Property type distribution
        List<Object[]> typeCounts = realEstateRepository.countByPropertyType();
        Map<String, Long> propertiesByType = typeCounts.stream()
                .collect(Collectors.toMap(
                    obj -> ((PropertyType) obj[0]).name(),
                    obj -> (Long) obj[1]
                ));
        analytics.put("propertiesByType", propertiesByType);
        
        // City distribution
        List<Object[]> cityCounts = realEstateRepository.countByCity();
        Map<String, Long> propertiesByCity = cityCounts.stream()
                .collect(Collectors.toMap(
                    obj -> (String) obj[0],
                    obj -> (Long) obj[1]
                ));
        analytics.put("propertiesByCity", propertiesByCity);
        
        // Recent activity
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<RealEstate> recentProperties = realEstateRepository.findByCreatedAtBetween(
            weekAgo, LocalDateTime.now());
        analytics.put("propertiesAddedLast7Days", recentProperties.size());
        
        return analytics;
    }
    
    // NEW: Transfer property ownership
    @Transactional
    public void transferPropertyOwnership(Long propertyId, Long newOwnerId) {
        RealEstate property = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + newOwnerId));
        
        User oldOwner = property.getOwner();
        property.setOwner(newOwner);
        property.preUpdate();
        realEstateRepository.save(property);
        
        logger.info("✅ Admin transferred property ID: {} from {} to {}", 
                   propertyId, 
                   oldOwner != null ? oldOwner.getEmail() : "Unknown", 
                   newOwner.getEmail());
    }
    
    // NEW: Reassign multiple properties
    @Transactional
    public void bulkTransferOwnership(List<Long> propertyIds, Long newOwnerId) {
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + newOwnerId));
        
        for (Long propertyId : propertyIds) {
            RealEstate property = realEstateRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
            
            property.setOwner(newOwner);
            property.preUpdate();
        }
        
        logger.info("✅ Admin transferred {} properties to user: {}", propertyIds.size(), newOwner.getEmail());
    }
}