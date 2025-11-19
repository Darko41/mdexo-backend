package com.doublez.backend.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.request.RealEstateRequest;

import jakarta.persistence.EntityNotFoundException;

@Component
public class RealEstateMapper {

    // 🆕 UPDATE toEntity METHOD:
    public RealEstate toEntity(RealEstateCreateDTO createDto, User owner, List<String> imageUrls) {
        RealEstate entity = new RealEstate();
        entity.setTitle(createDto.getTitle());
        entity.setDescription(createDto.getDescription());
        entity.setPropertyType(createDto.getPropertyType());
        entity.setListingType(createDto.getListingType());
        entity.setPrice(createDto.getPrice());
        entity.setAddress(createDto.getAddress());
        entity.setCity(createDto.getCity());
        entity.setState(createDto.getState());
        entity.setZipCode(createDto.getZipCode());
        entity.setSizeInSqMt(createDto.getSizeInSqMt());
        entity.setOwner(owner);
        entity.setIsActive(true); // 🆕 ADD THIS
        
        // 🆕 ADD AGENCY FIELDS:
        if (createDto.getAgentName() != null) {
            entity.setAgentName(createDto.getAgentName());
        }
        if (createDto.getAgentPhone() != null) {
            entity.setAgentPhone(createDto.getAgentPhone());
        }
        if (createDto.getAgentLicense() != null) {
            entity.setAgentLicense(createDto.getAgentLicense());
        }

        // Existing fields
        if (createDto.getFeatures() != null) {
            entity.setFeatures(new ArrayList<>(createDto.getFeatures()));
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            entity.setImages(new ArrayList<>(imageUrls));
        }
        
        // Additional fields
        if (createDto.getLatitude() != null) entity.setLatitude(createDto.getLatitude());
        if (createDto.getLongitude() != null) entity.setLongitude(createDto.getLongitude());
        if (createDto.getRoomCount() != null) entity.setRoomCount(createDto.getRoomCount());
        if (createDto.getFloor() != null) entity.setFloor(createDto.getFloor());
        if (createDto.getTotalFloors() != null) entity.setTotalFloors(createDto.getTotalFloors());
        if (createDto.getConstructionYear() != null) entity.setConstructionYear(createDto.getConstructionYear());
        if (createDto.getMunicipality() != null) entity.setMunicipality(createDto.getMunicipality());
        if (createDto.getHeatingType() != null) entity.setHeatingType(createDto.getHeatingType());
        if (createDto.getPropertyCondition() != null) entity.setPropertyCondition(createDto.getPropertyCondition());

        return entity;
    }

    // 🆕 UPDATE toResponseDto METHOD:
    public RealEstateResponseDTO toResponseDto(RealEstate entity) {
        RealEstateResponseDTO dto = new RealEstateResponseDTO();
        dto.setPropertyId(entity.getPropertyId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setPropertyType(entity.getPropertyType());
        dto.setListingType(entity.getListingType());
        dto.setPrice(entity.getPrice());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setZipCode(entity.getZipCode());
        dto.setSizeInSqMt(entity.getSizeInSqMt());
        dto.setFeatures(entity.getFeatures());
        dto.setImages(entity.getImages());
        dto.setOwnerId(entity.getOwner().getId());
        dto.setIsActive(entity.getIsActive()); // 🆕 ADD THIS
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // 🆕 ADD AGENCY INFO:
        if (entity.isAgencyProperty()) {
            dto.setAgencyId(entity.getAgency().getId());
            dto.setAgencyName(entity.getAgency().getName());
            dto.setAgentName(entity.getAgentName());
            dto.setAgentPhone(entity.getAgentPhone());
            dto.setAgentLicense(entity.getAgentLicense());
        }

        // Additional fields
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setRoomCount(entity.getRoomCount());
        dto.setFloor(entity.getFloor());
        dto.setTotalFloors(entity.getTotalFloors());
        dto.setConstructionYear(entity.getConstructionYear());
        dto.setMunicipality(entity.getMunicipality());
        dto.setHeatingType(entity.getHeatingType());
        dto.setPropertyCondition(entity.getPropertyCondition());

        return dto;
    }

    // 🆕 UPDATE updateEntity METHOD:
    public void updateEntity(RealEstateUpdateDTO updateDto, RealEstate entity) {
        if (updateDto.getTitle() != null) entity.setTitle(updateDto.getTitle());
        if (updateDto.getDescription() != null) entity.setDescription(updateDto.getDescription());
        if (updateDto.getPropertyType() != null) entity.setPropertyType(updateDto.getPropertyType());
        if (updateDto.getListingType() != null) entity.setListingType(updateDto.getListingType());
        if (updateDto.getPrice() != null) entity.setPrice(updateDto.getPrice());
        if (updateDto.getAddress() != null) entity.setAddress(updateDto.getAddress());
        if (updateDto.getCity() != null) entity.setCity(updateDto.getCity());
        if (updateDto.getState() != null) entity.setState(updateDto.getState());
        if (updateDto.getZipCode() != null) entity.setZipCode(updateDto.getZipCode());
        if (updateDto.getSizeInSqMt() != null) entity.setSizeInSqMt(updateDto.getSizeInSqMt());
        if (updateDto.getFeatures() != null) entity.setFeatures(updateDto.getFeatures());
        
        // 🆕 UPDATE AGENT FIELDS:
        if (updateDto.getAgentName() != null) entity.setAgentName(updateDto.getAgentName());
        if (updateDto.getAgentPhone() != null) entity.setAgentPhone(updateDto.getAgentPhone());
        if (updateDto.getAgentLicense() != null) entity.setAgentLicense(updateDto.getAgentLicense());

        // Additional fields
        if (updateDto.getLatitude() != null) entity.setLatitude(updateDto.getLatitude());
        if (updateDto.getLongitude() != null) entity.setLongitude(updateDto.getLongitude());
        if (updateDto.getRoomCount() != null) entity.setRoomCount(updateDto.getRoomCount());
        if (updateDto.getFloor() != null) entity.setFloor(updateDto.getFloor());
        if (updateDto.getTotalFloors() != null) entity.setTotalFloors(updateDto.getTotalFloors());
        if (updateDto.getConstructionYear() != null) entity.setConstructionYear(updateDto.getConstructionYear());
        if (updateDto.getMunicipality() != null) entity.setMunicipality(updateDto.getMunicipality());
        if (updateDto.getHeatingType() != null) entity.setHeatingType(updateDto.getHeatingType());
        if (updateDto.getPropertyCondition() != null) entity.setPropertyCondition(updateDto.getPropertyCondition());
    }
}