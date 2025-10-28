package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private RealEstateService realEstateService;
    
    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addCsrfToken(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        return setupDashboard(model);
    }

    @GetMapping("/users")
    public String showUserData(Model model) {
        addAuthenticationToModel(model);
        List<UserResponseDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/userdata";
    }

    @GetMapping("/real-estates")
    public String showRealEstatesData(Model model) {
        addAuthenticationToModel(model);
        List<RealEstateResponseDTO> realEstates = realEstateService.getAllRealEstates();
        model.addAttribute("realEstates", realEstates);
        return "admin/realestatedata";
    }

    @GetMapping("/real-estates/new")
    public String showCreateRealEstateForm(Model model) {
        addAuthenticationToModel(model);
        model.addAttribute("isEdit", false);
        return "admin/realestate-form";
    }

    // KEEP ONLY THIS ONE - remove the other version
    @GetMapping("/real-estates/{propertyId}/edit")
    public String showEditRealEstateForm(@PathVariable Long propertyId, Model model) {
        addAuthenticationToModel(model);
        
        // The @ModelAttribute method above already adds CSRF token automatically
        // No need for HttpServletRequest parameter here
        
        RealEstateResponseDTO realEstate = realEstateService.getRealEstateById(propertyId);
        model.addAttribute("realEstate", realEstate);
        model.addAttribute("isEdit", true);
        return "admin/realestate-form";
    }

    private String setupDashboard(Model model) {
        addAuthenticationToModel(model);
        model.addAttribute("realEstateCount", realEstateService.getRealEstateCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("agentCount", userService.getAgentCount());
        return "admin/dashboard";
    }
    
    private void addAuthenticationToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
        }
    }
    
    @GetMapping("/real-estates/{propertyId}")
    @ResponseBody
    public ResponseEntity<RealEstateResponseDTO> getRealEstate(@PathVariable Long propertyId) {
        RealEstateResponseDTO realEstate = realEstateService.getRealEstateById(propertyId);
        return ResponseEntity.ok(realEstate);
    }
}