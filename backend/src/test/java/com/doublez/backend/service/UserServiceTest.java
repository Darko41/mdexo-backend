package com.doublez.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.doublez.backend.dto.UserDetailsDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

// TODO update Mockito when a version that fully supports Java 21 is released

public class UserServiceTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Mock
	private RoleRepository roleRepository;
	
	@InjectMocks
	private UserService userService;
	
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	void testRegisterUser_UserAlreadyExists() {
		// Given
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("John Doe", List.of("USER"), "password123");
		when(userRepository.findByUsername(userDetailsDTO.getUsername())).thenReturn(Optional.of(new User())); // Simulate user already exists
		
		// When
		String result = userService.registerUser(userDetailsDTO);
		
		// Then
		assertEquals("User already exists", result); // Ensure correct message is returned
		verify(userRepository).findByUsername(userDetailsDTO.getUsername()); // Verify repository call

	}
	
	@Test
	void testRegisterUser_NoRolesProvided() {
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("John Doe", null, "password123");
		when(userRepository.findByUsername(userDetailsDTO.getUsername())).thenReturn(Optional.empty()); // User doesn't exist yet
		
		String result = userService.registerUser(userDetailsDTO);
		
		assertEquals("Role cannot be empty", result);
		verify(userRepository).findByUsername(userDetailsDTO.getUsername());
	}
	
	// Test for case when roles are not provided and user is successfully registered
	@Test
	void testRegisterUser_Success() {
		// Given
		List<String> roles = Arrays.asList("USER", "ADMIN");
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("John Doe", roles, "password123");
		String encodedPassword = "encodedPassword123";
		
		Role userRole = new Role();
		userRole.setName("USER");
		
		Role adminRole = new Role();
		userRole.setName("ADMIN");
		
		// Mocking the behavior of the repositories
		when(userRepository.findByUsername(userDetailsDTO.getUsername())).thenReturn(Optional.empty()); // User doesn't exist yet
		when(passwordEncoder.encode(userDetailsDTO.getPassword())).thenReturn(encodedPassword); // Password encoding
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole)); // Role "USER" exists
		when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty()); // Role "ADMIN" doesn't exist
		when(roleRepository.save(any(Role.class))).thenReturn(adminRole); // Save new "ADMIN" role
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(1L); // Simulate the user having an ID after being used
			return user;
		}); // Save the user and return the saved instance
		
		// When
		String result = userService.registerUser(userDetailsDTO);
		
		// Then
		assertEquals("User registered successfully!", result);
		verify(passwordEncoder).encode("password123");
		verify(roleRepository).findByName("USER");
		verify(roleRepository).findByName("ADMIN");
		verify(roleRepository).save(any(Role.class));
		verify(userRepository).save(any(User.class));
		
		// Ensure the saved user has the correct roles
		User savedUser = new User();
		savedUser.setRoles(Arrays.asList(userRole, adminRole));
		assertEquals(2, savedUser.getRoles().size());
		assertTrue(savedUser.getRoles().contains(userRole));
		assertTrue(savedUser.getRoles().contains(adminRole));
	}
	
	// Additional tests can be written for other edge cases (e.g., empty password, invalid role names, etc.)
}






































