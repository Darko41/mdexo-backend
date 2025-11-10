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

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.entity.UserProfile;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.AgencyMembership;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.SelfDeletionException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.AgencyMembershipRepository;
import com.doublez.backend.repository.AgencyRepository;
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
    private final AgencyRepository agencyRepository;
    private final AgencyMembershipRepository membershipRepository;

    public UserService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     RoleRepository roleRepository,
                     UserMapper userMapper,
                     RealEstateRepository realEstateRepository,
                     AgencyRepository agencyRepository,
                     AgencyMembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.realEstateRepository = realEstateRepository;
        this.agencyRepository = agencyRepository;
        this.membershipRepository = membershipRepository;
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

    // Delete user with cascade deletion - ALLOWS SELF-DELETION FOR EVERYONE
    @Transactional
    public void deleteUser(Long id, Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new UserNotFoundException(currentUserDetails.getId()));
        
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        boolean isSelfDeletion = currentUser.getId().equals(targetUser.getId());

        System.out.println("🔍 Delete User Check - Current User: " + currentUser.getId() + 
                          ", Target User: " + targetUser.getId() + 
                          ", isSuperAdmin: " + isSuperAdmin + 
                          ", isSelfDeletion: " + isSelfDeletion);

        // SIMPLIFIED LOGIC: Allow deletion if user is deleting themselves OR admin is deleting any user
        if (!isSelfDeletion && !isSuperAdmin) {
            throw new IllegalOperationException("You can only delete your own account");
        }

        // Handle cascade deletion - pass true for super admin, false for self-deletion
        // This controls whether agency deletion is blocked for non-admins
        deleteUserWithCascade(id, isSuperAdmin);
    }

    // Delete user with all associated data
    public void deleteUserWithCascade(Long userId, boolean isSuperAdmin) {
        System.out.println("🔍 Starting cascade deletion for user: " + userId + ", isSuperAdmin: " + isSuperAdmin);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        System.out.println("🔍 User found: " + user.getEmail());

        // Check if user owns agencies
        List<Agency> ownedAgencies = agencyRepository.findByAdminId(userId);
        System.out.println("🔍 Found " + ownedAgencies.size() + " owned agencies");
        
        // REMOVED THE RESTRICTION - Allow users to delete themselves even if they own agencies
        // The agencies will be deleted along with the user
        
        // Delete owned agencies (cascade will handle memberships)
        for (Agency agency : ownedAgencies) {
            System.out.println("🗑️ Deleting agency: " + agency.getName() + " owned by user: " + userId);
            agencyRepository.delete(agency);
        }
        
        // Delete user's real estate properties
        List<RealEstate> userProperties = realEstateRepository.findByUserId(userId);
        System.out.println("🔍 Found " + userProperties.size() + " user properties");
        
        for (RealEstate property : userProperties) {
            System.out.println("🗑️ Deleting property: " + property.getTitle() + " owned by user: " + userId);
            realEstateRepository.delete(property);
        }
        
        // Delete user's agency memberships
        List<AgencyMembership> userMemberships = membershipRepository.findByUserId(userId);
        System.out.println("🔍 Found " + userMemberships.size() + " user memberships");
        
        membershipRepository.deleteAll(userMemberships);
        
        // Finally delete the user
        System.out.println("🗑️ Deleting user: " + userId);
        userRepository.delete(user);
        
        System.out.println("✅ Successfully deleted user: " + userId + " and all associated data");
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
    
    @Transactional
    public void deleteUserAsAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Admin can delete any user, so we pass isAdmin = true
        deleteUserWithCascade(userId, true);
    }
}