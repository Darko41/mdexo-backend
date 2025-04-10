package com.doublez.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.RealEstateNotFoundException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UnauthorizedAccessException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.specification.RealEstateSpecifications;

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

    public Page<RealEstateResponseDTO> searchRealEstates(BigDecimal priceMin, 
                                                        BigDecimal priceMax,
                                                        PropertyType propertyType, 
                                                        List<String> features,
                                                        String city, 
                                                        String state, 
                                                        String zipCode,
                                                        ListingType listingType,
                                                        Pageable pageable) {
        Specification<RealEstate> spec = buildSearchSpecification(
            priceMin, priceMax, propertyType, features, city, state, zipCode, listingType);

        return realEstateRepository.findAll(spec, pageable)
                .map(realEstateMapper::toResponseDto);
    }

    public RealEstateResponseDTO createRealEstate(RealEstateCreateDTO createDto) {
        User owner = userService.getAuthenticatedUser();
        RealEstate entity = realEstateMapper.toEntity(createDto);
        entity.setOwner(owner);
        RealEstate saved = realEstateRepository.save(entity);
        return realEstateMapper.toResponseDto(saved);
    }

    public RealEstateResponseDTO createWithImages(RealEstateCreateDTO createDto, 
                                                MultipartFile[] images) {
        User owner = userService.getAuthenticatedUser();
        List<String> imageUrls = realEstateImageService.uploadRealEstateImages(images);
        
        RealEstate entity = realEstateMapper.toEntity(createDto);
        entity.setOwner(owner);
        entity.setImages(imageUrls);
        
        RealEstate saved = realEstateRepository.save(entity);
        return realEstateMapper.toResponseDto(saved);
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

    private Specification<RealEstate> buildSearchSpecification(BigDecimal priceMin,
                                                            BigDecimal priceMax,
                                                            PropertyType propertyType,
                                                            List<String> features,
                                                            String city,
                                                            String state,
                                                            String zipCode,
                                                            ListingType listingType) {
        return Specification.where(RealEstateSpecifications.hasMinPrice(priceMin))
                .and(RealEstateSpecifications.hasMaxPrice(priceMax))
                .and(RealEstateSpecifications.hasPropertyType(propertyType))
                .and(RealEstateSpecifications.hasFeatures(features))
                .and(RealEstateSpecifications.hasLocation(city, state, zipCode))
                .and(RealEstateSpecifications.hasListingType(listingType));
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
}