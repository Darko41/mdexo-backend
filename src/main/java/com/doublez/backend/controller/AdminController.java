package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	@GetMapping("/dashboard")
	public String showAdminDashboard(Model model,
	                                @RequestParam(name = "token", required = false) String tokenParam,
	                                HttpServletRequest request) {
	    
	    System.out.println("🛠️ Admin dashboard accessed");
	    
	    // Check token from URL parameter
	    if (tokenParam != null) {
	        try {
	            String email = jwtTokenUtil.extractEmail(tokenParam);
	            if (jwtTokenUtil.validateToken(tokenParam, email) && 
	                userService.hasAdminRole(email)) { // Now this method exists
	                
	                // Create session for this admin session
	                request.getSession().setAttribute("adminUser", email);
	                System.out.println("✅ Admin authenticated via token: " + email);
	                return setupDashboard(model);
	            } else {
	                System.out.println("❌ User is not an admin: " + email);
	            }
	        } catch (Exception e) {
	            System.out.println("❌ Invalid admin token: " + e.getMessage());
	        }
	    }
	    
	    // Check existing session
	    String adminUser = (String) request.getSession().getAttribute("adminUser");
	    if (adminUser != null && userService.hasAdminRole(adminUser)) {
	        System.out.println("✅ Admin session found for: " + adminUser);
	        return setupDashboard(model);
	    }
	    
	    // No valid auth - redirect to React login
	    System.out.println("❌ No admin auth, redirecting to React");
	    return "redirect:http://localhost:5173/login";
	}
    
    private String setupDashboard(Model model) {
        model.addAttribute("realEstateCount", realEstateService.getRealEstateCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("agentCount", userService.getAgentCount());
        return "admin/dashboard";
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
	
}
