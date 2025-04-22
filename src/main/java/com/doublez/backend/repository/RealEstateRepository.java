package com.doublez.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate>{
	
	long count();
	
	boolean existsByOwner(User owner);
	
	@Modifying
	@Query("UPDATE RealEstate r SET r.owner.id = :newOwnerId WHERE r.owner.id = :oldOwnerId")
	void reassignAllPropertiesFromUser(@Param("oldOwnerId") Long oldOwnerId, 
	                                 @Param("newOwnerId") Long newOwnerId);

}
