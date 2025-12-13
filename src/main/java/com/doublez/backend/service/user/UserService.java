package com.doublez.backend.service.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.agency.AgencyCreateDTO;
import com.doublez.backend.dto.agency.AgencyResponseDTO;
import com.doublez.backend.dto.auth.CustomUserDetails;
import com.doublez.backend.dto.contractor.ContractorProfileUpdateDTO;
import com.doublez.backend.dto.investor.InvestorProfileUpdateDTO;
import com.doublez.backend.dto.owner.OwnerProfileUpdateDTO;
import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.dto.user.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.profile.ContractorProfile;
import com.doublez.backend.entity.profile.InvestorProfile;
import com.doublez.backend.entity.profile.OwnerProfile;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserProfile;
import com.doublez.backend.entity.user.UserRole;
import com.doublez.backend.exception.CustomAuthenticationException;
import com.doublez.backend.exception.EmailExistsException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.credit.CreditInitializationService;
import com.doublez.backend.service.usage.TrialService;

import jakarta.transaction.Transactional; // TODO check if another package is required

@Service
@Transactional
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final RealEstateRepository realEstateRepository;
	private final AgencyRepository agencyRepository;
	private final TrialService trialService;
	private final AgencyService agencyService;
	private final CreditInitializationService creditInitializationService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository,
			UserMapper userMapper, RealEstateRepository realEstateRepository, AgencyRepository agencyRepository,
			TrialService trialService, AgencyService agencyService, CreditInitializationService creditInitializationService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.userMapper = userMapper;
		this.realEstateRepository = realEstateRepository;
		this.agencyRepository = agencyRepository;
		this.trialService = trialService;
		this.agencyService = agencyService;
		this.creditInitializationService = creditInitializationService;
	}

	// Consolidated user registration
	public UserResponseDTO registerUser(UserCreateDTO createDto, boolean isAdminOperation) {
        // Validate email uniqueness
        if (userRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new EmailExistsException("Email already in use: " + createDto.getEmail());
        }

        // Validate password
        validatePassword(createDto.getPassword());

        // Create user entity from DTO
        User user = userMapper.toEntityFromCreateDTO(createDto);
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        
        // Role assignment - users register with ONE primary role
        UserRole primaryRole = createDto.getRole();
        List<String> rolesToAssign;
        
        if (isAdminOperation) {
            // Admin can assign any role during creation
            rolesToAssign = createDto.getRolesAsList();
        } else {
            // Self-registration - use the single role from DTO
            rolesToAssign = createDto.getRolesAsList();
        }
        
        user.setRoles(resolveRoles(rolesToAssign));

        // Validate role-specific data consistency
        if (!createDto.hasValidRoleDataCombination()) {
            throw new IllegalArgumentException("Invalid role data combination provided");
        }

        // Save user first
        User savedUser = userRepository.save(user);
        
        // 🆕 INITIALIZE USER CREDITS
        try {
            creditInitializationService.initializeUserCredits(savedUser);
        } catch (Exception e) {
            logger.error("⚠️ Credit initialization failed but registration succeeded: {}", e.getMessage());
        }

        // Handle role-specific profile creation
        createRoleSpecificProfile(createDto, savedUser);

        // Handle agency creation for AGENCY role
        if (primaryRole == UserRole.AGENCY && createDto.getAgency() != null) {
            try {
                createAgencyForUser(createDto.getAgency(), savedUser);
            } catch (Exception e) {
                logger.error("⚠️ Agency creation failed but user registration succeeded: {}", e.getMessage());
                // User can create agency later via profile
            }
        }

        // Start trial for non-admin registrations
        if (!isAdminOperation) {
            try {
                trialService.startTrial(savedUser);
            } catch (Exception e) {
                logger.error("⚠️ Trial service failed but registration completed: {}", e.getMessage());
            }
        }

        return userMapper.toResponseDTO(savedUser);
    }
	
	// Create role-specific profiles
	private void createRoleSpecificProfile(UserCreateDTO createDto, User user) {
        switch (createDto.getRole()) {
            case INVESTOR:
                if (createDto.getInvestorProfile() != null) {
                    InvestorProfile profile = userMapper.toInvestorProfileEntity(createDto.getInvestorProfile(), user);
                    user.setInvestorProfile(profile);
                }
                break;
            case OWNER:
                if (createDto.getOwnerProfile() != null) {
                    OwnerProfile profile = userMapper.toOwnerProfileEntity(createDto.getOwnerProfile(), user);
                    user.setOwnerProfile(profile);
                }
                break;
            case CONTRACTOR:
                if (createDto.getContractorProfile() != null) {
                    ContractorProfile profile = userMapper.toContractorProfileEntity(createDto.getContractorProfile(), user);
                    user.setContractorProfile(profile);
                }
                break;
            case AGENCY:
                // Agency profile is handled separately via Agency entity
                break;
            case USER:
            case BUSINESS:
                // No additional profiles needed
                break;
        }
        
        if (createDto.getRole() != UserRole.USER && createDto.getRole() != UserRole.BUSINESS) {
            userRepository.save(user); // Save profile changes
        }
    }

	// Agency creation helper method
	private void createAgencyForUser(AgencyCreateDTO agencyDto, User user) {
        // Validate user has AGENCY role
        if (!user.isAgencyAdmin()) {
            throw new IllegalOperationException("Only agency users can create agencies");
        }

        // Validate agency data
        if (agencyDto.getName() == null || agencyDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Agency name is required");
        }

        // Check if agency name already exists
        if (agencyRepository.existsByName(agencyDto.getName())) {
            throw new IllegalOperationException("Agency name already exists: " + agencyDto.getName());
        }

        // Create agency using the static factory method
        Agency agency = Agency.fromCreateDto(agencyDto, user);
        Agency savedAgency = agencyRepository.save(agency);

        // Set the agency relationship (now OneToOne)
        user.setOwnedAgency(savedAgency);
        userRepository.save(user);

        logger.info("✅ Agency created successfully: {} for user: {}", agencyDto.getName(), user.getEmail());
    }
	
	// Update user method - only personal info, no email/role
    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        boolean wasUpdated = false;

        // Only update personal info - email and role require separate endpoints
        if (updateDto.getFirstName() != null && !updateDto.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(updateDto.getFirstName());
            wasUpdated = true;
        }
        if (updateDto.getLastName() != null && !updateDto.getLastName().equals(user.getLastName())) {
            user.setLastName(updateDto.getLastName());
            wasUpdated = true;
        }
        if (updateDto.getPhone() != null && !updateDto.getPhone().equals(user.getPhone())) {
            user.setPhone(updateDto.getPhone());
            wasUpdated = true;
        }
        if (updateDto.getBio() != null && !updateDto.getBio().equals(user.getBio())) {
            user.setBio(updateDto.getBio());
            wasUpdated = true;
        }

        if (wasUpdated) {
            user.preUpdate();
            userRepository.save(user);
        }

        return userMapper.toResponseDTO(user);
    }
    
    // Separate method for email update
    public UserResponseDTO updateUserEmail(Long id, String newEmail) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailExistsException("Email already in use: " + newEmail);
        }
        
        user.setEmail(newEmail);
        user.preUpdate();
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }
    
    // Separate method for role update (admin only)
    public UserResponseDTO updateUserRole(Long id, UserRole newRole) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        
        List<String> newRoles = List.of("ROLE_" + newRole.name());
        user.setRoles(resolveRoles(newRoles));
        user.preUpdate();
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }
	
	// Update user profile specifically
    public UserResponseDTO updateUserProfile(Long id, UserUpdateDTO updateDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        
        userMapper.updateEntityFromUpdateDTO(updateDto, user);
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }
    
    // Update role-specific profiles
    public UserResponseDTO updateInvestorProfile(Long userId, InvestorProfileUpdateDTO updateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.isInvestor()) {
            throw new IllegalOperationException("User is not an investor");
        }
        
        InvestorProfile profile = user.getOrCreateInvestorProfile();
        userMapper.updateInvestorProfileFromUpdateDTO(updateDto, profile);
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }
    
    public UserResponseDTO updateOwnerProfile(Long userId, OwnerProfileUpdateDTO updateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.isOwner()) {
            throw new IllegalOperationException("User is not a property owner");
        }
        
        OwnerProfile profile = user.getOrCreateOwnerProfile();
        userMapper.updateOwnerProfileFromUpdateDTO(updateDto, profile);
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }
    
    public UserResponseDTO updateContractorProfile(Long userId, ContractorProfileUpdateDTO updateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!user.isContractor()) {
            throw new IllegalOperationException("User is not a contractor");
        }
        
        ContractorProfile profile = user.getOrCreateContractorProfile();
        userMapper.updateContractorProfileFromUpdateDTO(updateDto, profile); // FIXED: Use UpdateDTO method
        userRepository.save(user);
        
        return userMapper.toResponseDTO(user);
    }

	// Get user by ID returning UserResponseDTO
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponseDTO(user);
    }

    // Get all users returning UserResponseDTO
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

	// Delete user with cascade deletion - ALLOWS SELF-DELETION FOR EVERYONE
	@Transactional
	public void deleteUser(Long id, Authentication authentication) {
		CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
		User currentUser = userRepository.findById(currentUserDetails.getId())
				.orElseThrow(() -> new UserNotFoundException(currentUserDetails.getId()));

		User targetUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		boolean isSuperAdmin = currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
		boolean isSelfDeletion = currentUser.getId().equals(targetUser.getId());

		// Allow deletion if user is deleting themselves OR admin is deleting any user
		if (!isSelfDeletion && !isSuperAdmin) {
			throw new IllegalOperationException("You can only delete your own account");
		}

		// Handle cascade deletion - pass true for super admin, false for self-deletion
		deleteUserWithCascade(id, isSuperAdmin);
	}

	// Delete user with all associated data
    public void deleteUserWithCascade(Long userId, boolean isSuperAdmin) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        // 1) Delete agency owned by user (if any)
        user.getOwnedAgency().ifPresent(agency -> {
            agencyRepository.delete(agency);
        });

        // 2) Delete user's real estate properties
        List<RealEstate> userProperties = realEstateRepository.findByUserId(userId);
        for (RealEstate property : userProperties) {
            realEstateRepository.delete(property);
        }

        // 3) Delete role-specific profiles (cascade should handle this, but explicit for clarity)
        if (user.getInvestorProfile() != null) {
            // Cascade should delete this
        }
        if (user.getOwnerProfile() != null) {
            // Cascade should delete this
        }
        if (user.getContractorProfile() != null) {
            // Cascade should delete this
        }

        // 4) Finally delete the user
        userRepository.delete(user);
    }

	public long getUserCount() {
		return userRepository.count();
	}

	// Authentication helpers
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
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomAuthenticationException("User not authenticated");
		}

		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' not found")).getId();
	}
	
	// Get current user (return ResponseDTO)
    public UserResponseDTO getCurrentUser() {
        User user = getAuthenticatedUser();
        return userMapper.toResponseDTO(user);
    }
    
    // NEW: Get users by role
    public List<UserResponseDTO> getUsersByRole(UserRole role) {
        String roleName = "ROLE_" + role.name();
        List<User> users = userRepository.findByRole(roleName);
        return users.stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    // NEW: Count users by role
    public long countUsersByRole(UserRole role) {
        String roleName = "ROLE_" + role.name();
        return userRepository.countByRole(roleName);
    }

	public User getUserEntityById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	public boolean hasAdminRole(String email) {
		return userRepository.findByEmail(email)
				.map(user -> user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName())))
				.orElse(false);
	}

	public boolean isAdmin(User user) {
		return user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
	}

	public User getUserEntityByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
	}

	// Helper methods
	private List<Role> resolveRoles(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of("ROLE_USER");
        }

        return roleNames.stream()
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

	@Transactional
	public void deleteUserAsAdmin(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		// Admin can delete any user, so we pass isAdmin = true
		deleteUserWithCascade(userId, true);
	}

	public boolean isCurrentUserAdmin() {
		Long currentUserId = getCurrentUserId();
		User currentUser = userRepository.findById(currentUserId)
				.orElseThrow(() -> new UserNotFoundException(currentUserId));
		return isAdmin(currentUser);
	}

	public long countImages(Long userId) {
		// Fetch properties owned by user and sum image list sizes
		List<RealEstate> props = realEstateRepository.findByUserId(userId);
		if (props == null || props.isEmpty()) {
			return 0L;
		}
		return props.stream().mapToLong(p -> p.getImages() == null ? 0L : p.getImages().size()).sum();
	}

	// Helper method to get user's agency (if any)
	public Optional<AgencyResponseDTO> getUserAgency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return user.getOwnedAgency()
                .map(agency -> agency.toResponseDTO());
    }

	// Check if user has an agency
	public boolean userHasAgency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.isAgencyAdmin() && user.getOwnedAgency().isPresent();
    }
	
	
}
