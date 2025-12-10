package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.invitation.AcceptInvitationDTO;
import com.doublez.backend.dto.invitation.InvitationCreateDTO;
import com.doublez.backend.dto.invitation.InvitationResponseDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.agency.InvitationService;
import com.doublez.backend.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {
    
    @Autowired
    private InvitationService invitationService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    /**
     * Send team invitation
     */
    @PostMapping
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<InvitationResponseDTO> sendInvitation(@Valid @RequestBody InvitationCreateDTO invitationCreateDTO) {
        User currentUser = getCurrentUser();
        InvitationResponseDTO invitation = invitationService.sendInvitation(invitationCreateDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }
    
    /**
     * Accept or reject invitation
     */
    @PostMapping("/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvitationResponseDTO> respondToInvitation(@Valid @RequestBody AcceptInvitationDTO acceptInvitationDTO) {
        User currentUser = getCurrentUser();
        InvitationResponseDTO response = invitationService.respondToInvitation(acceptInvitationDTO, currentUser);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel invitation
     */
    @DeleteMapping("/{invitationId}")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<Void> cancelInvitation(@PathVariable Long invitationId) {
        User currentUser = getCurrentUser();
        invitationService.cancelInvitation(invitationId, currentUser);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Resend invitation
     */
    @PostMapping("/{invitationId}/resend")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<InvitationResponseDTO> resendInvitation(@PathVariable Long invitationId) {
        User currentUser = getCurrentUser();
        InvitationResponseDTO resentInvitation = invitationService.resendInvitation(invitationId, currentUser);
        return ResponseEntity.ok(resentInvitation);
    }
    
    /**
     * Get invitation by token
     */
    @GetMapping("/{token}")
    public ResponseEntity<InvitationResponseDTO> getInvitation(@PathVariable String token) {
        InvitationResponseDTO invitation = invitationService.getInvitationByToken(token);
        return ResponseEntity.ok(invitation);
    }
    
    /**
     * Get pending invitations for current user's agency
     */
    @GetMapping("/agency/{agencyId}/pending")
    @PreAuthorize("hasRole('AGENCY')")
    public ResponseEntity<List<InvitationResponseDTO>> getPendingInvitations(@PathVariable Long agencyId) {
        // This would need to be implemented in the service
        throw new UnsupportedOperationException("Implement getPendingInvitations endpoint");
    }
    
    private User getCurrentUser() {
        // This should be implemented to get the authenticated user
        // For now, returning a placeholder
        throw new UnsupportedOperationException("Implement getCurrentUser() based on your authentication");
    }
}
