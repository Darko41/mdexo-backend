package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.doublez.backend.dto.RealEstateResponseDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;


@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private RealEstateService realEstateService;
	@Autowired
	private UserService userService;
	
	@GetMapping("/dashboard")
	public String showAdminDashboard(Model model) {
		
		long realEstateCount = realEstateService.getRealEstateCount();
		model.addAttribute("realEstateCount", realEstateCount);
		
		long userCount = userService.getUserCount();
		model.addAttribute("userCount", userCount);
		
		long agentCount = userService.getAgentCount();
		model.addAttribute("agentCount", agentCount);
		
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
