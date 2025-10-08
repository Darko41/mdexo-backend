package com.doublez.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthSuccessController {

    @Autowired
    private UserService userService;

    @GetMapping("/auth/success")
    public String handleLoginSuccess(Authentication authentication, 
                                   HttpServletRequest request) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            
            try {
                if (userService.hasAdminRole(email)) {
                    // Set admin session and redirect to admin dashboard
                    request.getSession().setAttribute("adminUser", email);
                    return "redirect:/admin/dashboard";
                }
            } catch (Exception e) {
                System.out.println("❌ Error checking admin role: " + e.getMessage());
            }
            
            // Regular user - redirect to React app home
            return "redirect:/";
        }
        
        return "redirect:/auth/login";
    }
}