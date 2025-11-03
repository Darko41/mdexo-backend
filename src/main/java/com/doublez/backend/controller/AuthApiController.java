package com.doublez.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.config.security.JwtTokenUtil;
import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.entity.User;
import com.doublez.backend.service.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(HttpServletRequest request) {
        try {
            // Read the raw request body
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            // Parse JSON manually
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            // Extract values from JSON
            String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
            String password = jsonNode.has("password") ? jsonNode.get("password").asText() : null;
            
            // Check for alternative field names
            if (email == null && jsonNode.has("username")) {
                email = jsonNode.get("username").asText();
            }
            if (email == null && jsonNode.has("userName")) {
                email = jsonNode.get("userName").asText();
                System.out.println("🔍 Found 'userName' field: " + email);
            }
            
            // Enhanced null checking
            if (email == null) {
                List<String> availableFields = new ArrayList<>();
                jsonNode.fieldNames().forEachRemaining(availableFields::add);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Email field not found in request",
                        "available_fields", availableFields,
                        "expected_field", "email",
                        "raw_request_body", requestBody
                    ));
            }
            
            if (password == null) {
                List<String> availableFields = new ArrayList<>();
                jsonNode.fieldNames().forEachRemaining(availableFields::add);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Password field not found in request",
                        "available_fields", availableFields,
                        "expected_field", "password",
                        "raw_request_body", requestBody
                    ));
            }
            
            if (email.isEmpty() || password.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email and password cannot be empty"));
            }
            
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            String authenticatedEmail = authentication.getName();
            
            // Get user and generate token
            User user = userService.getUserEntityByEmail(authenticatedEmail);
            List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

            // ✅ UPDATED: Pass user.getId() to generateToken
            String token = jwtTokenUtil.generateToken(authenticatedEmail, user.getId(), roles);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", authenticatedEmail);
            response.put("roles", roles);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Authentication failed",
                    "details", e.getMessage()
                ));
        }
    }
    
}