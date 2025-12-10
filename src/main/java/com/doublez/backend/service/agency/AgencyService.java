package com.doublez.backend.service.agency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.agency.AgencyCreateDTO;
import com.doublez.backend.dto.agency.AgencyResponseDTO;
import com.doublez.backend.dto.agency.AgencyUpdateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.enums.VerificationStatus;
import com.doublez.backend.exception.AgencyNotFoundException;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RoleRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AgencyService {
	
	private static final Logger logger = LoggerFactory.getLogger(AgencyService.class);

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AgencyService(
            AgencyRepository agencyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    
    // Use AgencyResponseDTO
    public List<AgencyResponseDTO> getAllAgencies() {
        return agencyRepository.findAll()
                .stream()
                .map(Agency::toResponseDTO) // Use entity method directly
                .collect(Collectors.toList());
    }

    // Use AgencyResponseDTO
    public AgencyResponseDTO getAgencyById(Long id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new AgencyNotFoundException(id));
        return agency.toResponseDTO(); // Use entity method directly
    }

    // Use AgencyCreateDTO and AgencyResponseDTO
    public AgencyResponseDTO createAgency(AgencyCreateDTO createDto, Long adminUserId) {
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

        // Use AgencyCreateDTO
        Agency agency = Agency.fromCreateDto(createDto, admin);

        Agency saved = agencyRepository.save(agency);
        
        // Update user role to AGENCY if not already
        if (!admin.isAgencyAdmin()) {
            upgradeUserToAgencyAdmin(admin);
        }

        return saved.toResponseDTO();
    }

    // Use AgencyUpdateDTO and AgencyResponseDTO
    public AgencyResponseDTO updateAgency(Long agencyId, AgencyUpdateDTO updateDto) {
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

        // Address updates
        if (updateDto.getAddress() != null) {
            agency.setAddress(updateDto.getAddress());
            wasUpdated = true;
        }
        if (updateDto.getCity() != null) {
            agency.setCity(updateDto.getCity());
            wasUpdated = true;
        }
        if (updateDto.getState() != null) {
            agency.setState(updateDto.getState());
            wasUpdated = true;
        }
        if (updateDto.getZipCode() != null) {
            agency.setZipCode(updateDto.getZipCode());
            wasUpdated = true;
        }
        if (updateDto.getCountry() != null) {
            agency.setCountry(updateDto.getCountry());
            wasUpdated = true;
        }

        if (wasUpdated) {
            agency = agencyRepository.save(agency);
        }

        return agency.toResponseDTO();
    }

    // Admin-only method for license number updates (with verification)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AgencyResponseDTO updateAgencyLicenseNumber(Long agencyId, String newLicenseNumber) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        // Validate new license number
        if (newLicenseNumber == null || newLicenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("License number cannot be empty");
        }

        // Check if license number already exists
        if (agencyRepository.existsByLicenseNumber(newLicenseNumber)) {
            throw new IllegalOperationException("License number already exists: " + newLicenseNumber);
        }

        // Log the change for audit purposes
        logger.info("Admin updating license number for agency {}: {} -> {}", 
            agency.getName(), agency.getLicenseNumber(), newLicenseNumber);

        agency.setLicenseNumber(newLicenseNumber);
        agencyRepository.save(agency);

        return agency.toResponseDTO();
    }
    
    public Optional<AgencyResponseDTO> getAgencyByAdminId(Long adminUserId) {
        return agencyRepository.findActiveAgencyByAdminId(adminUserId)
                .map(Agency::toResponseDTO);
    }

    // DEACTIVATE AGENCY 
    public void deactivateAgency(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        agency.setIsActive(false);
        agencyRepository.save(agency);
    }

    // ACTIVATE AGENCY
    public void activateAgency(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        agency.setIsActive(true);
        agencyRepository.save(agency);
    }

    // HELPER METHODS
    private void upgradeUserToAgencyAdmin(User user) {
        Role agencyAdminRole = roleRepository.findByName("ROLE_AGENCY")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_AGENCY");
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

    // Check if user can create agency
    public boolean canUserCreateAgency(User user) {
        return user.isAgencyAdmin() && user.getOwnedAgency().isEmpty();
    }

    // Get agency by ID with full details
    public AgencyResponseDTO getAgencyDetails(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        return agency.toResponseDTO();
    }

    // Update agency tier
    public AgencyResponseDTO updateAgencyTier(Long agencyId, UserTier newTier) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));
        
        agency.setTier(newTier);
        agencyRepository.save(agency);
        
        return agency.toResponseDTO();
    }
    
 // 🆕 VERIFICATION MANAGEMENT METHODS

    @PreAuthorize("hasRole('ROLE_AGENCY')")
    public AgencyResponseDTO submitVerification(Long agencyId, String documents, Long userId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        // Verify ownership
        if (!agency.getAdmin().getId().equals(userId)) {
            throw new IllegalOperationException("You can only submit verification for your own agency");
        }

        agency.submitVerification(documents);
        agencyRepository.save(agency);

        logger.info("Verification submitted for agency {} by user {}", agency.getName(), userId);
        return agency.toResponseDTO();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AgencyResponseDTO approveVerification(Long agencyId, Long adminUserId, String notes) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        agency.approveVerification(adminUserId, notes);
        agencyRepository.save(agency);

        // Send notification to agency admin
        sendVerificationApprovalNotification(agency);

        logger.info("Agency {} verification approved by admin {}", agency.getName(), adminUserId);
        return agency.toResponseDTO();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AgencyResponseDTO rejectVerification(Long agencyId, Long adminUserId, String notes) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        agency.rejectVerification(adminUserId, notes);
        agencyRepository.save(agency);

        // Send notification to agency admin
        sendVerificationRejectionNotification(agency, notes);

        logger.info("Agency {} verification rejected by admin {}", agency.getName(), adminUserId);
        return agency.toResponseDTO();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AgencyResponseDTO suspendVerification(Long agencyId, Long adminUserId, String reason) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        agency.setVerificationStatus(VerificationStatus.SUSPENDED);
        agency.setVerifiedAt(LocalDateTime.now());
        agency.setVerifiedBy(adminUserId);
        agency.setVerificationNotes(reason);
        agencyRepository.save(agency);

        logger.info("Agency {} verification suspended by admin {}", agency.getName(), adminUserId);
        return agency.toResponseDTO();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AgencyResponseDTO requestAdditionalDocuments(Long agencyId, Long adminUserId, String requestDetails) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new AgencyNotFoundException(agencyId));

        agency.setVerificationStatus(VerificationStatus.REQUESTED);
        agency.setVerificationNotes("Additional documents requested: " + requestDetails);
        agencyRepository.save(agency);

        // Send notification to agency admin
        sendDocumentRequestNotification(agency, requestDetails);

        logger.info("Additional documents requested for agency {} by admin {}", agency.getName(), adminUserId);
        return agency.toResponseDTO();
    }

    // 🆕 VERIFICATION QUERIES

    public List<AgencyResponseDTO> getAgenciesPendingVerification() {
        return agencyRepository.findByVerificationStatus(VerificationStatus.SUBMITTED)
                .stream()
                .map(Agency::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgencyResponseDTO> getVerifiedAgencies() {
        return agencyRepository.findByVerificationStatus(VerificationStatus.VERIFIED)
                .stream()
                .map(Agency::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgencyResponseDTO> getRejectedAgencies() {
        return agencyRepository.findByVerificationStatus(VerificationStatus.REJECTED)
                .stream()
                .map(Agency::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 🆕 VERIFICATION STATISTICS

    public Map<String, Object> getVerificationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAgencies", agencyRepository.count());
        stats.put("verifiedAgencies", agencyRepository.countByVerificationStatus(VerificationStatus.VERIFIED));
        stats.put("pendingVerification", agencyRepository.countByVerificationStatus(VerificationStatus.SUBMITTED));
        stats.put("rejectedAgencies", agencyRepository.countByVerificationStatus(VerificationStatus.REJECTED));
        stats.put("suspendedAgencies", agencyRepository.countByVerificationStatus(VerificationStatus.SUSPENDED));
        
        return stats;
    }

    // 🆕 NOTIFICATION METHODS (to be implemented with your notification system)

    private void sendVerificationApprovalNotification(Agency agency) {
        // Implement with your notification service (email, in-app, etc.)
        logger.info("Sending verification approval notification to agency: {}", agency.getName());
    }

    private void sendVerificationRejectionNotification(Agency agency, String reason) {
        // Implement with your notification service
        logger.info("Sending verification rejection notification to agency: {} with reason: {}", 
                   agency.getName(), reason);
    }

    private void sendDocumentRequestNotification(Agency agency, String requestDetails) {
        // Implement with your notification service
        logger.info("Sending document request notification to agency: {} with details: {}", 
                   agency.getName(), requestDetails);
    }

}