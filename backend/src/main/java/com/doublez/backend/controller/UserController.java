package com.doublez.backend.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.service.UserService;

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
	
	@GetMapping("/{username}")
	public ResponseEntity<UserDetailsDTO> getUserProfile(@PathVariable String username) {
		UserDetailsDTO userDetailsDTO = userService.getUserProfile(username);
		if (userDetailsDTO != null) {
			return ResponseEntity.ok(userDetailsDTO);
		}
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	@PutMapping("/{username}")
	public ResponseEntity<String> updateUserProfile(@PathVariable String username, @RequestBody UserDetailsDTO userDetailsDTO) {
		boolean isUpdated = userService.updateProfile(username, userDetailsDTO);
		if (isUpdated) {
			return ResponseEntity.ok("User profile updated successfully!");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
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
	
	@DeleteMapping("/{username}")
	public ResponseEntity<String> deleteUser(@PathVariable String username) {
		try {
			boolean isDeleted = userService.deleteUser(username);
			if (isDeleted) {
				return ResponseEntity.ok("User deleted successfully!");
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting user");
		}
	}
	
}
