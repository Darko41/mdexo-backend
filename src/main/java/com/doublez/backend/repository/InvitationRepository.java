package com.doublez.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.InvitationStatus;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    // Find by token
    Optional<Invitation> findByToken(String token);
    
    // Find by email and agency
    Optional<Invitation> findByEmailAndAgency(String email, Agency agency);
    
    // Find by user and agency
    Optional<Invitation> findByInvitedUserAndAgency(User invitedUser, Agency agency);
    
    // Find all invitations for an agency
    List<Invitation> findByAgency(Agency agency);
    
    // Find invitations by status for an agency
    List<Invitation> findByAgencyAndStatus(Agency agency, InvitationStatus status);
    
    // Find pending invitations for an agency
    List<Invitation> findByAgencyAndStatusOrderBySentAtDesc(Agency agency, InvitationStatus status);
    
    // Find all invitations sent by a user
    List<Invitation> findByInvitedBy(User invitedBy);
    
    // Find invitations sent to a specific email
    List<Invitation> findByEmail(String email);
    
    // Find pending invitations sent to a specific email
    List<Invitation> findByEmailAndStatus(String email, InvitationStatus status);
    
    // Count pending invitations for an agency
    long countByAgencyAndStatus(Agency agency, InvitationStatus status);
    
    // Find expired pending invitations
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    List<Invitation> findExpiredPendingInvitations(@Param("now") LocalDateTime now);
    
    // Check if pending invitation exists for email and agency
    boolean existsByEmailAndAgencyAndStatus(String email, Agency agency, InvitationStatus status);
    
    // Find invitations that need to be resent (pending and older than X days)
    @Query("SELECT i FROM Invitation i WHERE i.status = 'PENDING' AND i.sentAt < :cutoffDate")
    List<Invitation> findInvitationsToResend(@Param("cutoffDate") LocalDateTime cutoffDate);
}
