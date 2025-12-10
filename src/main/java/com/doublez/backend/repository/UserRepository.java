package com.doublez.backend.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
	
	Optional<User> findFirstByRoles_Name(String roleName);
	
	@Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
	long countUsersByRole(@Param("roleName") String rolename);
	
	boolean existsByEmail(String email);
		
	@Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRole(@Param("roleName") String roleName);
    
    // Additional useful methods you might want:
    boolean existsById(Long id);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    long countAdmins();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    long countByRoles_Name(String roleName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    List<User> findByTier(UserTier tier);
    
    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.owner.id = :userId")
    Long countActiveRealEstatesByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(i) FROM RealEstate re JOIN re.images i WHERE re.owner.id = :userId")
    Long countImagesByUser(@Param("userId") Long userId);
    
    // 🆕 TRIAL-RELATED QUERIES
    @Query("SELECT u FROM User u WHERE u.trialEndDate = :date AND u.trialUsed = true")
    List<User> findByTrialEndDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.trialEndDate < :date AND u.trialUsed = true")
    List<User> findByTrialEndDateBefore(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.trialUsed = true AND u.trialEndDate BETWEEN :startDate AND :endDate")
    List<User> findUsersWithTrialEndingBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.trialUsed = true AND u.trialEndDate > CURRENT_TIMESTAMP")
    Long countActiveTrials();
    
    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") String roleName);

}
