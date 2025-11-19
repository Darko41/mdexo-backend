package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.agency.Agency;

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
}