package com.doublez.backend.service.agency;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doublez.backend.dto.agent.AgentCreateDTO;
import com.doublez.backend.dto.invitation.AcceptInvitationDTO;
import com.doublez.backend.dto.invitation.InvitationCreateDTO;
import com.doublez.backend.dto.invitation.InvitationResponseDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.InvitationStatus;
import com.doublez.backend.exception.BusinessRuleException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.mapper.InvitationMapper;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.InvitationRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.email.ResendEmailService;

@Service
@Transactional
public class InvitationService {
    
    private static final Logger logger = LoggerFactory.getLogger(InvitationService.class);
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private AgencyRepository agencyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private InvitationMapper invitationMapper;
    
    @Autowired
    private ResendEmailService emailService;
    
    /**
     * Send team invitation
     */
    public InvitationResponseDTO sendInvitation(InvitationCreateDTO invitationCreateDTO, User currentUser) {
        logger.info("Sending invitation to: {} for agency: {}", 
                   invitationCreateDTO.getEmail(), invitationCreateDTO.getAgencyId());
        
        // Find agency
        Agency agency = agencyRepository.findById(invitationCreateDTO.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with id: " + invitationCreateDTO.getAgencyId()));
        
        // Check if invitation already exists for this email and agency
        if (invitationRepository.existsByEmailAndAgencyAndStatus(
                invitationCreateDTO.getEmail(), agency, InvitationStatus.PENDING)) {
            throw new BusinessRuleException("A pending invitation already exists for this email");
        }
        
        // Find invited user if they exist
        User invitedUser = userRepository.findByEmail(invitationCreateDTO.getEmail()).orElse(null);
        
        // Create invitation
        Invitation invitation;
        if (invitedUser != null) {
            invitation = invitationMapper.toEntity(invitationCreateDTO, agency, currentUser, invitedUser);
        } else {
            invitation = invitationMapper.toEntity(invitationCreateDTO, agency, currentUser);
        }
        
        Invitation savedInvitation = invitationRepository.save(invitation);
        
        // Send invitation email
        sendInvitationEmail(savedInvitation);
        
        logger.info("Invitation sent successfully with token: {}", savedInvitation.getToken());
        
        return invitationMapper.toResponseDTO(savedInvitation);
    }
    
    /**
     * Accept or reject invitation
     */
    public InvitationResponseDTO respondToInvitation(AcceptInvitationDTO acceptInvitationDTO, User currentUser) {
        logger.info("Processing invitation response for token: {}", acceptInvitationDTO.getToken());
        
        Invitation invitation = invitationRepository.findByToken(acceptInvitationDTO.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        
        // Verify invitation is valid
        if (!invitation.isValid()) {
            throw new BusinessRuleException("Invitation is no longer valid");
        }
        
        // Verify user matches invitation (if invitation has user specified)
        if (invitation.getInvitedUser() != null && !invitation.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("This invitation is for a different user");
        }
        
        // Verify email matches (for users not yet in system)
        if (invitation.getInvitedUser() == null && !invitation.getEmail().equals(currentUser.getEmail())) {
            throw new BusinessRuleException("This invitation is for a different email address");
        }
        
        if (Boolean.TRUE.equals(acceptInvitationDTO.getAccept())) {
            // Accept invitation
            invitation.accept(currentUser);
            invitationRepository.save(invitation);
            
            // Add user as agent to agency
            AgentCreateDTO agentDTO = createAgentDTOFromInvitation(invitation);
            teamService.addAgent(agentDTO, invitation.getInvitedBy());
            
            logger.info("Invitation accepted by user: {}", currentUser.getEmail());
        } else {
            // Reject invitation
            invitation.reject();
            invitationRepository.save(invitation);
            
            logger.info("Invitation rejected by user: {}", currentUser.getEmail());
        }
        
        return invitationMapper.toResponseDTO(invitation);
    }
    
    /**
     * Cancel invitation
     */
    public InvitationResponseDTO cancelInvitation(Long invitationId, User currentUser) {
        logger.info("Cancelling invitation: {}", invitationId);
        
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + invitationId));
        
        // Verify user has permission to cancel
        if (!invitation.getInvitedBy().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("You can only cancel invitations you sent");
        }
        
        invitation.cancel();
        Invitation cancelledInvitation = invitationRepository.save(invitation);
        
        logger.info("Invitation cancelled: {}", invitationId);
        
        return invitationMapper.toResponseDTO(cancelledInvitation);
    }
    
    /**
     * Resend invitation
     */
    public InvitationResponseDTO resendInvitation(Long invitationId, User currentUser) {
        logger.info("Resending invitation: {}", invitationId);
        
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found with id: " + invitationId));
        
        // Verify user has permission to resend
        if (!invitation.getInvitedBy().getId().equals(currentUser.getId())) {
            throw new BusinessRuleException("You can only resend invitations you sent");
        }
        
        // Verify invitation can be resent
        if (invitation.getStatus() != InvitationStatus.PENDING && 
            invitation.getStatus() != InvitationStatus.EXPIRED) {
            throw new BusinessRuleException("Only pending or expired invitations can be resent");
        }
        
        invitation.resend(currentUser);
        Invitation resentInvitation = invitationRepository.save(invitation);
        
        // Resend email
        sendInvitationEmail(resentInvitation);
        
        logger.info("Invitation resent: {}", invitationId);
        
        return invitationMapper.toResponseDTO(resentInvitation);
    }
    
    /**
     * Get pending invitations for an agency
     */
    @Transactional(readOnly = true)
    public List<Invitation> getPendingInvitationsForAgency(Agency agency) {
        return invitationRepository.findByAgencyAndStatusOrderBySentAtDesc(agency, InvitationStatus.PENDING);
    }
    
    /**
     * Get pending invitation DTOs for an agency
     */
    @Transactional(readOnly = true)
    public List<InvitationResponseDTO> getPendingInvitationDTOsForAgency(Agency agency) {
        List<Invitation> invitations = getPendingInvitationsForAgency(agency);
        return invitationMapper.toResponseDTOList(invitations);
    }
    
    /**
     * Get invitation by token
     */
    @Transactional(readOnly = true)
    public InvitationResponseDTO getInvitationByToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        
        return invitationMapper.toResponseDTO(invitation);
    }
    
    /**
     * Scheduled task to mark expired invitations
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void markExpiredInvitations() {
        logger.info("Running scheduled task to mark expired invitations");
        
        List<Invitation> expiredInvitations = invitationRepository.findExpiredPendingInvitations(LocalDateTime.now());
        
        for (Invitation invitation : expiredInvitations) {
            invitation.markAsExpired();
            invitationRepository.save(invitation);
            logger.debug("Marked invitation as expired: {}", invitation.getId());
        }
        
        logger.info("Marked {} invitations as expired", expiredInvitations.size());
    }
    
    /**
     * Send invitation email
     */
    private void sendInvitationEmail(Invitation invitation) {
        try {
            String subject = "Pozivnica za pridruživanje timu - " + invitation.getAgency().getName();
            
            String htmlContent = createInvitationEmailHtml(invitation);
            String textContent = createInvitationEmailText(invitation);
            
            emailService.sendEmail(invitation.getEmail(), subject, htmlContent, textContent);
            
            logger.info("Invitation email sent to: {}", invitation.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}: {}", invitation.getEmail(), e.getMessage());
            // Don't throw exception - invitation is still created
        }
    }
    
    /**
     * Create HTML email content for invitation
     */
    private String createInvitationEmailHtml(Invitation invitation) {
        String agencyName = invitation.getAgency().getName();
        String inviterName = invitation.getInvitedBy().getFirstName() + " " + invitation.getInvitedBy().getLastName();
        String roleName = invitation.getRole().getDisplayName();
        String invitationLink = "https://yourdomain.com/accept-invitation?token=" + invitation.getToken();
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50; 
                             color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; 
                             font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Pozivnica za tim</h1>
                    </div>
                    <div class="content">
                        <h2>Dobili ste pozivnicu!</h2>
                        <p>Poštovani,</p>
                        <p><strong>%s</strong> vas poziva da se pridružite timu agencije <strong>%s</strong>.</p>
                        <p>Pozicija: <strong>%s</strong></p>
                        %s
                        <p>Pozivnica ističe: <strong>%s</strong></p>
                        <a href="%s" class="button">Prihvati pozivnicu</a>
                        <p>Ili kopirajte ovaj link u pretraživač: %s</p>
                        <p>Ako niste zainteresovani, ignorišite ovaj email.</p>
                    </div>
                    <div class="footer">
                        <p>Ovo je automatski generisan email. Molimo ne odgovarajte na njega.</p>
                        <p>&copy; 2024 Real Estate Platform. Sva prava zadržana.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                inviterName,
                agencyName,
                roleName,
                invitation.getMessage() != null ? 
                    "<p>Poruka: <em>" + invitation.getMessage() + "</em></p>" : "",
                invitation.getExpiresAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")),
                invitationLink,
                invitationLink
            );
    }
    
    /**
     * Create plain text email content for invitation
     */
    private String createInvitationEmailText(Invitation invitation) {
        String agencyName = invitation.getAgency().getName();
        String inviterName = invitation.getInvitedBy().getFirstName() + " " + invitation.getInvitedBy().getLastName();
        String roleName = invitation.getRole().getDisplayName();
        String invitationLink = "https://yourdomain.com/accept-invitation?token=" + invitation.getToken();
        
        return """
            Pozivnica za pridruživanje timu
            
            Dobili ste pozivnicu!
            
            %s vas poziva da se pridružite timu agencije %s.
            
            Pozicija: %s
            
            %s
            Pozivnica ističe: %s
            
            Prihvatite pozivnicu ovde: %s
            
            Ako niste zainteresovani, ignorišite ovaj email.
            
            ---
            Ovo je automatski generisan email. Molimo ne odgovarajte na njega.
            © 2024 Real Estate Platform. Sva prava zadržana.
            """.formatted(
                inviterName,
                agencyName,
                roleName,
                invitation.getMessage() != null ? "Poruka: " + invitation.getMessage() + "\n\n" : "",
                invitation.getExpiresAt().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")),
                invitationLink
            );
    }
    
    /**
     * Create AgentCreateDTO from invitation
     */
    private AgentCreateDTO createAgentDTOFromInvitation(Invitation invitation) {
        AgentCreateDTO agentDTO = new AgentCreateDTO();
        agentDTO.setEmail(invitation.getEmail());
        agentDTO.setAgencyId(invitation.getAgency().getId());
        agentDTO.setRole(invitation.getRole());
        agentDTO.setCustomPermissions(invitation.getCustomPermissions());
        return agentDTO;
    }
    
    /**
     * Get all invitations for a user (sent and received)
     */
    @Transactional(readOnly = true)
    public List<InvitationResponseDTO> getUserInvitations(User user) {
        // Get invitations sent by user
        List<Invitation> sentInvitations = invitationRepository.findByInvitedBy(user);
        
        // Get invitations received by user (by email)
        List<Invitation> receivedInvitations = invitationRepository.findByEmailAndStatus(
            user.getEmail(), InvitationStatus.PENDING);
        
        // Combine and sort by sent date
        List<Invitation> allInvitations = new java.util.ArrayList<>();
        allInvitations.addAll(sentInvitations);
        allInvitations.addAll(receivedInvitations);
        
        allInvitations.sort((i1, i2) -> i2.getSentAt().compareTo(i1.getSentAt()));
        
        return invitationMapper.toResponseDTOList(allInvitations);
    }
}