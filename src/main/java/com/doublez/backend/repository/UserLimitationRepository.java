package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;

@Repository
public interface UserLimitationRepository extends JpaRepository<UserLimitation, Long>{
	
	Optional<UserLimitation> findByTier(UserTier tier);
    
    List<UserLimitation> findAllByOrderByPricePerMonthAsc();

}
