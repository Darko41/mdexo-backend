package com.doublez.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;	// TODO check if another package is required

// This class is for registration (encoding passwords, saving users)

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleRepository;
	
	@Transactional
	public String registerUser(UserDetailsDTO userDetailsDTO) {
		
		if (userRepository.findByEmail(userDetailsDTO.getEmail()).isPresent()) {
			return "User already exists with this email";
		}
		
		if (userDetailsDTO.getPassword() == null || userDetailsDTO.getPassword().isEmpty()) {
			return "Password cannot be empty";
		}
		
		if (userDetailsDTO.getPassword().length() < 6) {
			return "Password must be at least 6 characters long";
		}
		
		// If no roles are provided, assign a default role
		List<String> roles = userDetailsDTO.getRoles(); 
		if (roles == null || roles.isEmpty()) {
			roles = List.of("ROLE_USER");
		}
				
		// Fetch roles from the provided DTO using roleRepository
		List<Role> roleEntites = roles.stream()
	            .map(roleName -> {
	            	// If the role does not exist, create it
	            	Role role = roleRepository.findByName(roleName).orElseGet(() -> {
	            		Role newRole = new Role();
	            		newRole.setName(roleName);
	            		return roleRepository.save(newRole);
	            	});
	            	return role;
	            })
	            .collect(Collectors.toList());
		
		// Encode the password
		String hashedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
		// Create the User entity
		User user = new User();
		user.setEmail(userDetailsDTO.getEmail());
		user.setPassword(hashedPassword);
		user.setRoles(roleEntites);
		
		userRepository.save(user);
		
		return "User registered successfully!";
	}

	public UserDetailsDTO getUserProfile(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
		
		List<String> roles = user.getRoles().stream()
				.map(Role::getName)
				.collect(Collectors.toList());
		
		return new UserDetailsDTO(user.getEmail(), roles);
	}

	public boolean updateProfile(String email, UserDetailsDTO userDetailsDTO) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
		
		// Update email if provided (optional, as you may not allow email updates in some systems)
		if (userDetailsDTO.getEmail() != null && !userDetailsDTO.getEmail().isEmpty()) {
			user.setEmail(userDetailsDTO.getEmail());
		}
		
		if (userDetailsDTO.getPassword() != null && !userDetailsDTO.getPassword().isEmpty()) {
			String encodedPassword = passwordEncoder.encode(userDetailsDTO.getPassword());
			user.setPassword(encodedPassword);
		}
		
		userRepository.save(user);
			
		return true;
	}
	
	public List<UserDetailsDTO> getAllUsers() {
	    List<User> users = userRepository.findAll();
	    return users.stream()
	            .map(user -> new UserDetailsDTO(
	                    user.getId(),
	                    user.getEmail(), 
	                    user.getRoles().stream()
	                        .map(Role::getName)
	                        .collect(Collectors.toList()),
	                    user.getCreatedAt(),
	                    user.getUpdatedAt()))
	            .collect(Collectors.toList());
	}

	@Transactional
	public String addUser(UserDetailsDTO userDetailsDTO) {
		if (userRepository.findByEmail(userDetailsDTO.getEmail()).isPresent()) {
			throw new RuntimeException("User already exists");
		}
		
		// Create the user object
		User user = new User();
		user.setEmail(userDetailsDTO.getEmail());
		// Encode the password
		user.setPassword(passwordEncoder.encode(userDetailsDTO.getPassword()));
		
		// Fetch roles from the database based on the role names in the request
		List<Role> roles = roleRepository.findByNameIn(userDetailsDTO.getRoles());
		
		if (roles.isEmpty()) {
			throw new RuntimeException("Invalid roles provided.");
		}
		 
		// Assign the roles to the user
		user.setRoles(roles);
		
		userRepository.save(user);
		return "User added and registered succesfully!";
	}

	@Transactional
	public boolean deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with id " + id));
		
		userRepository.delete(user);
		return true;
	}
	
	
	public long getUserCount() {
		return userRepository.count();
	}
	
	public long getAgentCount() {
		return userRepository.countUsersByRole("ROLE_AGENT");
	}

}
