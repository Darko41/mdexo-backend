package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger logger = LoggerFactory.getLogger(UserApiController.class);

    private final UserService userService;
    private final TrialService trialService;

    public UserApiController(UserService userService, TrialService trialService) {
        this.userService = userService;
        this.trialService = trialService;
    }

    // === USER REGISTRATION ENDPOINTS ===

    /**
     * Public user registration
     */
//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO.Create createDto) {
//        try {
//            logger.info("👤 User registration attempt for email: {}", createDto.getEmail());
//
//            // Override any roles/tier for public registration
//            createDto.setRoles(List.of("ROLE_USER"));
//            createDto.setTier(UserTier.FREE_USER);
//
//            UserDTO user = userService.registerUser(createDto, false);
//            
//            logger.info("✅ User registered successfully: {}", user.getEmail());
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .header("Location", "/api/users/" + user.getId())
//                    .body(user);
//
//        } catch (EmailExistsException e) {
//            logger.warn("❌ Email already exists: {}", createDto.getEmail());
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body(Map.of("error", "Email already exists"));
//        } catch (IllegalArgumentException e) {
//            logger.warn("❌ Invalid registration data: {}", e.getMessage());
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            logger.error("❌ Registration failed for: {}", createDto.getEmail(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Registration failed"));
//        }
//    }

    /**
     * Admin user creation
     */
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO.Create createDto) {
//        try {
//            logger.info("👑 Admin creating user: {}", createDto.getEmail());
//
//            UserDTO user = userService.registerUser(createDto, true);
//            
//            logger.info("✅ Admin created user successfully: {}", user.getEmail());
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .header("Location", "/api/users/" + user.getId())
//                    .body(user);
//
//        } catch (EmailExistsException e) {
//            logger.warn("❌ Email already exists: {}", createDto.getEmail());
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body(Map.of("error", "Email already exists"));
//        } catch (Exception e) {
//            logger.error("❌ Admin user creation failed", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to create user"));
//        }
//    }

    // === USER RETRIEVAL ENDPOINTS ===

    /**
     * Get current user profile
     */
//    @GetMapping("/me")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> getCurrentUser() {
//        try {
//            Long userId = userService.getCurrentUserId();
//            UserDTO user = userService.getUserById(userId);
//            
//            logger.debug("✅ Retrieved current user: {}", userId);
//            return ResponseEntity.ok(user);
//
//        } catch (UserNotFoundException e) {
//            logger.warn("❌ Current user not found");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "User not found"));
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch current user", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to fetch user profile"));
//        }
//    }

    /**
     * Get user by ID
     */
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
//    public ResponseEntity<?> getUserById(@PathVariable Long id) {
//        try {
//            UserDTO user = userService.getUserById(id);
//            
//            logger.debug("✅ Retrieved user: {}", id);
//            return ResponseEntity.ok(user);
//
//        } catch (UserNotFoundException e) {
//            logger.warn("❌ User not found: {}", id);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "User not found"));
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch user: {}", id, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to fetch user"));
//        }
//    }

    /**
     * Get all users (admin only)
     */
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> getAllUsers() {
//        try {
//            List<UserDTO> users = userService.getAllUsers();
//            
//            logger.info("✅ Admin retrieved all users, count: {}", users.size());
//            return users.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(users);
//
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch users", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to fetch users"));
//        }
//    }

    // === USER UPDATE ENDPOINTS ===

    /**
     * Update user
//     */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
//    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO.Update updateDto) {
//        try {
//            UserDTO user = userService.updateUser(id, updateDto);
//            
//            logger.info("✅ Updated user: {}", id);
//            return ResponseEntity.ok(user);
//
//        } catch (UserNotFoundException e) {
//            logger.warn("❌ User not found for update: {}", id);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "User not found"));
//        } catch (Exception e) {
//            logger.error("❌ Failed to update user: {}", id, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to update user"));
//        }
//    }

    // === USER DELETION ENDPOINTS ===

    /**
     * Delete user (self-deletion or admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and #id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userService.deleteUser(id, authentication);
            
            logger.info("✅ User deleted: {}", id);
            return ResponseEntity.noContent().build();

        } catch (IllegalOperationException e) {
            logger.warn("🚫 Unauthorized deletion attempt for user: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (UserNotFoundException e) {
            logger.warn("❌ User not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to delete user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user"));
        }
    }

    // === ENHANCED USER MANAGEMENT ENDPOINTS ===

    /**
     * Get user statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStatistics() {
        try {
            long totalUsers = userService.getUserCount();
            Map<String, Object> trialStats = trialService.getEnhancedTrialStatistics();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.putAll(trialStats);
            
            logger.info("✅ Retrieved user statistics");
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("❌ Failed to fetch user statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch statistics"));
        }
    }

    /**
     * Get current user's agency info
     */
//    @GetMapping("/me/agency")
//    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'AGENCY_ADMIN')")
//    public ResponseEntity<?> getMyAgency() {
//        try {
//            Long userId = userService.getCurrentUserId();
//            Optional<AgencyDTO> agency = userService.getUserAgency(userId);
//            
//            if (agency.isPresent()) {
//                logger.info("✅ Retrieved agency info for user: {}", userId);
//                return ResponseEntity.ok(agency.get());
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Map.of("error", "User does not have an agency"));
//            }
//
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch user agency", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to fetch agency information"));
//        }
//    }

    /**
     * Check if current user has agency
     */
    @GetMapping("/me/has-agency")
    @PreAuthorize("hasAnyRole('USER', 'AGENT', 'AGENCY_ADMIN')")
    public ResponseEntity<?> hasAgency() {
        try {
            Long userId = userService.getCurrentUserId();
            boolean hasAgency = userService.userHasAgency(userId);
            
            return ResponseEntity.ok(Map.of("hasAgency", hasAgency));

        } catch (Exception e) {
            logger.error("❌ Failed to check agency status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check agency status"));
        }
    }

    /**
     * Get user's image count (admin only)
     */
    @GetMapping("/{id}/image-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserImageCount(@PathVariable Long id) {
        try {
            long imageCount = userService.countImages(id);
            
            return ResponseEntity.ok(Map.of("userId", id, "imageCount", imageCount));

        } catch (UserNotFoundException e) {
            logger.warn("❌ User not found for image count: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to fetch image count for user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch image count"));
        }
    }

    /**
     * Check if current user is admin
     */
    @GetMapping("/me/is-admin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> isCurrentUserAdmin() {
        try {
            boolean isAdmin = userService.isCurrentUserAdmin();
            
            return ResponseEntity.ok(Map.of("isAdmin", isAdmin));

        } catch (Exception e) {
            logger.error("❌ Failed to check admin status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check admin status"));
        }
    }

    /**
     * Get current user's trial status
     */
//    @GetMapping("/me/trial-status")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> getMyTrialStatus() {
//        try {
//            Long userId = userService.getCurrentUserId();
//            User user = userService.getUserEntityById(userId);
//            
//            Map<String, Object> status = new HashMap<>();
//            status.put("inTrial", trialService.isInTrial(user));
//            status.put("trialExpired", trialService.isTrialExpired(user));
//            status.put("daysRemaining", trialService.getTrialDaysRemaining(user));
//            status.put("progressPercentage", trialService.getTrialProgressPercentage(user));
//            status.put("tier", user.getTier());
//            
//            if (user.getTrialStartDate() != null) {
//                status.put("trialStartDate", user.getTrialStartDate());
//            }
//            if (user.getTrialEndDate() != null) {
//                status.put("trialEndDate", user.getTrialEndDate());
//            }
//            
//            // Add agency info if applicable
//            if (user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty()) {
//                Agency agency = user.getOwnedAgencies().get(0);
//                status.put("agencyInTrial", trialService.isAgencyInTrial(agency));
//                status.put("agencyName", agency.getName());
//            }
//            
//            logger.info("✅ Retrieved trial status for user: {}", userId);
//            return ResponseEntity.ok(status);
//
//        } catch (Exception e) {
//            logger.error("❌ Failed to fetch trial status", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to fetch trial status"));
//        }
//    }

    /**
     * Start trial for current user (self-service)
     */
    @PostMapping("/me/start-trial")
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
    @PostMapping("/{id}/extend-trial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> extendTrial(@PathVariable Long id, 
                                        @RequestParam Integer additionalMonths) {
        try {
            if (additionalMonths == null || additionalMonths <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Additional months must be positive"));
            }
            
            trialService.extendTrial(id, additionalMonths);
            
            logger.info("✅ Extended trial for user {} by {} months", id, additionalMonths);
            return ResponseEntity.ok(Map.of("message", "Trial extended successfully"));

        } catch (UserNotFoundException e) {
            logger.warn("❌ User not found for trial extension: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        } catch (Exception e) {
            logger.error("❌ Failed to extend trial for user: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to extend trial"));
        }
    }

    /**
     * Expire trial immediately (admin only)
     */
//    @PostMapping("/{id}/expire-trial")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> expireTrial(@PathVariable Long id) {
//        try {
//            trialService.expireTrial(id);
//            
//            logger.info("✅ Manually expired trial for user: {}", id);
//            return ResponseEntity.ok(Map.of("message", "Trial expired successfully"));
//
//        } catch (UserNotFoundException e) {
//            logger.warn("❌ User not found for trial expiration: {}", id);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", "User not found"));
//        } catch (Exception e) {
//            logger.error("❌ Failed to expire trial for user: {}", id, e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to expire trial"));
//        }
//    }
}