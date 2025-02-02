package com.doublez.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.entity.User;
import com.doublez.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public String registerUser(@RequestBody User user) {
		userService.registerUser(user);
		return "User registered successfully!";
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
	
}
