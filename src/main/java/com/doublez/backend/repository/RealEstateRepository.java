package com.doublez.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.User;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate>{
	
	long count();
	
	boolean existsByOwner(User owner);

}
