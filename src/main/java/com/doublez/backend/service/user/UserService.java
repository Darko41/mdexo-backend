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

import com.doublez.backend.dto.CustomUserDetails;
import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserProfile;
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

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository,
			UserMapper userMapper, RealEstateRepository realEstateRepository, AgencyRepository agencyRepository,
			TrialService trialService, AgencyService agencyService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.userMapper = userMapper;
		this.realEstateRepository = realEstateRepository;
		this.agencyRepository = agencyRepository;
		this.trialService = trialService;
		this.agencyService = agencyService;
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
		user.setTier(createDto.getTier());

		// Role assignment with agency support
		List<String> rolesToAssign;
		if (isAdminOperation) {
			rolesToAssign = createDto.getRoles();
		} else {
			// Self-registration - check if agency data is provided
			if (createDto.getAgency() != null) {
				// User provided agency data -> make them AGENCY_ADMIN
				rolesToAssign = List.of("ROLE_USER", "ROLE_AGENCY_ADMIN");
			} else {
				// Regular user registration
				rolesToAssign = List.of("ROLE_USER");
			}
		}
		user.setRoles(resolveRoles(rolesToAssign));

		// Handle profile creation
		if (createDto.getProfile() != null) {
			UserProfile profile = new UserProfile();
			profile.setUser(user);
			updateProfileFromDTO(createDto.getProfile(), profile);
			user.setUserProfile(profile);
		}

		User savedUser = userRepository.save(user);

		// Create agency if agency data provided
		if (!isAdminOperation && createDto.getAgency() != null) {
			try {
				createAgencyForUser(createDto.getAgency(), savedUser);
			} catch (Exception e) {
				// Log but don't fail registration - user can create agency later
				logger.error("⚠️ Agency creation failed but user registration succeeded: {}", e.getMessage());
				// You might want to notify admin about this failure
			}
		}

		// START FREE TRIAL for non-admin registrations
		if (!isAdminOperation) {
			try {
				trialService.startTrial(savedUser);
			} catch (Exception e) {
				// Log the error but registration succeeded
				logger.error("⚠️ Trial service failed but registration completed: {}", e.getMessage());
			}
		}

		return userMapper.toDTO(savedUser);
	}

	// Agency creation helper method
	private void createAgencyForUser(AgencyDTO.Create agencyDto, User user) {
		// Validate agency data
		if (agencyDto.getName() == null || agencyDto.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Agency name is required");
		}
		if (agencyDto.getLicenseNumber() == null || agencyDto.getLicenseNumber().trim().isEmpty()) {
			throw new IllegalArgumentException("Agency license number is required");
		}

		// Check if agency name already exists
		if (agencyRepository.existsByName(agencyDto.getName())) {
			throw new IllegalOperationException("Agency name already exists: " + agencyDto.getName());
		}

		// Check if license number already exists
		if (agencyRepository.existsByLicenseNumber(agencyDto.getLicenseNumber())) {
			throw new IllegalOperationException("License number already exists: " + agencyDto.getLicenseNumber());
		}

		// Create agency
		Agency agency = new Agency(agencyDto.getName(), agencyDto.getDescription(), agencyDto.getLogo(),
				agencyDto.getContactEmail(), agencyDto.getContactPhone(), agencyDto.getWebsite(),
				agencyDto.getLicenseNumber(), user);

		Agency savedAgency = agencyRepository.save(agency);

		// Update user's owned agencies
		user.getOwnedAgencies().add(savedAgency);
		userRepository.save(user);

		logger.info("✅ Agency created successfully: {} for user: {}", agencyDto.getName(), user.getEmail());
	}

	// Consolidated update method
	public UserDTO updateUser(Long id, UserDTO.Update updateDto) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

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
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return userMapper.toDTO(user);
	}

	// Get all users returning UserDTO
	public List<UserDTO> getAllUsers() {
		return userRepository.findAll().stream().map(userMapper::toDTO).collect(Collectors.toList());
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

		// 1) Delete agencies owned by user (if any)
		List<Agency> ownedAgencies = agencyRepository.findByAdminId(userId);
		for (Agency agency : ownedAgencies) {
			// cascade = CascadeType.ALL on Agency should remove related data (memberships,
			// listings) if configured
			agencyRepository.delete(agency);
		}

		// 2) Delete user's real estate properties
		List<RealEstate> userProperties = realEstateRepository.findByUserId(userId);
		for (RealEstate property : userProperties) {
			realEstateRepository.delete(property);
		}

		// 3) Finally delete the user
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
		List<String> rolesToAssign = (roleNames == null || roleNames.isEmpty()) ? List.of("ROLE_USER") : roleNames;

		return rolesToAssign.stream().map(roleName -> roleRepository.findByName(roleName).orElseGet(() -> {
			Role newRole = new Role();
			newRole.setName(roleName);
			return roleRepository.save(newRole);
		})).collect(Collectors.toList());
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
	public Optional<AgencyDTO> getUserAgency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty()) {
            Agency agency = user.getOwnedAgencies().get(0);
            return Optional.of(agencyService.toDTO(agency));
        }
        
        return Optional.empty();
    }

	// Check if user has an agency
	public boolean userHasAgency(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty();
    }
	
	
}
