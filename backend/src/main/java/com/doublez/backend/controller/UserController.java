package com.doublez.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.User;
import com.doublez.backend.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/register")
	public String registerUser(@RequestBody User user) {
		userService.registerUser(user);
		return "User registered successfully!";
	}
	
	@PostMapping("/login")
	public String loginUser(@RequestBody User user) {
		// Login logic here
		return "Login Successfull!";
	}
	
	public String getUserDetails() {
		// Get user details logic here
		return "User details";
	}
	
}
