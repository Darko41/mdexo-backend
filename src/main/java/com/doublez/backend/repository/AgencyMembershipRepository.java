package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.agency.AgencyMembership;

@Repository
public interface AgencyMembershipRepository extends JpaRepository<AgencyMembership, Long> {
    
    @Query("SELECT am FROM AgencyMembership am WHERE am.user.id = :userId AND am.agency.id = :agencyId")
    Optional<AgencyMembership> findByUserIdAndAgencyId(Long userId, Long agencyId);
    
    // Find all memberships for a specific user
    List<AgencyMembership> findByUserId(Long userId);
    
    List<AgencyMembership> findByAgencyId(Long agencyId);
    
    @Query("SELECT am FROM AgencyMembership am WHERE am.agency.id = :agencyId AND am.status = 'PENDING'")
    List<AgencyMembership> findPendingMembershipsByAgencyId(Long agencyId);
    
    @Query("SELECT am FROM AgencyMembership am WHERE am.user.id = :userId AND am.status = 'ACTIVE'")
    List<AgencyMembership> findActiveMembershipsByUserId(Long userId);
    
    boolean existsByUserIdAndAgencyIdAndStatus(Long userId, Long agencyId, AgencyMembership.MembershipStatus status);
    
    @Query("SELECT COUNT(m) > 0 FROM AgencyMembership m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    boolean existsByUserIdAndStatus(@Param("userId") Long userId, AgencyMembership.MembershipStatus status);
    
    // Optional: Get current active membership
    @Query("SELECT m FROM AgencyMembership m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    Optional<AgencyMembership> findActiveMembershipByUserId(@Param("userId") Long userId);
    
}
