package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
									@CookieValue(name = "JWT", required = false) String jwtToken,
									HttpServletRequest request) {
		
		if (jwtToken != null) {
			try {
				String email = jwtTokenUtil.extractEmail(jwtToken);
				if (!jwtTokenUtil.validateToken(jwtToken, email)) {
					return "redirect:/login";
				}
			} catch (Exception e) {
				return "redirect:/login";
			}
		}
		
		else if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }
		
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
