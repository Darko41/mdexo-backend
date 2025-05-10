package com.doublez.backend.service.realestate;

import org.springframework.stereotype.Service;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RealEstateAgentAssignmentService {
	
	private final RealEstateRepository realEstateRepository;
	private final UserRepository userRepository;
	
	public RealEstateAgentAssignmentService(
	        RealEstateRepository realEstateRepository,
	        UserRepository userRepository
	    ) {
	        this.realEstateRepository = realEstateRepository;
	        this.userRepository = userRepository;
	    }
	
	public void assignAgentToProperty(Long propertyId, Long userId) {
        RealEstate property = realEstateRepository.findById(propertyId)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if user is an agent
        if (!user.hasRole("ROLE_AGENT")) {
            throw new IllegalArgumentException("Specified user is not an agent");
        }

        property.assignAgent(user);
    }
}
