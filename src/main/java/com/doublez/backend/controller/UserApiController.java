package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.dto.user.UserUpdateDTO;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserService userService;
    
    // creation of new user
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserCreateDTO createDto) {
        // Self-service registration with limited fields
        try {
            return ResponseEntity.ok(userService.registerUser(createDto));
        } catch (EmailExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }
    
    // get all users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            List<UserResponseDTO> users = userService.getAllUsers();
            return users.isEmpty() ? 
                ResponseEntity.noContent().build() : 
                ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // get user by email
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable String email) {
        try {
            UserResponseDTO user = userService.getUserProfile(email);
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // USERS update their profiles
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') and #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        UserResponseDTO updatedUser = userService.updateUserProfile(id, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    // USERS delete themself
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') and #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id); // Let exception handler manage responses
        return ResponseEntity.noContent().build();
    }
    
    // get user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        try {
            UserResponseDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get user profile details
    @GetMapping("/{id}/profile")
    @PreAuthorize("(hasRole('USER') and #id == authentication.principal.id) or hasRole('ADMIN')") // ← ADDED ADMIN ACCESS
    public ResponseEntity<UserProfileDTO> getUserProfileDetails(@PathVariable Long id) {
        try {
            UserProfileDTO profile = userService.getUserProfile(id);
            return ResponseEntity.ok(profile);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update only profile details (separate from user account info)
    @PutMapping("/{id}/profile")
    @PreAuthorize("(hasRole('USER') and #id == authentication.principal.id) or hasRole('ADMIN')") // ← ADDED ADMIN ACCESS
    public ResponseEntity<UserResponseDTO> updateUserProfileDetails(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileDTO profileDTO) {
        
        try {
            UserResponseDTO updatedUser = userService.updateUserProfileDetails(id, profileDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Complete profile update (including email and roles - for admin use)
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserComplete(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        
        try {
            UserResponseDTO updatedUser = userService.updateUserProfile(id, updateDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}