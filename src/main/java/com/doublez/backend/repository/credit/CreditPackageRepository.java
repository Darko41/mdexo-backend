package com.doublez.backend.repository.credit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.credit.CreditPackage;
import com.doublez.backend.enums.CreditPackageType;

@Repository
public interface CreditPackageRepository extends JpaRepository<CreditPackage, Long> {
    List<CreditPackage> findByIsActiveTrue();
    
    List<CreditPackage> findByType(CreditPackageType type);
    
    List<CreditPackage> findByIsActiveTrueAndType(CreditPackageType type);
    
    Optional<CreditPackage> findByName(String name);
    
    @Query("SELECT cp FROM CreditPackage cp WHERE cp.isActive = true AND cp.price <= :maxPrice")
    List<CreditPackage> findActivePackagesUnderPrice(@Param("maxPrice") BigDecimal maxPrice);
    
    List<CreditPackage> findByIsAgencyPackageTrueAndIsActiveTrue();

	 // Find packages by type for agencies
	 List<CreditPackage> findByTypeAndIsAgencyPackageTrueAndIsActiveTrue(CreditPackageType type);
	
	 // Find packages suitable for team size
	 @Query("SELECT cp FROM CreditPackage cp WHERE " +
	        "cp.isAgencyPackage = true AND cp.isActive = true AND " +
	        "(cp.maxAgents IS NULL OR cp.maxAgents >= :agentCount) AND " +
	        "(cp.maxSuperAgents IS NULL OR cp.maxSuperAgents >= :superAgentCount)")
	 List<CreditPackage> findAgencyPackagesForTeamSize(
	         @Param("agentCount") int agentCount, 
	         @Param("superAgentCount") int superAgentCount);
	
	 // Find packages with bulk discounts
	 List<CreditPackage> findByBulkPurchaseDiscountNotNullAndIsActiveTrueOrderByBulkPurchaseDiscountDesc();
}
