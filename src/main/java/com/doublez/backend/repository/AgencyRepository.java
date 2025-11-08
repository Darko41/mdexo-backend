package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.User;
import com.doublez.backend.entity.agency.Agency;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
    Optional<Agency> findByName(String name);
    List<Agency> findByAdmin(User admin);
    boolean existsByName(String name);
    
    @Query("SELECT a FROM Agency a WHERE a.admin.id = :adminId")
    Optional<Agency> findByAdminId(Long adminId);
    
    @Query("SELECT a FROM Agency a JOIN a.memberships m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Agency> findActiveAgenciesByUserId(Long userId);
}