package com.doublez.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmail(String email);
	
	long count();
	
	@Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
	long countUsersByRole(@Param("roleName") String rolename);
}
