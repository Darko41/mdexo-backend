package com.doublez.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.enums.VerificationStatus;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
    
	@Query("SELECT a FROM Agency a WHERE a.admin.id = :adminId")
    List<Agency> findByAdminId(@Param("adminId") Long adminId);
    
    boolean existsByName(String name);
    
    boolean existsByLicenseNumber(String licenseNumber);
    
    List<Agency> findByIsActiveTrue();
    
    List<Agency> findByIsActiveFalse();
    
    // Find agencies by admin user (active only)
    @Query("SELECT a FROM Agency a WHERE a.admin.id = :adminId AND a.isActive = true")
    Optional<Agency> findActiveAgencyByAdminId(@Param("adminId") Long adminId);
    
    // 🆕 VERIFICATION QUERIES
    List<Agency> findByVerificationStatus(VerificationStatus status);
    
    @Query("SELECT COUNT(a) FROM Agency a WHERE a.verificationStatus = :status")
    long countByVerificationStatus(@Param("status") VerificationStatus status);
    
    @Query("SELECT a FROM Agency a WHERE a.verificationStatus = 'SUBMITTED' OR a.verificationStatus = 'UNDER_REVIEW'")
    List<Agency> findAgenciesNeedingVerification();
    
    @Query("SELECT a FROM Agency a WHERE a.verificationStatus = 'VERIFIED' AND a.isActive = true")
    List<Agency> findActiveVerifiedAgencies();
    
    @Query("SELECT a FROM Agency a WHERE a.verificationSubmittedAt BETWEEN :startDate AND :endDate")
    List<Agency> findAgenciesSubmittedInPeriod(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // 🆕 FOR TRIAL SUNSET MECHANISM
    @Query("SELECT COUNT(a) FROM Agency a WHERE a.createdAt > :cutoffDate")
    long countByCreatedAtAfter(@Param("cutoffDate") LocalDateTime cutoffDate);
}