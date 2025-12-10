package com.doublez.backend.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.doublez.backend.dto.auth.CustomUserDetails;

@Component
public class SecurityUtils {
 
 public Long getCurrentUserId() {
     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
     if (authentication != null && authentication.isAuthenticated() 
         && !(authentication instanceof AnonymousAuthenticationToken)) {
         
         Object principal = authentication.getPrincipal();
         
         if (principal instanceof CustomUserDetails) {
             // This matches your CustomUserDetails implementation
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
 
 public String getCurrentUsername() {
     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
     if (authentication != null && authentication.isAuthenticated()) {
         return authentication.getName();
     }
     return null;
 }
}