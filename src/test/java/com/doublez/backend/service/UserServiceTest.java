package com.doublez.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
	void testRegisterUser_NoRolesProvided_ShouldAssignDefaultRole() {
		// Given: User DTO with no roles (null roles)
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("John Doe", null, "password123");
		
		// Mock repository calls
		when(userRepository.findByUsername(userDetailsDTO.getUsername())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(userDetailsDTO.getPassword())).thenReturn("encodedPassword123");
		
		// Mock role repository to ensure the "USER" role exists or is created
		Role defaultRole = new Role();
		defaultRole.setName("USER");
		when(roleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole)); // Ensure "USER" role exists
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> 
			invocation.getArgument(0)); // Return saved user
		
		// When: Registering user without roles
		String result = userService.registerUser(userDetailsDTO);
		
		// Then: Ensure the result is successful and the default role is assigned
		assertEquals("User registered successfully!", result);
		verify(userRepository).save(any(User.class)); // Ensure user is saved
		verify(roleRepository).findByName("USER"); // Ensure default role is checked
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
		
		// Check roles on the saved user
		verify(userRepository).save(argThat(user -> user.getRoles().size() == 2 &&
				user.getRoles().contains(userRole) && user.getRoles().contains(adminRole)));
	}
	
	@Test
	void testRegistrationUser_WithEmptyPassword() {
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("John Doe", List.of("USER"), "");
		
		String result = userService.registerUser(userDetailsDTO);
		
		assertEquals("Password cannot be empty", result); // Empty password leads to role-related validation failure
		verify(userRepository, never()).save(any(User.class)); // Save is not called if password is empty
	}
	
	@Test
	void testDeleteUser_NotFound() {
		String username = "John Doe";
		
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
		
		Exception exception = assertThrows(RuntimeException.class, () -> {
			userService.deleteUser(username);
		});
		
		assertEquals("User not found with username " + username, exception.getMessage());
	}
	
	@Test
	void testUpdateProfile_Success() {
		String username = "John Doe";
		User existinUser = new User();
		existinUser.setUsername(username);
		existinUser.setPassword("oldEncodedPassword");
		
		// New user details to update
		String newUsername = "John Doe";
		String newPassword = "newPassword";
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO(newUsername, null, newPassword);
		
		when(userRepository.findByUsername(newUsername)).thenReturn(Optional.of(existinUser));
		when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword123");
		when(userRepository.save(any(User.class))).thenAnswer(
				invocation -> invocation.getArgument(0)); // Return the updated user
		
		boolean result = userService.updateProfile(username, userDetailsDTO);
		
		assertTrue(result); // Ensure the result is true
		assertEquals(newUsername, existinUser.getUsername()); // Ensure username is updated
		assertEquals("newEncodedPassword123", existinUser.getPassword()); // Ensure password is encoded and updated
		
		verify(userRepository).save(existinUser); // Ensure the save method is called with updated user
	}
	
	@Test
	void testUpdateProfile_UserNotFOund() {
		String username = "NonExistentUser";
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO("NewUsername", null, "newPassword123");
		
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
		
		Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
			userService.updateProfile(username, userDetailsDTO);
		});
		
		assertEquals("User not found", exception.getMessage()); // Ensure exception is thrown with correct message
		verify(userRepository, never()).save(any(User.class)); // Ensure save method is not called
	}
	
	// Additional tests can be written for other edge cases (e.g. invalid role names, etc.)
}






































