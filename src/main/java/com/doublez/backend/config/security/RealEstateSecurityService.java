package com.doublez.backend.config.security;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.UserService;

@Service
public class RealEstateSecurityService {

    private final RealEstateRepository realEstateRepository;
    private final UserService userService;

    public RealEstateSecurityService(
        RealEstateRepository realEstateRepository,
        UserService userService
    ) {
        this.realEstateRepository = realEstateRepository;
        this.userService = userService;
    }

    /**
     * Checks if the current authenticated user owns the given property.
     * @param propertyId ID of the property to check
     * @return true if the current user is the owner, false otherwise
     * @throws ResourceNotFoundException if the property doesn't exist
     */
    public boolean isOwner(Long propertyId) {
        RealEstate property = realEstateRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        // Null-safe check
        User owner = property.getOwner();
        if (owner == null) {
            return false; // Or throw an exception if properties must have owners
        }
        
        User currentUser = userService.getAuthenticatedUser();
        return owner.getId().equals(currentUser.getId());
    }

}
