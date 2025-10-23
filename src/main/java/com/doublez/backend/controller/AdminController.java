package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;



@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private RealEstateService realEstateService;
    @Autowired
    private UserService userService;

    // Show admin login page - use existing auth/login template
    @GetMapping("/login")
    public String showAdminLoginPage(Model model) {
        // If already authenticated as admin, redirect to dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && 
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        
        model.addAttribute("adminLogin", true);
        return "auth/login"; // Use existing login template
    }

    // Remove the manual authentication checks from other methods
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        System.out.println("🛠️ Admin dashboard accessed");
        return setupDashboard(model);
    }

    @GetMapping("/users")
    public String showUserData(Model model) {
        List<UserResponseDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/userdata";
    }

    @GetMapping("/real-estates")
    public String showRealEstatesData(Model model) {
        List<RealEstateResponseDTO> realEstates = realEstateService.getAllRealEstates();
        model.addAttribute("realEstates", realEstates);
        return "admin/realestatedata";
    }

    private String setupDashboard(Model model) {
        model.addAttribute("realEstateCount", realEstateService.getRealEstateCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("agentCount", userService.getAgentCount());
        return "admin/dashboard";
    }
}