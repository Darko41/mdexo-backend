package com.doublez.backend.service.realestate;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.service.user.UserService;
import com.doublez.backend.utils.RoleUtils;

@Service
public class RealEstateAuthorizationService {
	
	private static final String ROLE_PREFIX = "ROLE_";

    private final RealEstateRepository realEstateRepository;
    private final UserService userService;

    public RealEstateAuthorizationService(
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
    
    /** Important Considerations: TODO
     * 1. Testing: Make sure to thoroughly test the combined service,
     * especially the role hierarchy logic
     * 2. Performance: For frequently called methods (like isOwner),
     * 3. Error Handling: You might want to add more detailed
     * exception handling for security violations
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
    
    public boolean hasRealEstateCreateAccess() {
        return hasAnyRole("ADMIN", "AGENT", "USER");
    }

    public boolean hasRealEstateUpdateAccess(Long propertyId) {
        return isAdmin() || (hasAnyRole("USER", "AGENT") && isOwner(propertyId));
    }
    
    public boolean canDeleteRealEstate(Long propertyId) {
        // Admins can delete any property
        if (isAdmin()) {
            return true;
        }
        
        if (hasRole("USER") && isOwner(propertyId)) {
            return true;
        }
        
        return hasRole("AGENT") && isAssignedAgent(propertyId);
    }
    
    public boolean canManageProperty(Long propertyId) {
    	User currentUser = userService.getAuthenticatedUser();
    	
    	if (currentUser.hasRole("ROLE_ADMIN")) {
    		return true;
    	}
    	
    	if (isOwner(propertyId)) {
    		return true;
    	}
    	
    	return isAssignedAgent(propertyId);
    	
    }

    // Helper methods
    private boolean isAssignedAgent(Long propertyId) {
    	User currentUser = userService.getAuthenticatedUser();
        
        // Short-circuit if not an agent
        if (!currentUser.hasRole("ROLE_AGENT")) {
            return false;
        }
        
        // More efficient query - avoids loading entire property
        return realEstateRepository.existsByIdAndAssignedAgentsId(
            propertyId, 
            currentUser.getId()
        );
    }
    
    private boolean isAdmin() {
        return hasRole("ADMIN");
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return RoleUtils.hasRole(auth, role);
    }

    private boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return RoleUtils.hasAnyRole(auth, roles);
    }
    
    

}
