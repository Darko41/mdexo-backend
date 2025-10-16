package com.doublez.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	
	@Query("SELECT COUNT(re) > 0 FROM RealEstate re JOIN re.assignedAgents a WHERE re.propertyId = :propertyId AND a.id = :agentId")
	boolean existsByIdAndAssignedAgentsId (@Param("propertyId") Long propertyId, @Param("agentId") Long agentId);
	
	boolean existsByPropertyIdAndOwnerId(Long propertyId, Long ownerId);
	
	@Query("SELECT re FROM RealEstate re WHERE " +
		       "LOWER(re.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
		       "LOWER(re.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
		       "LOWER(re.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
		       "LOWER(re.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
	Page<RealEstate> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
	
	

}
