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

    // Remove the manual authentication checks from other methods
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
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