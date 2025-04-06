package com.doublez.backend.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.RealEstateDTO;
import com.doublez.backend.dto.RealEstateRequest;
import com.doublez.backend.dto.RealEstateResponse;
import com.doublez.backend.dto.RealEstateUpdateRequest;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.UserRepository;

@Component
public class RealEstateMapper {
    private final UserRepository userRepository;

    public RealEstateMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RealEstate toEntity(RealEstateRequest request) {
    	RealEstate entity = new RealEstate();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPropertyType(request.getPropertyType());
        entity.setListingType(request.getListingType());
        entity.setPrice(request.getPrice());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setZipCode(request.getZipCode());
        entity.setSizeInSqMt(request.getSizeInSqMt());
        entity.setFeatures(request.getFeatures());
        return entity;
    }
	
    public RealEstateResponse toResponse(RealEstate entity) {
        RealEstateResponse response = new RealEstateResponse();
        response.setPropertyId(entity.getPropertyId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPropertyType(entity.getPropertyType());
        response.setListingType(entity.getListingType());
        response.setPrice(entity.getPrice());
        response.setAddress(entity.getAddress());
        response.setCity(entity.getCity());
        response.setState(entity.getState());
        response.setZipCode(entity.getZipCode());
        response.setSizeInSqMt(entity.getSizeInSqMt());
        response.setFeatures(entity.getFeatures());
        response.setImages(entity.getImages());
        response.setOwnerId(entity.getOwner().getId());
        response.setOwnerEmail(entity.getOwner().getEmail());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
    
    public void updateEntity(RealEstateUpdateRequest updates, RealEstate entity) {
        if (updates.getTitle() != null) entity.setTitle(updates.getTitle());
        if (updates.getDescription() != null) entity.setDescription(updates.getDescription());
        if (updates.getPropertyType() != null) entity.setPropertyType(updates.getPropertyType());
        if (updates.getListingType() != null) entity.setListingType(updates.getListingType());
        if (updates.getPrice() != null) entity.setPrice(updates.getPrice());
        if (updates.getAddress() != null) entity.setAddress(updates.getAddress());
        if (updates.getCity() != null) entity.setCity(updates.getCity());
        if (updates.getState() != null) entity.setState(updates.getState());
        if (updates.getZipCode() != null) entity.setZipCode(updates.getZipCode());
        if (updates.getSizeInSqMt() != null) entity.setSizeInSqMt(updates.getSizeInSqMt());
        if (updates.getFeatures() != null) entity.setFeatures(updates.getFeatures());
        if (updates.getImages() != null) entity.setImages(updates.getImages());
        
        entity.setUpdatedAt(LocalDate.now()); // Fixed to use LocalDate
    }
}
