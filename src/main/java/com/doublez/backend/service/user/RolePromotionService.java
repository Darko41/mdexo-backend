package com.doublez.backend.service.user;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.doublez.backend.config.security.SecurityConfig;
import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.agency.AgencyService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RolePromotionService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final AgencyService agencyService;
	private final UserMapper userMapper;
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	// FIXED: Add constructor injection
	public RolePromotionService(UserRepository userRepository, RoleRepository roleRepository,
			AgencyService agencyService, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.agencyService = agencyService;
		this.userMapper = userMapper;
	}

	public UserDTO promoteToAgent(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		Role agentRole = roleRepository.findByName("ROLE_AGENT").orElseGet(() -> {
			Role newRole = new Role();
			newRole.setName("ROLE_AGENT");
			return roleRepository.save(newRole);
		});

		if (!user.hasRole("ROLE_AGENT")) {
			user.getRoles().add(agentRole);
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
}