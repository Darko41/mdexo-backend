package com.doublez.backend.service.user;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.management.relation.RoleNotFoundException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.user.AdminUserCreateDTO;
import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.dto.user.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.InvalidRoleException;
import com.doublez.backend.exception.SelfDeletionException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.realestate.RealEstateImageService;

import jakarta.transaction.Transactional;	// TODO check if another package is required

// This class is for registration (encoding passwords, saving users)

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RealEstateRepository realEstateRepository;

    public UserService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     RoleRepository roleRepository,
                     UserMapper userMapper,
                     RealEstateRepository realEstateRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.realEstateRepository = realEstateRepository;
    }
	
//	private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
	
	@Transactional
    public String registerUser(UserCreateDTO createDto) {
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            return "User already exists with this email";
        }
        
        validatePassword(createDto.getPassword());
        
        User user = userMapper.toEntity(createDto);
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        user.setRoles(resolveRoles(createDto.getRoles()));
        
        userRepository.save(user);
        return "User registered successfully!";
    }

    public UserResponseDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return userMapper.toResponseDto(user);
    }
    
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponseDto(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public String addUser(UserCreateDTO createDto) {
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new EmailExistsException("User already exists");
        }
        
        List<Role> roles = validateAndGetRoles(createDto.getRoles());
        
        User user = userMapper.toEntity(createDto);
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        user.setRoles(roles);
        
        userRepository.save(user);
        return "User added successfully!";
    }

    @Transactional
    public void deleteUser(Long id) {
        User currentUser = getAuthenticatedUser();
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // 1. Prevent self-deletion
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new SelfDeletionException("Self-deletion is not allowed", 
                Map.of("currentUserId", currentUser.getId()));
        }

        // 2. Prevent removing last admin
        long adminCount = userRepository.countByRoles_Name("ROLE_ADMIN");
        if (targetUser.isAdmin() && adminCount <= 1) {
            throw new IllegalOperationException("System must have at least one admin");
        }
        
        // 3. Handle property reassignment for admins
        if (targetUser.isAdmin()) {
            // Find all admins excluding the target user
            List<User> otherAdmins = userRepository.findByRoleName("ROLE_ADMIN")
                    .stream()
                    .filter(admin -> !admin.getId().equals(targetUser.getId()))
                    .collect(Collectors.toList());
            
            if (!otherAdmins.isEmpty()) {
                // Use the first available admin as replacement
                User replacementAdmin = otherAdmins.get(0);
                
                // Reassign properties only if the admin owns any properties
                if (realEstateRepository.existsByOwner(targetUser)) {
                    realEstateRepository.reassignAllPropertiesFromUser(targetUser.getId(), replacementAdmin.getId());
                    System.out.println("✅ Reassigned properties from user " + targetUser.getId() + " to admin " + replacementAdmin.getId());
                }
            } else {
                // This should not happen due to the admin count check above
                throw new IllegalOperationException("No replacement admin available for property reassignment");
            }
        }

        // 4. Delete the user
        try {
            userRepository.delete(targetUser);
            System.out.println("✅ Successfully deleted user: " + targetUser.getEmail());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalOperationException("Cannot delete user: may be referenced by existing properties");
        }
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
    
    public long getAgentCount() {
        return userRepository.countUsersByRole("ROLE_AGENT");
    }
    
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomAuthenticationException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO createDto) {
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new EmailExistsException("Email already in use: " + createDto.getEmail());
        }
        
        validatePassword(createDto.getPassword());
        
        User user = userMapper.toEntity(createDto);
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        user.setRoles(resolveRoles(createDto.getRoles()));
        
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }

    // Helper methods
    private List<Role> resolveRoles(List<String> roleNames) {
        List<String> rolesToAssign = (roleNames == null || roleNames.isEmpty()) 
            ? List.of("ROLE_USER") 
            : roleNames;
            
        return rolesToAssign.stream()
            .map(roleName -> roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                }))
            .collect(Collectors.toList());
    }

    private List<Role> validateAndGetRoles(List<String> roleNames) {
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            throw new InvalidRoleException("Invalid roles provided");
        }
        return roles;
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    // === UPDATED METHOD - Replace your existing updateUserProfile ===
    @Transactional
    public UserResponseDTO updateUserProfile(Long id, UserUpdateDTO updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        boolean wasUpdated = updateUserFields(user, updateDto);
        
        if (wasUpdated) {
            user.preUpdate(); // Changed from setUpdatedAt
            userRepository.save(user);
        }
        
        return userMapper.toResponseDto(user);
    }

    // === UPDATED METHOD - Replace your existing updateUserFields ===
    private boolean updateUserFields(User user, UserUpdateDTO updateDto) {
        boolean wasUpdated = false;
        
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDto.getEmail());
            wasUpdated = true;
        }
        
        if (updateDto.getRoles() != null) {
            List<Role> newRoles = updateDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
                .collect(Collectors.toList());
            
            if (!newRoles.equals(user.getRoles())) {
                user.setRoles(newRoles);
                wasUpdated = true;
            }
        }
        
        // === NEW: Profile updates ===
        if (updateDto.getProfile() != null) {
            userMapper.updateEntity(updateDto, user);
            wasUpdated = true;
        }
        
        return wasUpdated;
    }
    
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
    
    @Transactional
    public UserResponseDTO createUserWithAdminPrivileges(AdminUserCreateDTO adminCreateDto) {
        // Email existence check
        if (userRepository.findByEmail(adminCreateDto.getEmail()).isPresent()) {
            throw new EmailExistsException("Email already in use: " + adminCreateDto.getEmail());
        }

        // Role validation and conversion
        List<Role> roles = adminCreateDto.getRoles().stream()
            .map(roleName -> roleRepository.findByName(roleName))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        
        if (roles.isEmpty()) {
            throw new InvalidRoleException("No valid roles provided");
        }

        // User creation
        User user = new User();
        user.setEmail(adminCreateDto.getEmail());
        user.setPassword(passwordEncoder.encode(
            adminCreateDto.getPassword() != null ? 
            adminCreateDto.getPassword() : 
            generateTemporaryPassword()
        ));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }
    
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' not found"))
            .getId();
    }
    
    public User getUserEntityById(Long id) {
    	return userRepository.findById(id)
    			.orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public boolean hasAdminRole(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> "ROLE_ADMIN".equals(role.getName())))
                .orElse(false);
    }

    public boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
    }
    
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
    
    // === NEW METHODS - Add these at the end ===
    
    // Update profile separately
    @Transactional
    public UserResponseDTO updateUserProfileDetails(Long id, UserProfileDTO profileDto) {
    	User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // This will create profile if it doesn't exist
        userMapper.updateUserProfile(profileDto, user);
        user.preUpdate();
        userRepository.save(user);
        
        return userMapper.toResponseDto(user);
    }

    // Get user profile
    public UserProfileDTO getUserProfile(Long userId) {
    	User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (user.getUserProfile() == null) {
            return new UserProfileDTO(); // Return empty DTO
        }
        
        return userMapper.toProfileDto(user.getUserProfile());
    }
}