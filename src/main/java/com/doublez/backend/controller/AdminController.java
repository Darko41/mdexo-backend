package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.doublez.backend.config.security.JwtTokenUtil;
import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private RealEstateService realEstateService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Show admin login page (uses existing auth/login.html)
    @GetMapping("/login")
    public String showAdminLoginPage(Model model) {
        model.addAttribute("adminLogin", true);
        return "auth/login"; // Use existing login page
    }

    // Process admin login - redirects to form login
    @GetMapping("/access")
    public String adminAccessRedirect() {
        return "redirect:/admin/login";
    }

    // Secure all admin pages with session check
    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return "redirect:/admin/login";
        }
        
        String adminUser = (String) request.getSession().getAttribute("adminUser");
        System.out.println("🛠️ Admin dashboard accessed by: " + adminUser);
        return setupDashboard(model);
    }

    @GetMapping("/users")
    public String showUserData(Model model, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return "redirect:/admin/login";
        }
        
        List<UserResponseDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/userdata";
    }

    @GetMapping("/real-estates")
    public String showRealEstatesData(Model model, HttpServletRequest request) {
        if (!isAdminAuthenticated(request)) {
            return "redirect:/admin/login";
        }
        
        List<RealEstateResponseDTO> realEstates = realEstateService.getAllRealEstates();
        model.addAttribute("realEstates", realEstates);
        return "admin/realestatedata";
    }

    // Helper methods
    private boolean isAdminAuthenticated(HttpServletRequest request) {
        String adminUser = (String) request.getSession().getAttribute("adminUser");
        return adminUser != null && userService.hasAdminRole(adminUser);
    }

    private String setupDashboard(Model model) {
        model.addAttribute("realEstateCount", realEstateService.getRealEstateCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("agentCount", userService.getAgentCount());
        return "admin/dashboard";
    }
}
