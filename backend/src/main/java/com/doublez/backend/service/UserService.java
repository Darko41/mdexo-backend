package com.doublez.backend.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

// This class is for registration (encoding passwords, saving users)

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Transactional
	public String registerUser(User user) {
		
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			return "User already exists";
		}
		
		// Encode the password
		String hashedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(hashedPassword);
		
		Role defaultRole = new Role();
		defaultRole.setName("ROLE_USER");
		user.setRoles(Collections.singletonList(defaultRole));
		
		userRepository.save(user);
		
		return "User registered successfully!";
	}
	
}
