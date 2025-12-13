package com.doublez.backend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.doublez.backend.dto.auth.CustomUserDetails;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.repository.UserRepository;

@Component
public class SecurityUtils {
    
    @Autowired
    private UserRepository userRepository; // Add this dependency
    
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getId();
            } else if (principal instanceof String) {
                // If principal is username/email, we'll handle this in the controller
                throw new RuntimeException("User ID not available in principal - string principal detected");
            } else {
                throw new RuntimeException("Unknown principal type: " + principal.getClass().getName());
            }
        }
        throw new RuntimeException("User not authenticated");
    }
    
    // ADD THIS METHOD:
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}