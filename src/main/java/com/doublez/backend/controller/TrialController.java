package com.doublez.backend.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/trial")
public class TrialController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrialController.class);
    
    private final TrialService trialService;
    private final UserService userService;
    
    public TrialController(TrialService trialService, UserService userService) {
        this.trialService = trialService;
        this.userService = userService;
    }
    
    /**
     * Get current user's trial status
     */
    @GetMapping("/my-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyTrialStatus() {
        try {
            Long userId = userService.getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("inTrial", trialService.isInTrial(user));
            status.put("trialExpired", trialService.isTrialExpired(user));
            status.put("daysRemaining", trialService.getTrialDaysRemaining(user));
            status.put("progressPercentage", trialService.getTrialProgressPercentage(user));
            status.put("tier", user.getTier());
            
            if (user.getTrialStartDate() != null) {
                status.put("trialStartDate", user.getTrialStartDate());
            }
            if (user.getTrialEndDate() != null) {
                status.put("trialEndDate", user.getTrialEndDate());
            }
            
            // Add agency info if applicable
            if (user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty()) {
                Agency agency = user.getOwnedAgencies().get(0);
                status.put("agencyInTrial", trialService.isAgencyInTrial(agency));
                status.put("agencyName", agency.getName());
            }
            
            logger.info("✅ Retrieved trial status for user: {}", userId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch trial status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch trial status"));
        }
    }
    
    /**
     * Start trial for current user (self-service)
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> startTrial() {
        try {
            Long userId = userService.getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            // Check if user already used trial
            if (user.getTrialUsed()) {
                logger.warn("🚫 User {} already used their trial", userId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Trial already used"));
            }
            
            trialService.startTrial(user);
            
            logger.info("✅ Trial started for user: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Trial started successfully"));
            
        } catch (Exception e) {
            logger.error("❌ Failed to start trial", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start trial"));
        }
    }
    
    /**
     * Extend trial for a user (admin only)
     */
    @PostMapping("/{userId}/extend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> extendTrial(@PathVariable Long userId, 
                                        @RequestParam Integer additionalMonths) {
        try {
            if (additionalMonths == null || additionalMonths <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Additional months must be positive"));
            }
            
            trialService.extendTrial(userId, additionalMonths);
            
            logger.info("✅ Extended trial for user {} by {} months", userId, additionalMonths);
            return ResponseEntity.ok(Map.of("message", "Trial extended successfully"));
            
        } catch (UserNotFoundException e) {
            logger.warn("❌ User not found for trial extension: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to extend trial for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to extend trial"));
        }
    }
    
    /**
     * Expire trial immediately (admin only)
     */
    @PostMapping("/{userId}/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expireTrial(@PathVariable Long userId) {
        try {
            trialService.expireTrial(userId);
            
            logger.info("✅ Manually expired trial for user: {}", userId);
            return ResponseEntity.ok(Map.of("message", "Trial expired successfully"));
            
        } catch (UserNotFoundException e) {
            logger.warn("❌ User not found for trial expiration: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to expire trial for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to expire trial"));
        }
    }
    
    /**
     * Get trial statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTrialStatistics() {
        try {
            Map<String, Object> stats = trialService.getEnhancedTrialStatistics();
            
            logger.info("✅ Retrieved trial statistics");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch trial statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch trial statistics"));
        }
    }
    
    /**
     * Check if user can start trial
     */
    @GetMapping("/can-start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> canStartTrial() {
        try {
            Long userId = userService.getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            boolean canStart = !user.getTrialUsed();
            
            Map<String, Object> response = new HashMap<>();
            response.put("canStart", canStart);
            response.put("trialUsed", user.getTrialUsed());
            
            if (!canStart) {
                response.put("reason", "Trial already used");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to check trial eligibility", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check trial eligibility"));
        }
    }
    
    /**
     * Get trial expiration warnings (admin only)
     */
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiringTrials(@RequestParam(defaultValue = "7") Integer daysThreshold) {
        try {
            // This would require a new repository method to find trials expiring soon
            // For now, returning a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "This endpoint requires additional repository implementation");
            response.put("daysThreshold", daysThreshold);
            
            logger.info("📊 Fetching trials expiring within {} days", daysThreshold);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch expiring trials", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch expiring trials"));
        }
    }
    
    /**
     * Get trial progress with detailed timeline
     */
    @GetMapping("/my-progress")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getTrialProgress() {
        try {
            Long userId = userService.getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("progressPercentage", trialService.getTrialProgressPercentage(user));
            progress.put("daysRemaining", trialService.getTrialDaysRemaining(user));
            progress.put("inTrial", trialService.isInTrial(user));
            progress.put("trialExpired", trialService.isTrialExpired(user));
            
            // Calculate timeline
            if (user.getTrialStartDate() != null && user.getTrialEndDate() != null) {
                progress.put("startDate", user.getTrialStartDate());
                progress.put("endDate", user.getTrialEndDate());
                progress.put("currentDate", LocalDate.now());
                
                long totalDays = ChronoUnit.DAYS.between(user.getTrialStartDate(), user.getTrialEndDate());
                long daysPassed = ChronoUnit.DAYS.between(user.getTrialStartDate(), LocalDate.now());
                progress.put("daysPassed", daysPassed);
                progress.put("totalDays", totalDays);
            }
            
            logger.info("✅ Retrieved detailed trial progress for user: {}", userId);
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch trial progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch trial progress"));
        }
    }
    
    /**
     * Agency trial status check
     */
    @GetMapping("/agency-status")
    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    public ResponseEntity<?> getAgencyTrialStatus() {
        try {
            Long userId = userService.getCurrentUserId();
            User user = userService.getUserEntityById(userId);
            
            if (!user.isAgencyAdmin() || user.getOwnedAgencies().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "User is not an agency admin"));
            }
            
            Agency agency = user.getOwnedAgencies().get(0);
            Map<String, Object> status = new HashMap<>();
            status.put("agencyInTrial", trialService.isAgencyInTrial(agency));
            status.put("agencyName", agency.getName());
            status.put("userInTrial", trialService.isInTrial(user));
            status.put("userTrialDaysRemaining", trialService.getTrialDaysRemaining(user));
            
            logger.info("✅ Retrieved agency trial status for agency: {}", agency.getName());
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ Failed to fetch agency trial status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch agency trial status"));
        }
    }
}