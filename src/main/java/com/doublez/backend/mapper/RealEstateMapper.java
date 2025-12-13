package com.doublez.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.property.FurnitureStatus;

@Component
public class RealEstateMapper {

    public RealEstate toEntity(RealEstateCreateDTO createDto, User owner, List<String> imageUrls) {
        RealEstate entity = new RealEstate();
        
        // ===== BASIC INFORMATION =====
        entity.setTitle(createDto.getTitle());
        entity.setDescription(createDto.getDescription());
        entity.setPropertyType(createDto.getPropertyType());
        entity.setPropertySubtype(createDto.getPropertySubtype());
        entity.setListingType(createDto.getListingType());
        
        // ===== PRICE & FINANCIAL =====
        entity.setPrice(createDto.getPrice());
        entity.setCurrency(createDto.getCurrency() != null ? createDto.getCurrency() : "RSD");
        entity.setOriginalPrice(createDto.getOriginalPrice());
        entity.setDiscountAmount(createDto.getDiscountAmount());
        entity.setDiscountEndDate(createDto.getDiscountEndDate());
        entity.setPriceNegotiable(createDto.getPriceNegotiable() != null ? createDto.getPriceNegotiable() : true);
        entity.setIncludesUtilities(createDto.getIncludesUtilities());
        entity.setDepositAmount(createDto.getDepositAmount());
        
        // ===== LOCATION INFORMATION =====
        entity.setAddress(createDto.getAddress());
        entity.setStreetNumber(createDto.getStreetNumber());
        entity.setNeighborhood(createDto.getNeighborhood());
        entity.setCity(createDto.getCity());
        entity.setMunicipality(createDto.getMunicipality());
        entity.setState(createDto.getState() != null ? createDto.getState() : "Srbija");
        entity.setZipCode(createDto.getZipCode());
        entity.setLocationDescription(createDto.getLocationDescription());
        entity.setLatitude(createDto.getLatitude());
        entity.setLongitude(createDto.getLongitude());
        
        // ===== PROPERTY CHARACTERISTICS - CORE =====
        entity.setSizeInSqMt(createDto.getSizeInSqMt());
        entity.setRoomCount(createDto.getRoomCount());
        entity.setBathroomCount(createDto.getBathroomCount());
        entity.setBalconyCount(createDto.getBalconyCount());
        entity.setFloor(createDto.getFloor());
        entity.setTotalFloors(createDto.getTotalFloors());
        entity.setConstructionYear(createDto.getConstructionYear());
        entity.setPropertyCondition(createDto.getPropertyCondition());
        entity.setHeatingType(createDto.getHeatingType());
        entity.setOtherHeatingTypeDescription(createDto.getOtherHeatingTypeDescription());
        
        // FIXED: Convert FurnitureStatus to boolean fields
        mapFurnitureStatusToEntity(createDto.getFurnitureStatus(), entity);
        
        // ===== AMENITIES & COMFORT =====
        entity.setHasElevator(createDto.getHasElevator());
        entity.setHasAirConditioning(createDto.getHasAirConditioning());
        entity.setHasInternet(createDto.getHasInternet());
        entity.setHasCableTV(createDto.getHasCableTV());
        entity.setHasSecurity(createDto.getHasSecurity());
        entity.setHasParking(createDto.getHasParking() != null ? createDto.getHasParking() : false);
        entity.setParkingSpaces(createDto.getParkingSpaces());
        entity.setHasGarden(createDto.getHasGarden());
        entity.setGardenSizeSqMt(createDto.getGardenSizeSqMt());
        entity.setHasTerrace(createDto.getHasTerrace());
        entity.setHasBalcony(createDto.getHasBalcony());
        
        // ===== ENERGY & UTILITIES =====
        entity.setEnergyEfficiency(createDto.getEnergyEfficiency());
        entity.setHasWater(createDto.getHasWater());
        entity.setHasSewage(createDto.getHasSewage());
        entity.setHasElectricity(createDto.getHasElectricity());
        entity.setHasGas(createDto.getHasGas());
        
        // ===== LEGAL & DOCUMENTATION =====
        entity.setHasConstructionPermit(createDto.getHasConstructionPermit());
        entity.setHasUsePermit(createDto.getHasUsePermit());
        entity.setOwnershipType(createDto.getOwnershipType());
        entity.setOtherOwnershipTypeDescription(createDto.getOtherOwnershipTypeDescription());
        entity.setIsRegistered(createDto.getIsRegistered());
        
        // ===== COMMERCIAL-SPECIFIC =====
        entity.setBusinessType(createDto.getBusinessType());
        entity.setHasShowcaseWindow(createDto.getHasShowcaseWindow());
        entity.setHasStorageRoom(createDto.getHasStorageRoom());
        entity.setEmployeeCapacity(createDto.getEmployeeCapacity());
        
        // ===== LAND-SPECIFIC =====
        entity.setLandType(createDto.getLandType());
        entity.setHasWaterSource(createDto.getHasWaterSource());
        entity.setHasElectricityAccess(createDto.getHasElectricityAccess());
        entity.setHasRoadAccess(createDto.getHasRoadAccess());
        
        // ===== OWNERSHIP & AGENCY =====
        entity.setOwner(owner);
        // agencyId is set separately via setAgency() method
        
        // ===== AGENT OVERRIDES =====
        entity.setAgentName(createDto.getAgentName());
        entity.setAgentPhone(createDto.getAgentPhone());
        entity.setAgentLicense(createDto.getAgentLicense());
        entity.setContactEmail(createDto.getContactEmail());
        entity.setPreferredContactMethod(createDto.getPreferredContactMethod());
        
        // ===== AVAILABILITY =====
        entity.setAvailableFrom(createDto.getAvailableFrom());
        entity.setMinimumRentPeriod(createDto.getMinimumRentPeriod());
        
        // ===== MEDIA & FEATURES =====
        if (createDto.getFeatures() != null) {
            entity.setFeatures(new ArrayList<>(createDto.getFeatures()));
        }
        if (imageUrls != null && !imageUrls.isEmpty()) {
            entity.setImages(new ArrayList<>(imageUrls));
        }
        
        // ===== STATUS =====
        entity.setIsActive(createDto.getIsActive() != null ? createDto.getIsActive() : true);
        entity.setIsFeatured(createDto.getIsFeatured() != null ? createDto.getIsFeatured() : false);

        return entity;
    }

    // NEW: Helper method to map FurnitureStatus to entity boolean fields
    private void mapFurnitureStatusToEntity(FurnitureStatus furnitureStatus, RealEstate entity) {
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
                default:
                    entity.setIsFurnished(false);
                    entity.setIsSemiFurnished(false);
                    break;
            }
        } else {
            // Default to unfurnished if not specified
            entity.setIsFurnished(false);
            entity.setIsSemiFurnished(false);
        }
    }

    // NEW: Helper method to map entity boolean fields to FurnitureStatus
    private FurnitureStatus mapEntityToFurnitureStatus(RealEstate entity) {
        Boolean isFurnished = entity.getIsFurnished();
        Boolean isSemiFurnished = entity.getIsSemiFurnished();
        
        if (Boolean.TRUE.equals(isFurnished) && Boolean.TRUE.equals(isSemiFurnished)) {
            return FurnitureStatus.PARTIALLY_FURNISHED;
        } else if (Boolean.TRUE.equals(isFurnished)) {
            return FurnitureStatus.FURNISHED;
        } else if (Boolean.TRUE.equals(isSemiFurnished)) {
            return FurnitureStatus.SEMI_FURNISHED;
        } else {
            return FurnitureStatus.UNFURNISHED;
        }
    }

    public RealEstateResponseDTO toResponseDto(RealEstate entity) {
        // Use the constructor we already have in RealEstateResponseDTO
        return new RealEstateResponseDTO(entity);
    }

    // NEW: Enhanced response DTO method with furniture status mapping
    public RealEstateResponseDTO toDetailedResponseDto(RealEstate entity) {
        RealEstateResponseDTO dto = new RealEstateResponseDTO(entity);
        
        // Map furniture status from entity boolean fields to enum
        FurnitureStatus furnitureStatus = mapEntityToFurnitureStatus(entity);
        // Note: You'll need to add a setFurnitureStatus method in RealEstateResponseDTO
        // dto.setFurnitureStatus(furnitureStatus);
        
        return dto;
    }

    public void updateEntity(RealEstateUpdateDTO updateDto, RealEstate entity) {
        // ===== BASIC INFORMATION =====
        if (updateDto.getTitle() != null) entity.setTitle(updateDto.getTitle());
        if (updateDto.getDescription() != null) entity.setDescription(updateDto.getDescription());
        if (updateDto.getPropertyType() != null) entity.setPropertyType(updateDto.getPropertyType());
        if (updateDto.getPropertySubtype() != null) entity.setPropertySubtype(updateDto.getPropertySubtype());
        if (updateDto.getListingType() != null) entity.setListingType(updateDto.getListingType());
        
        // ===== PRICE & FINANCIAL =====
        if (updateDto.getPrice() != null) entity.setPrice(updateDto.getPrice());
        if (updateDto.getCurrency() != null) entity.setCurrency(updateDto.getCurrency());
        if (updateDto.getOriginalPrice() != null) entity.setOriginalPrice(updateDto.getOriginalPrice());
        if (updateDto.getDiscountAmount() != null) entity.setDiscountAmount(updateDto.getDiscountAmount());
        if (updateDto.getDiscountEndDate() != null) entity.setDiscountEndDate(updateDto.getDiscountEndDate());
        if (updateDto.getPriceNegotiable() != null) entity.setPriceNegotiable(updateDto.getPriceNegotiable());
        if (updateDto.getIncludesUtilities() != null) entity.setIncludesUtilities(updateDto.getIncludesUtilities());
        if (updateDto.getDepositAmount() != null) entity.setDepositAmount(updateDto.getDepositAmount());
        
        // ===== LOCATION INFORMATION =====
        if (updateDto.getAddress() != null) entity.setAddress(updateDto.getAddress());
        if (updateDto.getStreetNumber() != null) entity.setStreetNumber(updateDto.getStreetNumber());
        if (updateDto.getNeighborhood() != null) entity.setNeighborhood(updateDto.getNeighborhood());
        if (updateDto.getCity() != null) entity.setCity(updateDto.getCity());
        if (updateDto.getMunicipality() != null) entity.setMunicipality(updateDto.getMunicipality());
        if (updateDto.getState() != null) entity.setState(updateDto.getState());
        if (updateDto.getZipCode() != null) entity.setZipCode(updateDto.getZipCode());
        if (updateDto.getLocationDescription() != null) entity.setLocationDescription(updateDto.getLocationDescription());
        if (updateDto.getLatitude() != null) entity.setLatitude(updateDto.getLatitude());
        if (updateDto.getLongitude() != null) entity.setLongitude(updateDto.getLongitude());
        
        // ===== PROPERTY CHARACTERISTICS - CORE =====
        if (updateDto.getSizeInSqMt() != null) entity.setSizeInSqMt(updateDto.getSizeInSqMt());
        if (updateDto.getRoomCount() != null) entity.setRoomCount(updateDto.getRoomCount());
        if (updateDto.getBathroomCount() != null) entity.setBathroomCount(updateDto.getBathroomCount());
        if (updateDto.getBalconyCount() != null) entity.setBalconyCount(updateDto.getBalconyCount());
        if (updateDto.getFloor() != null) entity.setFloor(updateDto.getFloor());
        if (updateDto.getTotalFloors() != null) entity.setTotalFloors(updateDto.getTotalFloors());
        if (updateDto.getConstructionYear() != null) entity.setConstructionYear(updateDto.getConstructionYear());
        if (updateDto.getPropertyCondition() != null) entity.setPropertyCondition(updateDto.getPropertyCondition());
        if (updateDto.getHeatingType() != null) entity.setHeatingType(updateDto.getHeatingType());
        if (updateDto.getOtherHeatingTypeDescription() != null) entity.setOtherHeatingTypeDescription(updateDto.getOtherHeatingTypeDescription());
        
        // FIXED: Convert FurnitureStatus to boolean fields
        if (updateDto.getFurnitureStatus() != null) {
            mapFurnitureStatusToEntity(updateDto.getFurnitureStatus(), entity);
        }
        
        // ===== AMENITIES & COMFORT =====
        if (updateDto.getHasElevator() != null) entity.setHasElevator(updateDto.getHasElevator());
        if (updateDto.getHasAirConditioning() != null) entity.setHasAirConditioning(updateDto.getHasAirConditioning());
        if (updateDto.getHasInternet() != null) entity.setHasInternet(updateDto.getHasInternet());
        if (updateDto.getHasCableTV() != null) entity.setHasCableTV(updateDto.getHasCableTV());
        if (updateDto.getHasSecurity() != null) entity.setHasSecurity(updateDto.getHasSecurity());
        if (updateDto.getHasParking() != null) entity.setHasParking(updateDto.getHasParking());
        if (updateDto.getParkingSpaces() != null) entity.setParkingSpaces(updateDto.getParkingSpaces());
        if (updateDto.getHasGarden() != null) entity.setHasGarden(updateDto.getHasGarden());
        if (updateDto.getGardenSizeSqMt() != null) entity.setGardenSizeSqMt(updateDto.getGardenSizeSqMt());
        if (updateDto.getHasTerrace() != null) entity.setHasTerrace(updateDto.getHasTerrace());
        if (updateDto.getHasBalcony() != null) entity.setHasBalcony(updateDto.getHasBalcony());
        
        // ===== ENERGY & UTILITIES =====
        if (updateDto.getEnergyEfficiency() != null) entity.setEnergyEfficiency(updateDto.getEnergyEfficiency());
        if (updateDto.getHasWater() != null) entity.setHasWater(updateDto.getHasWater());
        if (updateDto.getHasSewage() != null) entity.setHasSewage(updateDto.getHasSewage());
        if (updateDto.getHasElectricity() != null) entity.setHasElectricity(updateDto.getHasElectricity());
        if (updateDto.getHasGas() != null) entity.setHasGas(updateDto.getHasGas());
        
        // ===== LEGAL & DOCUMENTATION =====
        if (updateDto.getHasConstructionPermit() != null) entity.setHasConstructionPermit(updateDto.getHasConstructionPermit());
        if (updateDto.getHasUsePermit() != null) entity.setHasUsePermit(updateDto.getHasUsePermit());
        if (updateDto.getOwnershipType() != null) entity.setOwnershipType(updateDto.getOwnershipType());
        if (updateDto.getOtherOwnershipTypeDescription() != null) entity.setOtherOwnershipTypeDescription(updateDto.getOtherOwnershipTypeDescription());
        if (updateDto.getIsRegistered() != null) entity.setIsRegistered(updateDto.getIsRegistered());
        
        // ===== COMMERCIAL-SPECIFIC =====
        if (updateDto.getBusinessType() != null) entity.setBusinessType(updateDto.getBusinessType());
        if (updateDto.getHasShowcaseWindow() != null) entity.setHasShowcaseWindow(updateDto.getHasShowcaseWindow());
        if (updateDto.getHasStorageRoom() != null) entity.setHasStorageRoom(updateDto.getHasStorageRoom());
        if (updateDto.getEmployeeCapacity() != null) entity.setEmployeeCapacity(updateDto.getEmployeeCapacity());
        
        // ===== LAND-SPECIFIC =====
        if (updateDto.getLandType() != null) entity.setLandType(updateDto.getLandType());
        if (updateDto.getHasWaterSource() != null) entity.setHasWaterSource(updateDto.getHasWaterSource());
        if (updateDto.getHasElectricityAccess() != null) entity.setHasElectricityAccess(updateDto.getHasElectricityAccess());
        if (updateDto.getHasRoadAccess() != null) entity.setHasRoadAccess(updateDto.getHasRoadAccess());
        
        // ===== AGENT OVERRIDES =====
        if (updateDto.getAgentName() != null) entity.setAgentName(updateDto.getAgentName());
        if (updateDto.getAgentPhone() != null) entity.setAgentPhone(updateDto.getAgentPhone());
        if (updateDto.getAgentLicense() != null) entity.setAgentLicense(updateDto.getAgentLicense());
        if (updateDto.getContactEmail() != null) entity.setContactEmail(updateDto.getContactEmail());
        if (updateDto.getPreferredContactMethod() != null) entity.setPreferredContactMethod(updateDto.getPreferredContactMethod());
        
        // ===== AVAILABILITY =====
        if (updateDto.getAvailableFrom() != null) entity.setAvailableFrom(updateDto.getAvailableFrom());
        if (updateDto.getMinimumRentPeriod() != null) entity.setMinimumRentPeriod(updateDto.getMinimumRentPeriod());
        
        // ===== MEDIA & FEATURES =====
        if (updateDto.getFeatures() != null) entity.setFeatures(updateDto.getFeatures());
        
        // ===== STATUS =====
        if (updateDto.getIsActive() != null) entity.setIsActive(updateDto.getIsActive());
        if (updateDto.getIsFeatured() != null) entity.setIsFeatured(updateDto.getIsFeatured());
        if (updateDto.getFeaturedAt() != null) entity.setFeaturedAt(updateDto.getFeaturedAt());
        if (updateDto.getFeaturedUntil() != null) entity.setFeaturedUntil(updateDto.getFeaturedUntil());
    }

    // NEW: Method to convert entity boolean fields to FurnitureStatus enum
    public FurnitureStatus getFurnitureStatusFromEntity(RealEstate entity) {
        return mapEntityToFurnitureStatus(entity);
    }
}