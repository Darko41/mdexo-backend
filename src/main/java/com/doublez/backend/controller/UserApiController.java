package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserRegistrationDTO;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.service.user.RolePromotionService;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RolePromotionService rolePromotionService;
    
    // Consolidated user creation (for admin use)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO.Create createDto) {
        UserDTO user = userService.registerUser(createDto, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // Consolidated user update
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
                                            @Valid @RequestBody UserDTO.Update updateDto) {
        UserDTO user = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(user);
    }
    
    // SIMPLE REGISTRATION: For public user registration
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDto) {
        try {
            String result = userService.simpleRegister(registrationDto.getEmail(), registrationDto.getPassword());
            return ResponseEntity.ok(result);
        } catch (EmailExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    // GET ALL USERS: Returns UserDTO now
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return users.isEmpty() ? 
                ResponseEntity.noContent().build() : 
                ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET USER BY ID: Returns UserDTO now
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET CURRENT USER: Returns UserDTO
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserDTO user = userService.getUserById(userDetails.getId());
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // CASCADE DELETION
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and #id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            userService.deleteUser(id, authentication);
            return ResponseEntity.noContent().build();
        } catch (IllegalOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // User-Facing Promotion Endpoint
    @PostMapping("/request-agent-promotion")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> requestAgentPromotion(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserDTO user = rolePromotionService.promoteToAgent(userDetails.getId());
            return ResponseEntity.ok("Successfully promoted to agent role");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to promote to agent: " + e.getMessage());
        }
    }
}