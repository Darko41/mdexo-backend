package com.doublez.backend.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

import com.doublez.backend.dto.RealEstateCreateDTO;
import com.doublez.backend.dto.RealEstateDTO;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.request.RealEstateRequest;

import jakarta.persistence.EntityNotFoundException;

@Component
public class RealEstateMapper {
    private final UserRepository userRepository;

    public RealEstateMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RealEstate toEntity(RealEstateCreateDTO createDto, User owner, List<String> images) {
        RealEstate entity = new RealEstate();
        mapCommonFields(createDto, entity);
        
        entity.setOwner(owner);
        entity.setImages(images != null ? images : new ArrayList<>());
        
        return entity;
    }
    
    public RealEstate toEntity(RealEstateCreateDTO createDto, User owner) {
    	return toEntity(createDto, owner, null);
    }

    public RealEstateResponseDTO toResponseDto(RealEstate entity) {
        if (entity == null) return null;
        
        return new RealEstateResponseDTO(entity);
    }

    public void updateEntity(RealEstateUpdateDTO updateDto, RealEstate entity) {
        if (updateDto == null || entity == null) return;

        Optional.ofNullable(updateDto.getTitle()).ifPresent(entity::setTitle);
        Optional.ofNullable(updateDto.getDescription()).ifPresent(entity::setDescription);
        Optional.ofNullable(updateDto.getPropertyType()).ifPresent(entity::setPropertyType);
        Optional.ofNullable(updateDto.getListingType()).ifPresent(entity::setListingType);
        Optional.ofNullable(updateDto.getPrice()).ifPresent(entity::setPrice);
        Optional.ofNullable(updateDto.getAddress()).ifPresent(entity::setAddress);
        Optional.ofNullable(updateDto.getCity()).ifPresent(entity::setCity);
        Optional.ofNullable(updateDto.getState()).ifPresent(entity::setState);
        Optional.ofNullable(updateDto.getZipCode()).ifPresent(entity::setZipCode);
        Optional.ofNullable(updateDto.getSizeInSqMt()).ifPresent(entity::setSizeInSqMt);
        Optional.ofNullable(updateDto.getFeatures()).ifPresent(entity::setFeatures);
        Optional.ofNullable(updateDto.getImages()).ifPresent(entity::setImages);
        
        entity.setUpdatedAt(LocalDate.now());
    }

    private void mapCommonFields(RealEstateCreateDTO source, RealEstate target) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setPropertyType(source.getPropertyType());
        target.setListingType(source.getListingType());
        target.setPrice(source.getPrice());
        target.setAddress(source.getAddress());
        target.setCity(source.getCity());
        target.setState(source.getState());
        target.setZipCode(source.getZipCode());
        target.setSizeInSqMt(source.getSizeInSqMt());
        target.setFeatures(source.getFeatures() != null ? source.getFeatures() : new ArrayList<>());
        target.setImages(source.getImages() != null ? source.getImages() : new ArrayList<>());
    }
}
