package com.doublez.backend.service.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.entity.UserProfile;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.SelfDeletionException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;	// TODO check if another package is required


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

    // Consolidated user registration
    public UserDTO registerUser(UserDTO.Create createDto, boolean isAdminOperation) {
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new EmailExistsException("Email already in use: " + createDto.getEmail());
        }

        validatePassword(createDto.getPassword());
        
        User user = new User();
        user.setEmail(createDto.getEmail());
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        
        // Role assignment logic
        if (isAdminOperation) {
            user.setRoles(resolveRoles(createDto.getRoles()));
        } else {
            // Self-registration - default to ROLE_USER only
            user.setRoles(resolveRoles(List.of("ROLE_USER")));
        }

        // Handle profile creation
        if (createDto.getProfile() != null) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            updateProfileFromDTO(createDto.getProfile(), profile);
            user.setUserProfile(profile);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    // Consolidated update method
    public UserDTO updateUser(Long id, UserDTO.Update updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        boolean wasUpdated = false;
        
        // Email update
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            user.setEmail(updateDto.getEmail());
            wasUpdated = true;
        }
        
        // Role update (admin only in practice)
        if (updateDto.getRoles() != null) {
            List<Role> newRoles = resolveRoles(updateDto.getRoles());
            if (!newRoles.equals(user.getRoles())) {
                user.setRoles(newRoles);
                wasUpdated = true;
            }
        }
        
        // Profile update
        if (updateDto.getProfile() != null) {
            UserProfile profile = user.getOrCreateProfile();
            updateProfileFromDTO(updateDto.getProfile(), profile);
            wasUpdated = true;
        }
        
        if (wasUpdated) {
            user.preUpdate();
            userRepository.save(user);
        }
        
        return userMapper.toDTO(user);
    }

    // Get user by ID returning UserDTO
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toDTO(user);
    }

    // Get all users returning UserDTO
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Simple registration for public use
    public String simpleRegister(String email, String password) {
        UserDTO.Create createDto = new UserDTO.Create();
        createDto.setEmail(email);
        createDto.setPassword(password);
        createDto.setRoles(List.of("ROLE_USER"));
        
        registerUser(createDto, false);
        return "User registered successfully!";
    }

    // Delete user (no changes needed)
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
            List<User> otherAdmins = userRepository.findByRoleName("ROLE_ADMIN")
                    .stream()
                    .filter(admin -> !admin.getId().equals(targetUser.getId()))
                    .collect(Collectors.toList());
            
            if (!otherAdmins.isEmpty()) {
                User replacementAdmin = otherAdmins.get(0);
                if (realEstateRepository.existsByOwner(targetUser)) {
                    realEstateRepository.reassignAllPropertiesFromUser(targetUser.getId(), replacementAdmin.getId());
                }
            }
        }

        // 4. Delete the user
        try {
            userRepository.delete(targetUser);
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

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    private void updateProfileFromDTO(UserProfileDTO profileDto, UserProfile profile) {
        if (profileDto.getFirstName() != null) {
            profile.setFirstName(profileDto.getFirstName());
        }
        if (profileDto.getLastName() != null) {
            profile.setLastName(profileDto.getLastName());
        }
        if (profileDto.getPhone() != null) {
            profile.setPhone(profileDto.getPhone());
        }
        if (profileDto.getBio() != null) {
            profile.setBio(profileDto.getBio());
        }
    }
}