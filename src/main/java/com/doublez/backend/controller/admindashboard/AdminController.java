package com.doublez.backend.controller.admindashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.doublez.backend.dto.realestate.RealEstateCreateDTO;
import com.doublez.backend.dto.realestate.RealEstateResponseDTO;
import com.doublez.backend.dto.realestate.RealEstateUpdateDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.service.realestate.AdminRealEstateService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

//For Admin dashboard (Thymeleaf server side) actions

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final RealEstateService realEstateService;
    private final AdminRealEstateService adminRealEstateService;
    private final UserService userService;

    public AdminController(
            RealEstateService realEstateService,
            AdminRealEstateService adminRealEstateService,
            UserService userService) {
        this.realEstateService = realEstateService;
        this.adminRealEstateService = adminRealEstateService;
        this.userService = userService;
    }

    // Single @ModelAttribute method that handles everything
    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        // Add CSRF token
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        
        // Add authentication info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("username", auth.getName());
        }
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model, HttpSession session) {
        if (session.isNew()) {
            return "redirect:/auth/login?admin=true";
        }
        model.addAttribute("realEstateCount", realEstateService.getRealEstateCount());
        model.addAttribute("userCount", userService.getUserCount());
        model.addAttribute("agentCount", userService.getAgentCount());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String showUserData(Model model) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/userdata";
    }

    @GetMapping("/real-estates")
    public String showRealEstatesData(Model model) {
        List<RealEstateResponseDTO> realEstates = realEstateService.getAllRealEstates();
        model.addAttribute("realEstates", realEstates);
        return "admin/realestatedata";
    }

    @GetMapping("/real-estates/new")
    public String showCreateRealEstateForm(Model model) {
        model.addAttribute("isEdit", false);
        return "admin/realestate-form";
    }

    @GetMapping("/real-estates/{propertyId}/edit")
    public String showEditRealEstateForm(@PathVariable Long propertyId, Model model) {
        RealEstateResponseDTO realEstate = realEstateService.getRealEstateById(propertyId);
        model.addAttribute("realEstate", realEstate);
        model.addAttribute("isEdit", true);
        return "admin/realestate-form";
    }

    @GetMapping("/real-estates/{propertyId}")
    @ResponseBody
    public ResponseEntity<RealEstateResponseDTO> getRealEstate(@PathVariable Long propertyId) {
        RealEstateResponseDTO realEstate = realEstateService.getRealEstateById(propertyId);
        return ResponseEntity.ok(realEstate);
    }
    
    @GetMapping("/real-estates/{propertyId}/view")
    public String viewRealEstate(@PathVariable Long propertyId, Model model) {
        RealEstateResponseDTO realEstate = realEstateService.getRealEstateById(propertyId);
        model.addAttribute("property", realEstate);
        return "admin/property";
    }
    
    @PostMapping("/real-estates/{propertyId}/update")
    public String updateRealEstateAndRedirect(@PathVariable Long propertyId,
                                              @ModelAttribute RealEstateUpdateDTO updateDto,
                                              @RequestParam(required = false) MultipartFile[] images,
                                              RedirectAttributes redirectAttributes) {
        try {
            adminRealEstateService.updateRealEstate(propertyId, updateDto, images);
            redirectAttributes.addFlashAttribute("success", "Real estate updated successfully!");
            return "redirect:/admin/real-estates/" + propertyId + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update real estate: " + e.getMessage());
            return "redirect:/admin/real-estates/" + propertyId + "/edit";
        }
    }
    
    @PostMapping("/real-estates")
    public String createRealEstateAndRedirect(
            @ModelAttribute RealEstateCreateDTO createDto,
            @RequestParam(required = false) MultipartFile[] images,
            RedirectAttributes redirectAttributes) {
        try {
            RealEstateResponseDTO response = realEstateService.createRealEstateForUser(createDto, images);
            redirectAttributes.addFlashAttribute("success", "Real estate created successfully!");
            return "redirect:/admin/real-estates/" + response.getPropertyId() + "/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create real estate: " + e.getMessage());
            return "redirect:/admin/real-estates/new";
        }
    }
    
    @GetMapping("/session-status")
    @ResponseBody
    public ResponseEntity<?> checkSessionStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/real-estates/{propertyId}")
    @ResponseBody
    public ResponseEntity<?> deleteRealEstate(@PathVariable Long propertyId) {
        try {
            adminRealEstateService.deleteRealEstate(propertyId);
            return ResponseEntity.ok().body("Real estate deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete real estate: " + e.getMessage());
        }
    }
}