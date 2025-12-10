package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.agency.AgentRole;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    // Find agent by user and agency
    Optional<Agent> findByUserAndAgency(User user, Agency agency);
    
    // Find agent by user ID and agency ID
    @Query("SELECT a FROM Agent a WHERE a.user.id = :userId AND a.agency.id = :agencyId")
    Optional<Agent> findByUserIdAndAgencyId(@Param("userId") Long userId, @Param("agencyId") Long agencyId);
    
    // Find all agents for an agency
    List<Agent> findByAgency(Agency agency);
    
    // Find all agents for an agency with specific role
    List<Agent> findByAgencyAndRole(Agency agency, AgentRole role);
    
    // Find all agents for an agency that are active
    List<Agent> findByAgencyAndIsActive(Agency agency, Boolean isActive);
    
    // Find all agents for a user
    List<Agent> findByUser(User user);
    
    // Find all active agents for a user
    List<Agent> findByUserAndIsActive(User user, Boolean isActive);
    
    // Count agents in agency
    long countByAgency(Agency agency);
    
    // Count active agents in agency
    long countByAgencyAndIsActive(Agency agency, Boolean isActive);
    
    // Count agents by role in agency
    long countByAgencyAndRole(Agency agency, AgentRole role);
    
    // Find agent by user email and agency
    @Query("SELECT a FROM Agent a WHERE a.user.email = :email AND a.agency.id = :agencyId")
    Optional<Agent> findByUserEmailAndAgencyId(@Param("email") String email, @Param("agencyId") Long agencyId);
    
    // Check if user is agent in any agency
    boolean existsByUser(User user);
    
    // Check if user is active agent in any agency
    boolean existsByUserAndIsActive(User user, Boolean isActive);
    
    // Find agents with listing statistics
    @Query("SELECT a FROM Agent a WHERE a.agency = :agency AND a.activeListingsCount > 0")
    List<Agent> findAgentsWithListings(@Param("agency") Agency agency);
    
    // Find top performing agents by deals closed
    @Query("SELECT a FROM Agent a WHERE a.agency = :agency ORDER BY a.dealsClosed DESC")
    List<Agent> findTopPerformers(@Param("agency") Agency agency);
}
