package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
	
//	Optional<User> findById(Long id);
//	
//	long count();
	
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
}
