package com.doublez.backend.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.profile.InvestorProfile;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.investor.InvestorProfileService;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/investor")
@PreAuthorize("hasRole('INVESTOR')")
public class InvestorProfileController {

    private final InvestorProfileService investorProfileService;
    private final UserService userService;
    
    private static final Logger logger = LoggerFactory.getLogger(InvestorProfileController.class);

    public InvestorProfileController(
            InvestorProfileService investorProfileService,
            UserService userService) {
        this.investorProfileService = investorProfileService;
        this.userService = userService;
    }

//    @PutMapping("/profile")
//    public ResponseEntity<?> updateInvestorProfile(@RequestBody @Valid InvestorProfileDTO investorProfileDto) {
//        try {
//            Long userId = userService.getCurrentUserId();
//            logger.info("✏️ Updating investor profile for user ID: {}", userId);
//            
//            UserDTO updatedUser = investorProfileService.createOrUpdateInvestorProfile(userId, investorProfileDto);
//            
//            logger.info("✅ Investor profile updated successfully for user: {}", userId);
//            return ResponseEntity.ok(updatedUser);
//            
//        } catch (UserNotFoundException e) {
//            logger.warn("❌ User not found for profile update");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
//        } catch (IllegalOperationException e) {
//            logger.warn("🚫 Unauthorized profile update attempt");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            logger.error("❌ Failed to update investor profile", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update profile"));
//        }
//    }

//    @GetMapping("/profile")
//    public ResponseEntity<?> getInvestorProfile() {
//        try {
//            Long userId = userService.getCurrentUserId();
//            logger.info("🔍 Fetching investor profile for user ID: {}", userId);
//            
//            InvestorProfileDTO profileDto = investorProfileService.getInvestorProfile(userId);
//            
//            logger.info("✅ Investor profile retrieved successfully for user: {}", userId);
//            return ResponseEntity.ok(profileDto);
//            
//        } catch (ResourceNotFoundException e) {
//            logger.warn("❌ Investor profile not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
//        } catch (IllegalOperationException e) {
//            logger.warn("🚫 Unauthorized profile access attempt");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            logger.error("❌ Failed to retrieve investor profile", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to retrieve profile"));
//        }
//    }

//    @PostMapping("/profile")
//    public ResponseEntity<?> createInvestorProfile(@RequestBody @Valid InvestorProfileDTO investorProfileDto) {
//        try {
//            Long userId = userService.getCurrentUserId();
//            logger.info("🆕 Creating investor profile for user ID: {}", userId);
//            
//            UserDTO updatedUser = investorProfileService.createInvestorProfile(userId, investorProfileDto);
//            
//            logger.info("✅ Investor profile created successfully for user: {}", userId);
//            return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
//            
//        } catch (IllegalOperationException e) {
//            logger.warn("🚫 Profile creation conflict - profile already exists");
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            logger.error("❌ Failed to create investor profile", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create profile"));
//        }
//    }

    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteInvestorProfile() {
        try {
            Long userId = userService.getCurrentUserId();
            logger.info("🗑️ Deleting investor profile for user ID: {}", userId);
            
            investorProfileService.deleteInvestorProfile(userId);
            
            logger.info("✅ Investor profile deleted successfully for user: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Investor profile deleted successfully"));
            
        } catch (ResourceNotFoundException e) {
            logger.warn("❌ No investor profile found to delete");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Failed to delete investor profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to delete profile"));
        }
    }

//    @GetMapping("/dashboard")
//    public ResponseEntity<?> getInvestorDashboard() {
//        try {
//            Long userId = userService.getCurrentUserId();
//            logger.info("📊 Fetching investor dashboard for user ID: {}", userId);
//            
//            Map<String, Object> dashboard = investorProfileService.getInvestorDashboard(userId);
//            
//            logger.info("✅ Investor dashboard retrieved successfully for user: {}", userId);
//            return ResponseEntity.ok(dashboard);
//            
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch investor dashboard", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to fetch dashboard"));
//        }
//    }

    @GetMapping("/profile/exists")
    public ResponseEntity<Map<String, Boolean>> checkProfileExists() {
        try {
            Long userId = userService.getCurrentUserId();
            logger.info("🔍 Checking if investor profile exists for user ID: {}", userId);
            
            boolean exists = investorProfileService.hasInvestorProfile(userId);
            
            logger.info("✅ Profile existence check completed - exists: {}", exists);
            return ResponseEntity.ok(Map.of("exists", exists));
            
        } catch (Exception e) {
            logger.error("❌ Failed to check profile existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("exists", false));
        }
    }
}