package com.doublez.backend.service.user;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.doublez.backend.controller.AdminApiController;
import com.doublez.backend.dto.InvestorProfileDTO;
import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.InvestorProfile;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.agency.AgencyService;
import com.doublez.backend.service.verification.UserVerificationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RolePromotionService {

	private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgencyService agencyService;
    private final UserMapper userMapper;
    private final UserVerificationService verificationService;
    
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);
    
    public RolePromotionService(UserRepository userRepository, RoleRepository roleRepository,
                              AgencyService agencyService, UserMapper userMapper,
                              UserVerificationService verificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.agencyService = agencyService;
        this.userMapper = userMapper;
        this.verificationService = verificationService;
    }

    // PROMOTION METHODS CHECK VERIFICATION
    public UserDTO promoteToAgent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // CHECK IF USER IS VERIFIED
        if (!verificationService.isUserVerifiedForRole(userId, "ROLE_AGENT")) {
            throw new RuntimeException("User must be verified before becoming an agent");
        }
        
        Role agentRole = roleRepository.findByName("ROLE_AGENT").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_AGENT");
            return roleRepository.save(newRole);
        });
        
        if (!user.hasRole("ROLE_AGENT")) {
            user.getRoles().add(agentRole);
            user.setTier(UserTier.FREE_AGENT); // SET APPROPRIATE TIER
            user.preUpdate();
            userRepository.save(user);
        }
        
        return userMapper.toDTO(user);
    }

	public UserDTO promoteToAgencyAdmin(Long userId, AgencyDTO.Create agencyDto) {
		logger.info("🔍 promoteToAgencyAdmin called - userId: {}, agencyDto: {}", userId, agencyDto);
	    
	    if (agencyDto != null) {
	    	logger.info("🔍 Agency name: '{}', description: '{}'", agencyDto.getName(), agencyDto.getDescription());
	    } else {
	    	logger.warn("⚠️ agencyDto is null!");
	    }
		
		User user = userRepository.findById(userId)
	        .orElseThrow(() -> new UserNotFoundException(userId));

	    // Ensure user is an agent first
	    if (!user.isAgent()) {
	        promoteToAgent(userId);
	        user = userRepository.findById(userId)
	            .orElseThrow(() -> new UserNotFoundException(userId));
	    }

	    Role agencyAdminRole = roleRepository.findByName("ROLE_AGENCY_ADMIN")
	        .orElseGet(() -> {
	            Role newRole = new Role();
	            newRole.setName("ROLE_AGENCY_ADMIN");
	            return roleRepository.save(newRole);
	        });

	    if (!user.hasRole("ROLE_AGENCY_ADMIN")) {
	        user.getRoles().add(agencyAdminRole);
	    }

	    User updatedUser = userRepository.save(user);

	    // Create agency for this user (optional - could let them create later)
	    if (agencyDto != null) {
	        agencyService.createAgency(agencyDto, updatedUser.getId());
	    }

	    return userMapper.toDTO(updatedUser);
	}

	public UserDTO demoteFromAgent(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		// Remove agent and agency admin roles, keep only USER role
		List<Role> newRoles = user.getRoles().stream()
				.filter(role -> !role.getName().equals("ROLE_AGENT") && !role.getName().equals("ROLE_AGENCY_ADMIN"))
				.collect(Collectors.toList());

		// Ensure they at least have USER role
		if (newRoles.isEmpty()) {
			Role userRole = roleRepository.findByName("ROLE_USER")
					.orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
			newRoles.add(userRole);
		}

		user.setRoles(newRoles);
		user.preUpdate();
		User updatedUser = userRepository.save(user);

		return userMapper.toDTO(updatedUser);
	}
	
	// INVESTOR METHODS
	public UserDTO promoteToInvestor(Long userId, InvestorProfileDTO investorProfileDto) {
        logger.info("🔍 promoteToInvestor called - userId: {}, investorProfileDto: {}", userId, investorProfileDto);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // CHECK IF USER IS VERIFIED
        if (!verificationService.isUserVerifiedForRole(userId, "ROLE_INVESTOR")) {
            throw new RuntimeException("User must be verified before becoming an investor");
        }
        
        // Add ROLE_INVESTOR if not present
        Role investorRole = roleRepository.findByName("ROLE_INVESTOR")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ROLE_INVESTOR");
                return roleRepository.save(newRole);
            });
        
        if (!user.hasRole("ROLE_INVESTOR")) {
            user.getRoles().add(investorRole);
        }
        
        // Set user tier to FREE_INVESTOR
        user.setTier(UserTier.FREE_INVESTOR);
        
        // Create or update investor profile
        if (investorProfileDto != null) {
            InvestorProfile investorProfile = user.getOrCreateInvestorProfile();
            userMapper.updateInvestorProfileFromDTO(investorProfileDto, investorProfile);
            user.setInvestorProfile(investorProfile);
        }
        
        User updatedUser = userRepository.save(user);
        
        return userMapper.toDTO(updatedUser);
    }
	
	// Investor-specific demotion
    public UserDTO demoteFromInvestor(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Remove investor role, keep other roles
        List<Role> newRoles = user.getRoles().stream()
            .filter(role -> !role.getName().equals("ROLE_INVESTOR"))
            .collect(Collectors.toList());
        
        // Ensure they at least have USER role
        if (newRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            newRoles.add(userRole);
        }
        
        user.setRoles(newRoles);
        user.setTier(UserTier.FREE_USER); // Default back to free user
        user.preUpdate();
        
        User updatedUser = userRepository.save(user);
        
        return userMapper.toDTO(updatedUser);
    }
}