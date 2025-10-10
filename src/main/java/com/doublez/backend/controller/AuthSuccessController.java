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
                    request.getSession().setAttribute("adminUser", email);
                    return "redirect:/admin/dashboard";
                }
            } catch (Exception e) {
                System.out.println("❌ Error checking admin role: " + e.getMessage());
            }
            
            // For regular users accessing via form login
            return "redirect:/";
        }
        
        return "redirect:/auth/login";
    }
}