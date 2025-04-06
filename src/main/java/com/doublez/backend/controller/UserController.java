package com.doublez.backend.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public ResponseEntity<String> registerUser (@RequestBody UserDetailsDTO userDetailsDTO) {
		
		try {
			String registrationResult = userService.registerUser(userDetailsDTO);
			return ResponseEntity.ok(registrationResult);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error registering user: " + e.getMessage());
		}
		
	}
	
	@GetMapping
	public ResponseEntity<List<UserDetailsDTO>> getAllUsers() {
		try {
			List<UserDetailsDTO> userDetailsDTOs = userService.getAllUsers();
			if (!userDetailsDTOs.isEmpty()) {
				return ResponseEntity.ok(userDetailsDTOs);
			}
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
	
	@GetMapping("/by-email{email}")
	public ResponseEntity<UserDetailsDTO> getUserProfile(@PathVariable String email) {
		try {
	        UserDetailsDTO userDetailsDTO = userService.getUserProfile(email);
	        return ResponseEntity.ok(userDetailsDTO);
	    } catch (UsernameNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	    }
	}
	
	@PutMapping("/update/{id}")
	@PreAuthorize("hasRole('USER') and #id == authentication.principal.id")
	public ResponseEntity<String> updateUserProfile(
	        @PathVariable Long id,
	        @Valid @RequestBody UserUpdateDTO updateDTO) {  // Direct DTO binding
	    
	    boolean isUpdated = userService.updateProfile(id, updateDTO);
	    return isUpdated ? 
	        ResponseEntity.ok("Profile updated successfully") :
	        ResponseEntity.notFound().build();
	}
	
	@PostMapping("/add")
	public ResponseEntity<String> addUser(@RequestBody UserDetailsDTO userDetailsDTO) {
		try {
			String result = userService.addUser(userDetailsDTO);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding user: " + e.getMessage());
		}
		
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		try {
			boolean isDeleted = userService.deleteUser(id);
			if (isDeleted) {
				return ResponseEntity.ok("User deleted successfully!");
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting user");
		}
	}
	
	
	
}
