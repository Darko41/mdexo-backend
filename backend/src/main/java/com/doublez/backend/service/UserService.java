package com.doublez.backend.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.UserDetailsDTO;
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

	public UserDetailsDTO getUserProfile(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		
		List<String> roles = user.getRoles().stream()
				.map(Role::getName)
				.collect(Collectors.toList());
		
		return new UserDetailsDTO(user.getUsername(), roles);
	}

	public boolean updateProfile(String username, UserDetailsDTO userDetailsDTO) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		
		// Update user details
		if (userDetailsDTO.getUsername() != null && !userDetailsDTO.getUsername().isEmpty()) {
			user.setUsername(userDetailsDTO.getUsername());
		}
		
		if (userDetailsDTO.getPassword() != null && !userDetailsDTO.getPassword().isEmpty()) {
			String encodedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
			user.setPassword(encodedPassword);
		}
		
		userRepository.save(user);
			
		return true;
	}
	
}
