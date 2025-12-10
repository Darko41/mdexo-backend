package com.doublez.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.config.security.JwtTokenUtil;
import com.doublez.backend.dto.agency.AgencyDTO;
import com.doublez.backend.dto.auth.CustomUserDetails;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.request.AuthenticationRequest;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final AgencyService agencyService;
    private final TrialService trialService;
    
    private static final Logger logger = LoggerFactory.getLogger(AuthApiController.class);

    public AuthApiController(
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            UserService userService,
            AgencyService agencyService,
            TrialService trialService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.agencyService = agencyService;
        this.trialService = trialService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest authRequest) {
        try {
            logger.info("🔐 Authentication attempt for email: {}", authRequest.getEmail());
            
            // Validate input
            if (authRequest.getEmail() == null || authRequest.getEmail().trim().isEmpty()) {
                logger.warn("❌ Authentication failed: Email is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email is required"));
            }
            
            if (authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
                logger.warn("❌ Authentication failed: Password is empty for email: {}", authRequest.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Password is required"));
            }

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            String authenticatedEmail = authentication.getName();
            logger.info("✅ User authenticated successfully: {}", authenticatedEmail);
            
            // Get user and generate token
            User user = userService.getUserEntityByEmail(authenticatedEmail);
            List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

            // Generate JWT token
            String token = jwtTokenUtil.generateToken(authenticatedEmail, user.getId(), roles);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", authenticatedEmail);
            response.put("roles", roles);
            response.put("userId", user.getId());
            response.put("userTier", user.getTier());
            response.put("message", "Login successful");
            
            // Add agency info if user is an agency admin
            if (user.isAgencyAdmin()) {
                try {
                    AgencyDTO agencyDto = agencyService.getAgencyByAdminId(user.getId());
                    if (agencyDto != null) {
                        response.put("agency", Map.of(
                            "id", agencyDto.getId(),
                            "name", agencyDto.getName(),
                            "isActive", agencyDto.getIsActive()
                        ));
                        logger.info("🏢 Agency info added for user: {} - Agency: {}", authenticatedEmail, agencyDto.getName());
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Could not fetch agency info for user: {}", authenticatedEmail, e);
                }
            }

            // Add trial info if user is in trial
            if (trialService.isInTrial(user)) {
                response.put("trial", Map.of(
                    "isInTrial", true,
                    "trialEndDate", user.getTrialEndDate(),
                    "trialDaysRemaining", trialService.getTrialDaysRemaining(user)
                ));
            }

            logger.info("✅ Login successful for user: {}", authenticatedEmail);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("❌ Invalid credentials for email: {}", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
                
        } catch (DisabledException e) {
            logger.warn("❌ Account disabled for email: {}", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Account is disabled"));
                
        } catch (LockedException e) {
            logger.warn("❌ Account locked for email: {}", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Account is locked"));
                
        } catch (Exception e) {
            logger.error("❌ Authentication failed for email: {}", authRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Authentication failed",
                    "details", e.getMessage()
                ));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
            }
            
            String email = authentication.getName();
            User user = userService.getUserEntityByEmail(email);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", user.getEmail());
            userInfo.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));
            userInfo.put("userId", user.getId());
            userInfo.put("tier", user.getTier());
            
            // Add agency info if applicable
            if (user.isAgencyAdmin()) {
                try {
                    AgencyDTO agencyDto = agencyService.getAgencyByAdminId(user.getId());
                    if (agencyDto != null) {
                        userInfo.put("agency", Map.of(
                            "id", agencyDto.getId(),
                            "name", agencyDto.getName(),
                            "isActive", agencyDto.getIsActive()
                        ));
                    }
                } catch (Exception e) {
                    logger.warn("Could not fetch agency info for user: {}", email, e);
                }
            }
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            logger.error("Failed to get current user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user information"));
        }
    }
    
    // ✅ FIXED TOKEN REFRESH - Using extractEmail() instead of getUsernameFromToken()
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Authorization header is missing or invalid"));
            }
            
            String token = authHeader.substring(7);
            
            // ✅ FIX: Use extractEmail() instead of getUsernameFromToken()
            String email = jwtTokenUtil.extractEmail(token);
            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                // Token is valid - you can either return the same token or generate a new one
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("message", "Token is valid");
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token is invalid or expired"));
            }
            
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }
    
    // ✅ FIXED TOKEN VALIDATION - Using extractEmail()
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token is required"));
            }
            
            String email = jwtTokenUtil.extractEmail(token);
            if (email != null && jwtTokenUtil.validateToken(token, email)) {
                // Extract additional info from token
                Long userId = jwtTokenUtil.extractUserId(token);
                List<String> roles = jwtTokenUtil.extractRoles(token);
                
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", email,
                    "userId", userId,
                    "roles", roles,
                    "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Token is invalid or expired"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Token validation failed"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        logger.info("🚪 User logout requested");
        return ResponseEntity.ok(Map.of("message", "Logout successful - please discard your token"));
    }
}