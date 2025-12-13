package com.doublez.backend.repository.credit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.credit.AgencyCredit;

public interface AgencyCreditRepository extends JpaRepository<AgencyCredit, Long> {
    
    Optional<AgencyCredit> findByAgency(Agency agency);
    Optional<AgencyCredit> findByAgencyId(Long agencyId);
    
    @Query("SELECT SUM(ac.currentBalance) FROM AgencyCredit ac")
    Long getTotalAgencyCredits();
}
