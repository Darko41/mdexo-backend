package com.doublez.backend.service.agency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.AgencyNotFoundException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AgencyService(
            AgencyRepository agencyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) { // Update constructor
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
 // Get all agencies
    public List<AgencyDTO> getAllAgencies() {
        return agencyRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Get agency by ID
    public AgencyDTO getAgencyById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new AgencyNotFoundException(id));
        return toDTO(agency);
    }

    // ============================================
    // CREATE AGENCY (Updated for new fields)
    // ============================================
    public AgencyDTO createAgency(AgencyDTO.Create createDto, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new UserNotFoundException(adminUserId));

        // Check if agency name already exists
        if (agencyRepository.existsByName(createDto.getName())) {
            throw new IllegalOperationException("Agency name already exists: " + createDto.getName());
        }

        // Check if license number already exists
        if (agencyRepository.existsByLicenseNumber(createDto.getLicenseNumber())) {
            throw new IllegalOperationException("License number already exists: " + createDto.getLicenseNumber());
        }

        // ✅ FIX: Use the full class name AgencyDTO.Create
        Agency agency = Agency.fromCreateDto(createDto, admin);

        Agency saved = agencyRepository.save(agency);
        
        // Update user role to AGENCY_ADMIN if not already
        if (!admin.isAgencyAdmin()) {
            upgradeUserToAgencyAdmin(admin);
        }

        return toDTO(saved);
    }

    // ============================================
    // UPDATE AGENCY
    // ============================================
    public AgencyDTO updateAgency(Long agencyId, AgencyDTO.Update updateDto) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        boolean wasUpdated = false;

        if (updateDto.getName() != null && !updateDto.getName().equals(agency.getName())) {
            if (agencyRepository.existsByName(updateDto.getName())) {
                throw new IllegalOperationException("Agency name already exists: " + updateDto.getName());
            }
            agency.setName(updateDto.getName());
            wasUpdated = true;
        }

        if (updateDto.getDescription() != null) {
            agency.setDescription(updateDto.getDescription());
            wasUpdated = true;
        }

        if (updateDto.getLogo() != null) {
            agency.setLogo(updateDto.getLogo());
            wasUpdated = true;
        }

        if (updateDto.getContactEmail() != null) {
            agency.setContactEmail(updateDto.getContactEmail());
            wasUpdated = true;
        }

        if (updateDto.getContactPhone() != null) {
            agency.setContactPhone(updateDto.getContactPhone());
            wasUpdated = true;
        }

        if (updateDto.getWebsite() != null) {
            agency.setWebsite(updateDto.getWebsite());
            wasUpdated = true;
        }

        if (updateDto.getLicenseNumber() != null && !updateDto.getLicenseNumber().equals(agency.getLicenseNumber())) {
            if (agencyRepository.existsByLicenseNumber(updateDto.getLicenseNumber())) {
                throw new IllegalOperationException("License number already exists: " + updateDto.getLicenseNumber());
            }
            agency.setLicenseNumber(updateDto.getLicenseNumber());
            wasUpdated = true;
        }

        if (updateDto.getIsActive() != null) {
            agency.setIsActive(updateDto.getIsActive());
            wasUpdated = true;
        }

        if (wasUpdated) {
            agency = agencyRepository.save(agency);
        }

        return toDTO(agency);
    }

    // ============================================
    // GET AGENCY BY ADMIN USER ID
    // ============================================
    public AgencyDTO getAgencyByAdminId(Long adminUserId) {
        List<Agency> agencies = agencyRepository.findByAdminId(adminUserId);
        if (agencies.isEmpty()) {
            throw new AgencyNotFoundException("Agency not found for admin user ID: " + adminUserId);
        }
        // Since a user can only have one agency, return the first one
        return toDTO(agencies.get(0));
    }

    // ============================================
    // DEACTIVATE AGENCY
    // ============================================
    public void deactivateAgency(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        agency.setIsActive(false);
        agencyRepository.save(agency);
    }

    // ============================================
    // ACTIVATE AGENCY
    // ============================================
    public void activateAgency(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        agency.setIsActive(true);
        agencyRepository.save(agency);
    }

    // ============================================
    // HELPER METHODS
    // ============================================
    private void upgradeUserToAgencyAdmin(User user) {
        Role agencyAdminRole = roleRepository.findByName("ROLE_AGENCY_ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_AGENCY_ADMIN");
                    return roleRepository.save(newRole);
                });

        List<Role> currentRoles = new ArrayList<>(user.getRoles());
        currentRoles.add(agencyAdminRole);
        user.setRoles(currentRoles);
        userRepository.save(user);
    }
    
    // Security check method for @PreAuthorize
    public boolean isAgencyAdmin(Long agencyId, Long userId) {
        return agencyRepository.findById(agencyId)
                .map(agency -> agency.getAdmin().getId().equals(userId))
                .orElse(false);
    }

    public AgencyDTO toDTO(Agency agency) {
        AgencyDTO dto = new AgencyDTO();
        dto.setId(agency.getId());
        dto.setName(agency.getName());
        dto.setDescription(agency.getDescription());
        dto.setLogo(agency.getLogo());
        dto.setContactEmail(agency.getContactEmail());
        dto.setContactPhone(agency.getContactPhone());
        dto.setWebsite(agency.getWebsite());
        dto.setLicenseNumber(agency.getLicenseNumber());
        dto.setIsActive(agency.getIsActive());
        dto.setCreatedAt(agency.getCreatedAt());

        User admin = agency.getAdmin();
        if (admin != null) {
            UserDTO adminDto = new UserDTO();
            adminDto.setId(admin.getId());
            adminDto.setEmail(admin.getEmail());
            // Add profile info if needed
            if (admin.getUserProfile() != null) {
                UserProfileDTO profileDto = new UserProfileDTO();
                profileDto.setFirstName(admin.getUserProfile().getFirstName());
                profileDto.setLastName(admin.getUserProfile().getLastName());
                profileDto.setPhone(admin.getUserProfile().getPhone());
                adminDto.setProfile(profileDto);
            }
            dto.setAdmin(adminDto);
        }

        return dto;
    }
}