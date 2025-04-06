package com.doublez.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.relation.RoleNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;	// TODO check if another package is required

// This class is for registration (encoding passwords, saving users)

@Service
@Transactional
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RoleRepository roleRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
	
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
	
	public UserDetailsDTO getUserById(Long id) {
	    User user = userRepository.findById(id)
	        .orElseThrow(() -> new UserNotFoundException(id));
	    
	    return new UserDetailsDTO(
	        user.getId(),
	        user.getEmail(),
	        user.getRoles().stream().map(Role::getName).collect(Collectors.toList()),
	        user.getCreatedAt(),
	        user.getUpdatedAt()
	    );
	}

	public boolean updateProfile(Long id, UserUpdateDTO updateDTO) {
        // 1. Fetch user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // 2. Track changes
        boolean wasUpdated = false;
        
        // 3. Update email (if provided and different)
        if (updateDTO.getEmail() != null 
                && !updateDTO.getEmail().isEmpty()
                && !updateDTO.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDTO.getEmail());
            wasUpdated = true;
        }
        
        // 4. Update password (if provided and different)
        if (updateDTO.getPassword() != null 
                && !updateDTO.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updateDTO.getPassword());
            if (!encodedPassword.equals(user.getPassword())) {
                user.setPassword(encodedPassword);
                wasUpdated = true;
            }
        }
        
        // 5. Persist changes if any
        if (wasUpdated) {
            user.setUpdatedAt(LocalDate.now());
            userRepository.save(user);
        }
        
        return wasUpdated;
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
	
	public User getAuthenticatedUser()  {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomAuthenticationException("User not authenticated");
		}
		return userRepository.findByEmail(authentication.getName())
	                .orElseThrow(() -> new UsernameNotFoundException("User not found: "));
	}

	@Transactional
	public UserResponseDTO createUser(UserCreateDTO createDto) {
	    // Reuse existing validation logic
	    if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
	        throw new EmailExistsException("Email already in use: " + createDto.getEmail());
	    }
	    
	    if (createDto.getPassword() == null || createDto.getPassword().isEmpty()) {
	        throw new IllegalArgumentException("Password cannot be empty");
	    }
	    
	    // Process roles - using your existing Role entity pattern
	    List<String> roleNames = createDto.getRoles() != null && !createDto.getRoles().isEmpty() 
	        ? createDto.getRoles() 
	        : List.of("ROLE_USER");
	    
	    List<Role> roles = roleNames.stream()
	        .map(roleName -> roleRepository.findByName(roleName)
	            .orElseGet(() -> {
	                Role newRole = new Role();
	                newRole.setName(roleName);
	                return roleRepository.save(newRole);
	            }))
	        .collect(Collectors.toList());

	    // Create and save user
	    User user = new User();
	    user.setEmail(createDto.getEmail());
	    user.setPassword(passwordEncoder.encode(createDto.getPassword()));
	    user.setRoles(roles);
	    
	    User savedUser = userRepository.save(user);

	    // Convert to response DTO
	    return new UserResponseDTO(
	        savedUser.getId(),
	        savedUser.getEmail(),
	        savedUser.getRoles().stream()
	            .map(Role::getName)
	            .collect(Collectors.toList()),
	        savedUser.getCreatedAt(),
	        savedUser.getUpdatedAt()
	    );
	}
	
}
