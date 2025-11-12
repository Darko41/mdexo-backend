package com.doublez.backend.service.agency;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.agent.AgencyMembershipDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.AgencyMembership;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.exception.agent.AgencyNotFoundException;
import com.doublez.backend.exception.agent.MembershipNotFoundException;
import com.doublez.backend.repository.AgencyMembershipRepository;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AgencyService {

	private final AgencyRepository agencyRepository;
	private final AgencyMembershipRepository membershipRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	// FIXED: Add constructor injection
	public AgencyService(AgencyRepository agencyRepository, AgencyMembershipRepository membershipRepository,
			UserRepository userRepository, UserService userService) {
		this.agencyRepository = agencyRepository;
		this.membershipRepository = membershipRepository;
		this.userRepository = userRepository;
		this.userService = userService;
	}

	public AgencyDTO createAgency(AgencyDTO.Create createDto, Long adminUserId) {
		User admin = userRepository.findById(adminUserId).orElseThrow(() -> new UserNotFoundException(adminUserId));

		if (!admin.hasRole("ROLE_AGENCY_ADMIN")) {
			throw new IllegalOperationException("User must have ROLE_AGENCY_ADMIN to create agency");
		}

		// Check if agency name already exists
		if (agencyRepository.existsByName(createDto.getName())) {
			throw new IllegalOperationException("Agency name already exists: " + createDto.getName());
		}

		Agency agency = new Agency(createDto.getName(), createDto.getDescription(), admin);
		agency.setLogo(createDto.getLogo());
		agency.setContactInfo(createDto.getContactInfo());

		Agency savedAgency = agencyRepository.save(agency);

		return mapToDTO(savedAgency);
	}

	public AgencyMembershipDTO applyToAgency(Long agencyId, Long userId) {
		Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new AgencyNotFoundException(agencyId));
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		if (!user.isAgent()) {
			throw new IllegalOperationException("User must be an agent to join agency");
		}

		// CHECK: User already has an active agency membership
		boolean hasActiveMembership = membershipRepository.existsByUserIdAndStatus(userId,
				AgencyMembership.MembershipStatus.ACTIVE);

		if (hasActiveMembership) {
			throw new IllegalOperationException("Agent can only be a member of one agency at a time");
		}

		// CHECK: Already applied or is a member
		if (membershipRepository.existsByUserIdAndAgencyIdAndStatus(userId, agencyId,
				AgencyMembership.MembershipStatus.PENDING)
				|| membershipRepository.existsByUserIdAndAgencyIdAndStatus(userId, agencyId,
						AgencyMembership.MembershipStatus.ACTIVE)) {
			throw new IllegalOperationException("User has already applied to or is a member of this agency");
		}

		AgencyMembership membership = new AgencyMembership();
		membership.setUser(user);
		membership.setAgency(agency);
		membership.setStatus(AgencyMembership.MembershipStatus.PENDING);

		AgencyMembership savedMembership = membershipRepository.save(membership);
		return mapToMembershipDTO(savedMembership);
	}

	public AgencyMembershipDTO approveMembership(Long membershipId, Long agencyAdminId) {
		AgencyMembership membership = membershipRepository.findById(membershipId)
				.orElseThrow(() -> new MembershipNotFoundException(membershipId));

		// Verify the approving user is admin of this agency
		if (!membership.getAgency().getAdmin().getId().equals(agencyAdminId)) {
			throw new IllegalOperationException("Only agency admin can approve memberships");
		}

		membership.setStatus(AgencyMembership.MembershipStatus.ACTIVE);
		membership.setJoinDate(LocalDate.now());

		return mapToMembershipDTO(membershipRepository.save(membership));
	}

	public AgencyMembershipDTO rejectMembership(Long membershipId, Long agencyAdminId) {
		AgencyMembership membership = membershipRepository.findById(membershipId)
				.orElseThrow(() -> new MembershipNotFoundException(membershipId));

		if (!membership.getAgency().getAdmin().getId().equals(agencyAdminId)) {
			throw new IllegalOperationException("Only agency admin can reject memberships");
		}

		membership.setStatus(AgencyMembership.MembershipStatus.REJECTED);
		return mapToMembershipDTO(membershipRepository.save(membership));
	}

	public List<AgencyDTO> getAllAgencies() {
		return agencyRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
	}

	public AgencyDTO getAgencyById(Long agencyId) {
		Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new AgencyNotFoundException(agencyId));
		return mapToDTO(agency);
	}

	public List<AgencyMembershipDTO> getPendingMemberships(Long agencyId, Long adminUserId) {
		Agency agency = agencyRepository.findById(agencyId).orElseThrow(() -> new AgencyNotFoundException(agencyId));

		if (!agency.getAdmin().getId().equals(adminUserId)) {
			throw new IllegalOperationException("Only agency admin can view pending memberships");
		}

		return membershipRepository.findPendingMembershipsByAgencyId(agencyId).stream().map(this::mapToMembershipDTO)
				.collect(Collectors.toList());
	}

	// Mapping methods
	private AgencyDTO mapToDTO(Agency agency) {
		AgencyDTO dto = new AgencyDTO();
		dto.setId(agency.getId());
		dto.setName(agency.getName());
		dto.setDescription(agency.getDescription());
		dto.setLogo(agency.getLogo());
		dto.setContactInfo(agency.getContactInfo());
		dto.setCreatedAt(agency.getCreatedAt());

		// Map admin (simplified)
		UserDTO adminDto = new UserDTO();
		adminDto.setId(agency.getAdmin().getId());
		adminDto.setEmail(agency.getAdmin().getEmail());
		dto.setAdmin(adminDto);

		return dto;
	}

	private AgencyMembershipDTO mapToMembershipDTO(AgencyMembership membership) {
		AgencyMembershipDTO dto = new AgencyMembershipDTO();
		dto.setId(membership.getId());
		dto.setStatus(membership.getStatus());
		dto.setPosition(membership.getPosition());
		dto.setJoinDate(membership.getJoinDate());
		dto.setCreatedAt(membership.getCreatedAt());

		// Map user (simplified)
		UserDTO userDto = new UserDTO();
		userDto.setId(membership.getUser().getId());
		userDto.setEmail(membership.getUser().getEmail());
		dto.setUser(userDto);

		// Map agency (simplified)
		AgencyDTO agencyDto = new AgencyDTO();
		agencyDto.setId(membership.getAgency().getId());
		agencyDto.setName(membership.getAgency().getName());
		dto.setAgency(agencyDto);

		return dto;
	}

	public void cancelMembership(Long membershipId) {
		AgencyMembership membership = membershipRepository.findById(membershipId)
				.orElseThrow(() -> new MembershipNotFoundException(membershipId));

		if (membership.getStatus() != AgencyMembership.MembershipStatus.PENDING) {
			throw new IllegalOperationException("Only pending applications can be cancelled");
		}

		membershipRepository.delete(membership);
	}
	
	public List<AgencyMembershipDTO> getUserMemberships(Long userId) {
        List<AgencyMembership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(this::mapToMembershipDTO)
                .collect(Collectors.toList());
    }
}