package com.doublez.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.invitation.InvitationCreateDTO;
import com.doublez.backend.dto.invitation.InvitationResponseDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.InvitationStatus;

@Component
public class InvitationMapper {
    
    // ===== INVITATION MAPPING =====
    
    public InvitationResponseDTO toResponseDTO(Invitation invitation) {
        if (invitation == null) return null;
        
        InvitationResponseDTO dto = new InvitationResponseDTO();
        dto.setId(invitation.getId());
        dto.setToken(invitation.getToken());
        dto.setEmail(invitation.getEmail());
        
        // Map invited user info
        if (invitation.getInvitedUser() != null) {
            dto.setInvitedUserId(invitation.getInvitedUser().getId());
            dto.setInvitedUserName(getUserFullName(invitation.getInvitedUser()));
        }
        
        // Map agency info
        if (invitation.getAgency() != null) {
            dto.setAgencyId(invitation.getAgency().getId());
            dto.setAgencyName(invitation.getAgency().getName());
        }
        
        // Map role
        dto.setRole(invitation.getRole());
        dto.setRoleDisplayName(invitation.getRole() != null ? invitation.getRole().getDisplayName() : null);
        
        // Map inviter info
        if (invitation.getInvitedBy() != null) {
            dto.setInvitedById(invitation.getInvitedBy().getId());
            dto.setInvitedByName(getUserFullName(invitation.getInvitedBy()));
        }
        
        // Map status and timestamps
        dto.setStatus(invitation.getStatus());
        dto.setStatusDisplayName(invitation.getStatus() != null ? invitation.getStatus().getDisplayName() : null);
        dto.setSentAt(invitation.getSentAt());
        dto.setExpiresAt(invitation.getExpiresAt());
        dto.setAcceptedAt(invitation.getAcceptedAt());
        dto.setRejectedAt(invitation.getRejectedAt());
        dto.setCancelledAt(invitation.getCancelledAt());
        dto.setMessage(invitation.getMessage());
        dto.setCustomPermissions(invitation.getCustomPermissions());
        
        // Calculate derived properties
        dto.setExpired(invitation.isExpired());
        dto.setDaysUntilExpiry(invitation.getDaysUntilExpiry());
        
        // Business rules for actions
        dto.setCanResend(invitation.getStatus() == InvitationStatus.PENDING || 
                         invitation.getStatus() == InvitationStatus.EXPIRED);
        dto.setCanCancel(invitation.getStatus() == InvitationStatus.PENDING);
        
        return dto;
    }
    
    public List<InvitationResponseDTO> toResponseDTOList(List<Invitation> invitations) {
        if (invitations == null) return null;
        return invitations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Invitation toEntity(InvitationCreateDTO createDto, Agency agency, User invitedBy, User invitedUser) {
        if (createDto == null || agency == null || invitedBy == null) return null;
        
        Invitation invitation = new Invitation();
        invitation.setEmail(createDto.getEmail());
        invitation.setAgency(agency);
        invitation.setRole(createDto.getRole());
        invitation.setInvitedBy(invitedBy);
        invitation.setInvitedUser(invitedUser);
        invitation.setMessage(createDto.getMessage());
        invitation.setCustomPermissions(createDto.getCustomPermissions());
        invitation.setSentAt(java.time.LocalDateTime.now());
        
        // Set expiry date
        int expiryDays = createDto.getExpiryDays() != null ? createDto.getExpiryDays() : 7;
        invitation.setExpiresAt(java.time.LocalDateTime.now().plusDays(expiryDays));
        
        // Default status
        invitation.setStatus(InvitationStatus.PENDING);
        
        return invitation;
    }
    
    public Invitation toEntity(InvitationCreateDTO createDto, Agency agency, User invitedBy) {
        return toEntity(createDto, agency, invitedBy, null);
    }
    
    // Helper method to get user's full name
    private String getUserFullName(User user) {
        if (user == null) return null;
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        }
        return user.getEmail();
    }
}