package com.doublez.backend.service.realestate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.mapper.RealEstateMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminRealEstateService {
    
    private final RealEstateRepository realEstateRepository;
    private final RealEstateMapper realEstateMapper;
    private final UserRepository userRepository;
    private final RealEstateImageService realEstateImageService;

    public AdminRealEstateService(RealEstateRepository realEstateRepository,
                                RealEstateMapper realEstateMapper,
                                UserRepository userRepository,
                                RealEstateImageService realEstateImageService) {
        this.realEstateRepository = realEstateRepository;
        this.realEstateMapper = realEstateMapper;
        this.userRepository = userRepository;
        this.realEstateImageService = realEstateImageService;
    }

    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));
        
        // Admin can update any property without ownership check
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
        RealEstate entity = realEstateRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Real estate not found with id: " + propertyId));
        
        // Delete associated images from S3
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            realEstateImageService.deleteImages(entity.getImages());
        }
        
        // Delete the property
        realEstateRepository.delete(entity);
    }

    public List<RealEstateResponseDTO> getAllRealEstates() {
        return realEstateRepository.findAll().stream()
                .map(realEstateMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public RealEstateResponseDTO getRealEstateById(Long propertyId) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Real estate not found with id: " + propertyId));
        return realEstateMapper.toResponseDto(realEstate);
    }
}