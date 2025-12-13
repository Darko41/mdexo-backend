package com.doublez.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.credit.AgencyCreditPurchaseDTO;
import com.doublez.backend.dto.credit.AgencyPurchaseQuoteDTO;
import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.dto.credit.TeamCreditPackageResponseDTO;
import com.doublez.backend.dto.credit.TeamCreditSummaryDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.credit.TeamCreditService;
import com.doublez.backend.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
public class CreditPackageController {
	
	@Autowired
	private TeamCreditService teamCreditService;
	
	@Autowired
	private SecurityUtils securityUtils;

	/**
	 * Get credit packages for agencies
	 */
	@GetMapping("/agency/{agencyId}/packages")
	@PreAuthorize("hasRole('AGENCY')")
	public ResponseEntity<List<TeamCreditPackageResponseDTO>> getAgencyCreditPackages(
	        @PathVariable Long agencyId) {
	    
	    User currentUser = securityUtils.getCurrentUser();
	    verifyAgencyAccess(currentUser, agencyId);
	    
	    List<TeamCreditPackageResponseDTO> packages = teamCreditService.getAgencyCreditPackages(agencyId);
	    return ResponseEntity.ok(packages);
	}

	/**
	 * Calculate purchase quote for agency
	 */
	@PostMapping("/agency/calculate-quote")
	@PreAuthorize("hasRole('AGENCY')")
	public ResponseEntity<AgencyPurchaseQuoteDTO> calculateAgencyPurchaseQuote(
	        @Valid @RequestBody AgencyCreditPurchaseDTO purchaseDTO) {
	    
	    User currentUser = securityUtils.getCurrentUser();
	    verifyAgencyAccess(currentUser, purchaseDTO.getAgencyId());
	    
	    AgencyPurchaseQuoteDTO quote = teamCreditService.calculateAgencyPurchaseQuote(purchaseDTO);
	    return ResponseEntity.ok(quote);
	}

	/**
	 * Purchase credits for agency team
	 */
	@PostMapping("/agency/purchase")
	@PreAuthorize("hasRole('AGENCY')")
	public ResponseEntity<CreditTransactionResponseDTO> purchaseAgencyCredits(
	        @Valid @RequestBody AgencyCreditPurchaseDTO purchaseDTO) {
	    
	    User currentUser = securityUtils.getCurrentUser();
	    
	    CreditTransactionResponseDTO transaction = teamCreditService.purchaseAgencyCredits(
	        purchaseDTO, currentUser);
	    
	    return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
	}

	/**
	 * Get team credit summary
	 */
	@GetMapping("/agency/{agencyId}/summary")
	@PreAuthorize("hasRole('AGENCY')")
	public ResponseEntity<TeamCreditSummaryDTO> getTeamCreditSummary(@PathVariable Long agencyId) {
	    
	    User currentUser = securityUtils.getCurrentUser();
	    verifyAgencyAccess(currentUser, agencyId);
	    
	    TeamCreditSummaryDTO summary = teamCreditService.getTeamCreditSummary(agencyId);
	    return ResponseEntity.ok(summary);
	}

	private void verifyAgencyAccess(User user, Long agencyId) {
	    // Implement based on your permission system
	    // Check if user is agent in this agency with appropriate permissions
	}

}
