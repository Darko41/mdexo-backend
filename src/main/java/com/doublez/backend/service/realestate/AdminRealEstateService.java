package com.doublez.backend.service.realestate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.user.User;
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

    @Transactional
    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto, 
                                                 MultipartFile[] newImages, List<String> imagesToRemove) {
        RealEstate realEstate = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));
        
        // Handle image removal
        if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
            // Remove from property and S3
            List<String> currentImages = new ArrayList<>(realEstate.getImages());
            currentImages.removeAll(imagesToRemove);
            realEstate.setImages(currentImages);
            
            // Delete from S3
            realEstateImageService.deleteImages(imagesToRemove);
        }
        
        // Handle new image uploads (replace if specified, otherwise add)
        if (newImages != null && newImages.length > 0) {
            boolean shouldReplace = updateDto.getReplaceImages() != null && updateDto.getReplaceImages();
            
            if (shouldReplace) {
                // Replace all images
                List<String> newImageUrls = realEstateImageService.uploadRealEstateImages(newImages);
                realEstate.setImages(newImageUrls);
            } else {
                // Add to existing images
                List<String> newImageUrls = realEstateImageService.uploadRealEstateImages(newImages);
                List<String> allImages = new ArrayList<>(realEstate.getImages());
                allImages.addAll(newImageUrls);
                realEstate.setImages(allImages);
            }
        }
        
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
    
    @Transactional
    public RealEstateResponseDTO updateRealEstate(Long propertyId, RealEstateUpdateDTO updateDto, MultipartFile[] images) {
        // Call the main method with empty imagesToRemove list
        return updateRealEstate(propertyId, updateDto, images, null);
    }

    public void deleteRealEstate(Long propertyId) {
        RealEstate entity = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Real estate not found"));
        
        // Delete associated images from S3 FIRST
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            realEstateImageService.deleteImages(entity.getImages());
        }
        
        // Then delete the property from database
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