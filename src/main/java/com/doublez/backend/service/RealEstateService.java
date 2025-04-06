package com.doublez.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.catalina.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.RealEstateDTO;
import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponse;
import com.doublez.backend.dto.RealEstateUpdateRequest;
import com.doublez.backend.entity.ListingType;
import com.doublez.backend.entity.PropertyType;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.RealEstateNotFoundException;
import com.doublez.backend.exception.UnauthorizedAccessException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.specification.RealEstateSpecifications;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RealEstateService {

	private final RealEstateRepository realEstateRepository;
	private final UserService userService;
	private final RealEstateImageService realEstateImageService;
	private final RealEstateMapper realEstateMapper;

	public RealEstateService(RealEstateRepository realEstateRepository, UserService userService,
			RealEstateImageService realEstateImageService, RealEstateMapper realEstateMapper) {
		this.realEstateRepository = realEstateRepository;
		this.userService = userService;
		this.realEstateImageService = realEstateImageService;
		this.realEstateMapper = realEstateMapper;
	}

	// Search with pagination and filtering
	public Page<RealEstateResponse> searchRealEstates(BigDecimal priceMin, BigDecimal priceMax,
			PropertyType propertyType, List<String> features, String city, String state, String zipCode,
			ListingType listingType, Pageable pageable) {
		
		Specification<RealEstate> spec = buildSearchSpecification(priceMin, priceMax, propertyType, features, city,
				state, zipCode, listingType);

		return realEstateRepository.findAll(spec, pageable)
	            .map(entity -> realEstateMapper.toResponse(entity));
	}

	// Get single property by ID
	public RealEstateResponse getById(Long id) {
		RealEstate entity = realEstateRepository.findById(id)
	            .orElseThrow(() -> new RealEstateNotFoundException(id));
	    return realEstateMapper.toResponse(entity);
	}

	// Create from DTO without images
	public RealEstateResponse createRealEstate(RealEstateRequest request) {
		User owner = userService.getAuthenticatedUser();
		RealEstate entity = realEstateMapper.toEntity(request);
		entity.setOwner(owner);
		RealEstate saved = realEstateRepository.save(entity);
		return realEstateMapper.toResponse(saved);
	}

	// Create with images
	public RealEstateResponse createWithImages(RealEstateRequest request, MultipartFile[] images) {
	    User owner = userService.getAuthenticatedUser();
	    List<String> imageUrls = realEstateImageService.uploadRealEstateImages(images);
	    
	    RealEstate entity = realEstateMapper.toEntity(request);
	    entity.setOwner(owner);
	    entity.setImages(imageUrls);
	    
	    RealEstate saved = realEstateRepository.save(entity);
	    return realEstateMapper.toResponse(saved);
	}

	// Update property
	public RealEstateResponse updateRealEstate(Long propertyId, RealEstateUpdateRequest updates) {
	    RealEstate entity = getValidatedRealEstate(propertyId);
	    realEstateMapper.updateEntity(updates, entity);
	    entity.setUpdatedAt(LocalDate.now());
	    
	    RealEstate updated = realEstateRepository.save(entity);
	    return realEstateMapper.toResponse(updated);
	}

	// Delete property
	public void deleteRealEstate(Long propertyId) {
		RealEstate entity = getValidatedRealEstate(propertyId);
		realEstateRepository.delete(entity);
	}

	// Helper methods
	private RealEstate getValidatedRealEstate(Long propertyId) {
	    RealEstate entity = realEstateRepository.findById(propertyId)
	        .orElseThrow(() -> new RealEstateNotFoundException(propertyId));
	    
	    User currentUser = userService.getAuthenticatedUser();
	    if (!entity.getOwner().equals(currentUser)) {
	        throw new UnauthorizedAccessException("You don't own this property");
	    }
	    
	    return entity;
	}

	private Specification<RealEstate> buildSearchSpecification(BigDecimal priceMin, BigDecimal priceMax,
			PropertyType propertyType, List<String> features, String city, String state, String zipCode,
			ListingType listingType) {

		return Specification.where(RealEstateSpecifications.hasMinPrice(priceMin))
				.and(RealEstateSpecifications.hasMaxPrice(priceMax))
				.and(RealEstateSpecifications.hasPropertyType(propertyType))
				.and(RealEstateSpecifications.hasFeatures(features))
				.and(RealEstateSpecifications.hasLocation(city, state, zipCode))
				.and(RealEstateSpecifications.hasListingType(listingType));
	}

	// Statistics methods
	public long getRealEstateCount() {
		return realEstateRepository.count();
	}

	public List<RealEstate> getAllRealEstates() {
		return realEstateRepository.findAll();
	}
}