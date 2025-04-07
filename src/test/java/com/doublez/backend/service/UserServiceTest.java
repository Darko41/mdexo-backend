package com.doublez.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

// TODO update Mockito when a version that fully supports Java 21 is released

class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_WhenUserExists_ShouldReturnErrorMessage() {
        UserCreateDTO createDto = new UserCreateDTO("existing@test.com", "password123", List.of("ROLE_USER"));
        when(userRepository.findByEmail(createDto.getEmail())).thenReturn(Optional.of(new User()));
        
        String result = userService.registerUser(createDto);
        
        assertEquals("User already exists with this email", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_WhenNoRolesProvided_ShouldAssignDefaultRole() {
        // Arrange
        UserCreateDTO createDto = new UserCreateDTO("new@test.com", "password123", null);
        Role defaultRole = createTestRole("ROLE_USER");
        User user = new User();
        user.setEmail(createDto.getEmail());
        user.setPassword("encodedPass");
        user.setRoles(List.of(defaultRole));
        
        when(userRepository.findByEmail(createDto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPass");
        when(userMapper.toEntity(createDto)).thenReturn(user); // Add this mock
        when(userRepository.save(user)).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        
        // Act
        String result = userService.registerUser(createDto);
        
        // Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository).save(argThat(savedUser -> 
            savedUser.getRoles() != null &&
            savedUser.getRoles().size() == 1 &&
            savedUser.getRoles().get(0).getName().equals("ROLE_USER")
        ));
    }

    @Test
    void registerUser_WhenPasswordInvalid_ShouldThrowException() {
        UserCreateDTO createDto = new UserCreateDTO("test@test.com", "123", List.of("ROLE_USER"));
        
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(createDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenValid_ShouldReturnUserResponse() {
        // Arrange
        UserCreateDTO createDto = new UserCreateDTO("new@test.com", "password123", List.of("ROLE_USER"));
        User user = new User();
        user.setId(1L);
        user.setEmail(createDto.getEmail());
        user.setPassword("encodedPass");
        user.setRoles(List.of(createTestRole("ROLE_USER")));
        
        UserResponseDTO responseDto = createTestUserResponseDTO();
        
        when(userRepository.findByEmail(createDto.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(createTestRole("ROLE_USER")));
        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPass");
        when(userMapper.toEntity(createDto)).thenReturn(user); // Add this mock
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);
        
        // Act
        UserResponseDTO result = userService.createUser(createDto);
        
        // Assert
        assertEquals(responseDto, result);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        UserCreateDTO createDto = new UserCreateDTO("existing@test.com", "password123", List.of("ROLE_USER"));
        when(userRepository.findByEmail(createDto.getEmail())).thenReturn(Optional.of(new User()));
        
        assertThrows(EmailExistsException.class, () -> userService.createUser(createDto));
    }
    
    

    @Test
    void updateUserProfile_WhenValid_ShouldUpdateUser() {
        Long userId = 1L;
        UserUpdateDTO updateDto = new UserUpdateDTO();
        updateDto.setEmail("new@email.com");
        updateDto.setPassword("newPassword");
        
        User user = new User();
        user.setId(userId);
        user.setEmail("old@email.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updateDto.getPassword())).thenReturn("encodedPass");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(createTestUserResponseDTO());
        
        UserResponseDTO result = userService.updateUserProfile(userId, updateDto);
        
        assertEquals("new@email.com", user.getEmail());
        assertEquals("encodedPass", user.getPassword());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(result);
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserResponseDTO responseDto = createTestUserResponseDTO();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);
        
        UserResponseDTO result = userService.getUserById(userId);
        
        assertEquals(responseDto, result);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toResponseDto(user1)).thenReturn(new UserResponseDTO(1L, "user1@test.com", List.of(), null, null));
        when(userMapper.toResponseDto(user2)).thenReturn(new UserResponseDTO(2L, "user2@test.com", List.of(), null, null));
        
        List<UserResponseDTO> result = userService.getAllUsers();
        
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        userService.deleteUser(userId);
        
        verify(userRepository).delete(user);
    }

    @Test
    void getAuthenticatedUser_WhenNotAuthenticated_ShouldThrowException() {
        SecurityContextHolder.clearContext();
        
        assertThrows(CustomAuthenticationException.class, () -> userService.getAuthenticatedUser());
    }

    @Test
    void getUserCount_ShouldReturnCount() {
        when(userRepository.count()).thenReturn(10L);
        assertEquals(10L, userService.getUserCount());
    }

    @Test
    void getAgentCount_ShouldReturnCount() {
        when(userRepository.countUsersByRole("ROLE_AGENT")).thenReturn(5L);
        assertEquals(5L, userService.getAgentCount());
    }

    // Helper methods
    private UserResponseDTO createTestUserResponseDTO() {
        return new UserResponseDTO(
            1L,
            "test@test.com",
            List.of("ROLE_USER"),
            LocalDate.now(),
            LocalDate.now()
        );
    }

    private Role createTestRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}
