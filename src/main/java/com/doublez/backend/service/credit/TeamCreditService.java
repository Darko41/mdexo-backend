package com.doublez.backend.service.credit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.credit.AgencyCreditBalanceDTO;
import com.doublez.backend.dto.credit.AgencyCreditPurchaseDTO;
import com.doublez.backend.dto.credit.AgencyPurchaseQuoteDTO;
import com.doublez.backend.dto.credit.CreditTransactionCreateDTO;
import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.dto.credit.TeamCreditPackageResponseDTO;
import com.doublez.backend.dto.credit.TeamCreditSummaryDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.credit.AgencyCredit;
import com.doublez.backend.entity.credit.CreditPackage;
import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.entity.credit.UserCredit;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.enums.agency.AgentRole;
import com.doublez.backend.enums.subscription.SubscriptionPeriod;
import com.doublez.backend.exception.BusinessRuleException;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.AgentRepository;
import com.doublez.backend.repository.credit.AgencyCreditRepository;
import com.doublez.backend.repository.credit.CreditPackageRepository;
import com.doublez.backend.repository.credit.CreditTransactionRepository;
import com.doublez.backend.repository.credit.UserCreditRepository;

@Service
public class TeamCreditService {

	@Autowired
	private CreditPackageRepository creditPackageRepository;

	@Autowired
	private AgencyRepository agencyRepository;

	@Autowired
	private AgentRepository agentRepository;

	@Autowired
	private CreditService creditService;

	@Autowired
	private CreditTransactionRepository creditTransactionRepository;

	@Autowired
	private AgencyCreditRepository agencyCreditRepository;

	@Autowired
	private UserCreditRepository userCreditRepository;

	/**
	 * Get agency-specific credit packages
	 */
	public List<TeamCreditPackageResponseDTO> getAgencyCreditPackages(Long agencyId) {
		Agency agency = agencyRepository.findById(agencyId)
				.orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

		// Get current team size
		List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
		int agentCount = agents.size();
		long superAgentCount = agents.stream().filter(agent -> agent.getRole() == AgentRole.SUPER_AGENT).count();

		// Get agency packages
		List<CreditPackage> packages = creditPackageRepository.findAgencyPackagesForTeamSize(agentCount,
				(int) superAgentCount);

		// Convert to DTOs
		return packages.stream()
				.map(pkg -> TeamCreditPackageResponseDTO.fromEntity(pkg, agentCount, (int) superAgentCount))
				.collect(Collectors.toList());
	}

	/**
	 * Calculate price for agency package purchase
	 */
	public AgencyPurchaseQuoteDTO calculateAgencyPurchaseQuote(AgencyCreditPurchaseDTO purchaseDTO) {
		Agency agency = agencyRepository.findById(purchaseDTO.getAgencyId())
				.orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

		CreditPackage creditPackage = creditPackageRepository.findById(purchaseDTO.getCreditPackageId())
				.orElseThrow(() -> new ResourceNotFoundException("Credit package not found"));

		// Verify package is for agencies
		if (!Boolean.TRUE.equals(creditPackage.getIsAgencyPackage())) {
			throw new BusinessRuleException("This package is not for agencies");
		}

		// Verify team size limits
		if (!creditPackage.isSuitableForAgency(purchaseDTO.getNumberOfAgents(), purchaseDTO.getNumberOfSuperAgents())) {
			throw new BusinessRuleException("Package not suitable for this team size");
		}

		// Calculate price
		BigDecimal totalPrice = creditPackage.calculateTeamPrice(purchaseDTO.getNumberOfAgents(),
				purchaseDTO.getNumberOfSuperAgents(), purchaseDTO.getPeriod());

		// Calculate total credits
		Integer totalCredits = creditPackage.getTotalCreditsWithTeamBonus();

		// Apply bulk discount if applicable
		BigDecimal finalPrice = totalPrice;
		if (creditPackage.hasBulkDiscount() && purchaseDTO.getNumberOfAgents() > 5) {
			BigDecimal discount = totalPrice.multiply(creditPackage.getBulkPurchaseDiscount())
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			finalPrice = totalPrice.subtract(discount);
		}

		// Create quote
		AgencyPurchaseQuoteDTO quote = new AgencyPurchaseQuoteDTO();
		quote.setAgencyId(purchaseDTO.getAgencyId());
		quote.setCreditPackageId(purchaseDTO.getCreditPackageId());
		quote.setPackageName(creditPackage.getName());
		quote.setNumberOfAgents(purchaseDTO.getNumberOfAgents());
		quote.setNumberOfSuperAgents(purchaseDTO.getNumberOfSuperAgents());
		quote.setPeriod(purchaseDTO.getPeriod());
		quote.setBasePrice(totalPrice);
		quote.setDiscountAmount(totalPrice.subtract(finalPrice));
		quote.setFinalPrice(finalPrice);
		quote.setTotalCredits(totalCredits);
		quote.setPricePerCredit(finalPrice.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP));
		quote.setValidUntil(LocalDateTime.now().plusHours(24));

		return quote;
	}

	/**
	 * Purchase credits for agency team
	 */
	public CreditTransactionResponseDTO purchaseAgencyCredits(AgencyCreditPurchaseDTO purchaseDTO, User currentUser) {
		Agency agency = agencyRepository.findById(purchaseDTO.getAgencyId())
				.orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

		CreditPackage creditPackage = creditPackageRepository.findById(purchaseDTO.getCreditPackageId())
				.orElseThrow(() -> new ResourceNotFoundException("Credit package not found"));

		// Verify permissions
		verifyCanPurchaseForAgency(currentUser, agency);

		// Calculate total credits (base + team bonus)
		Integer totalCredits = creditPackage.getCreditAmount();
		if (creditPackage.getBonusCreditsForTeams() != null) {
			totalCredits += creditPackage.getBonusCreditsForTeams();
		}

		// Calculate price
		BigDecimal totalPrice = creditPackage.calculateTeamPrice(purchaseDTO.getNumberOfAgents(),
				purchaseDTO.getNumberOfSuperAgents(), purchaseDTO.getPeriod());

		// Apply bulk discount if applicable
		if (creditPackage.hasBulkDiscount() && purchaseDTO.getNumberOfAgents() > 5) {
			BigDecimal discount = totalPrice.multiply(creditPackage.getBulkPurchaseDiscount())
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			totalPrice = totalPrice.subtract(discount);
		}

		// Create transaction using your existing CreditService
		// We'll need to extend CreditTransactionCreateDTO to support agency

		CreditTransactionCreateDTO transactionDTO = new CreditTransactionCreateDTO();
		transactionDTO.setUserId(currentUser.getId());
		transactionDTO.setCreditPackageId(creditPackage.getId());
		transactionDTO.setCreditChange(totalCredits);
		transactionDTO.setDescription(
				"Agency purchase: " + creditPackage.getName() + " for " + purchaseDTO.getNumberOfAgents() + " agents");
		transactionDTO.setTransactionType(CreditTransactionType.PURCHASE);

		// Set agency info (you'll need to add agencyId to your CreditTransactionCreateDTO)
		// transactionDTO.setAgencyId(agency.getId());

		CreditTransactionResponseDTO transaction = creditService.createCreditTransaction(transactionDTO);

		// Update transaction with agency price info
		CreditTransaction ct = creditTransactionRepository.findById(transaction.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		// Store price in metadata
		String metadata = String.format(
				"{\"agency_id\":%d,\"total_price\":%s,\"number_of_agents\":%d,\"number_of_super_agents\":%d,\"period\":\"%s\"}",
				agency.getId(), totalPrice.toString(), purchaseDTO.getNumberOfAgents(),
				purchaseDTO.getNumberOfSuperAgents(), purchaseDTO.getPeriod().name());
		ct.setMetadata(metadata);
		creditTransactionRepository.save(ct);

		// Add credits to agency pool
		addCreditsToAgency(agency.getId(), totalCredits, "Purchase: " + creditPackage.getName());

		// Distribute to team if requested
		if (Boolean.TRUE.equals(purchaseDTO.getDistributeToTeam())) {
			distributeAgencyCreditsToTeam(agency.getId(), totalCredits, transaction.getId());
		}

		return transaction;
	}
	
	/**
     * Distribute agency credits to team agents
     */
	private void distributeAgencyCreditsToTeam(Long agencyId, Integer totalCredits, Long sourceTransactionId) {
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
        
        List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
        
        if (agents.isEmpty()) {
            return;
        }
        
        // Get agency credit settings
        AgencyCredit agencyCredit = agencyCreditRepository.findByAgencyId(agencyId).orElse(null);
        int distributionPercentage = agencyCredit != null && agencyCredit.getTeamDistributionEnabled() ? 
                agencyCredit.getDistributionPercentage() : 100;
        
        // Calculate credits to distribute
        int creditsToDistribute = (totalCredits * distributionPercentage) / 100;
        if (creditsToDistribute <= 0) {
            return;
        }
        
        // Simple equal distribution
        int creditsPerAgent = creditsToDistribute / agents.size();
        int remainingCredits = creditsToDistribute % agents.size();
        
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            int agentCredits = creditsPerAgent;
            
            // Distribute remaining credits
            if (i == 0) {
                agentCredits += remainingCredits;
            }
            
            if (agentCredits > 0) {
                // Use your existing CreditService to add to user
                creditService.addCreditsToUser(
                    agent.getUser().getId(),
                    agentCredits,
                    "Team distribution from agency purchase #" + sourceTransactionId
                );
                
                // Deduct from agency pool
                if (agencyCredit != null) {
                    agencyCredit.deductCredits(agentCredits);
                }
                
                // Create distribution transaction
                CreditTransaction distributionTx = new CreditTransaction(
                    agent.getUser(),
                    agentCredits,
                    CreditTransactionType.TRANSFER,
                    "Credits distributed from agency purchase",
                    agency
                );
                distributionTx.setReferenceNumber("AGENCY_DIST_" + sourceTransactionId);
                creditTransactionRepository.save(distributionTx);
            }
        }
        
        if (agencyCredit != null) {
            agencyCreditRepository.save(agencyCredit);
        }
    }
	
	/**
	 * Get agency credit balance
	 */
	public AgencyCreditBalanceDTO getAgencyCreditBalance(Long agencyId) {
	    Agency agency = agencyRepository.findById(agencyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
	    
	    AgencyCredit agencyCredit = agencyCreditRepository.findByAgencyId(agencyId)
	            .orElse(new AgencyCredit(agency));
	    
	    List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);
	    
	    // Calculate total team credits
	    int totalTeamCredits = 0;
	    for (Agent agent : agents) {
	        UserCredit userCredit = userCreditRepository.findByUserId(agent.getUser().getId()).orElse(null);
	        if (userCredit != null) {
	            totalTeamCredits += userCredit.getCurrentBalance();
	        }
	    }
	    
	    AgencyCreditBalanceDTO balanceDTO = new AgencyCreditBalanceDTO();
	    balanceDTO.setAgencyId(agencyId);
	    balanceDTO.setAgencyName(agency.getName());
	    balanceDTO.setAgencyPoolBalance(agencyCredit.getCurrentBalance());
	    balanceDTO.setTotalTeamCredits(totalTeamCredits);
	    balanceDTO.setTeamDistributionEnabled(agencyCredit.getTeamDistributionEnabled());
	    balanceDTO.setDistributionPercentage(agencyCredit.getDistributionPercentage());
	    balanceDTO.setTotalAgents(agents.size());
	    
	    // Get recent agency transactions and convert to DTOs
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<CreditTransaction> recentTransactionsPage = creditTransactionRepository
	            .findByAgencyOrderByCreatedAtDesc(agency, pageable);
	    
	    List<CreditTransactionResponseDTO> transactionDTOs = recentTransactionsPage.getContent()
	            .stream()
	            .map(CreditTransaction::toDTO)
	            .collect(Collectors.toList());
	    
	    balanceDTO.setRecentTransactions(transactionDTOs);
	    
	    return balanceDTO;
	}
	
	/**
     * Add credits to agency pool
     */
    private void addCreditsToAgency(Long agencyId, Integer credits, String description) {
        AgencyCredit agencyCredit = agencyCreditRepository.findByAgencyId(agencyId)
                .orElseGet(() -> {
                    Agency agency = agencyRepository.findById(agencyId)
                            .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));
                    return new AgencyCredit(agency);
                });
        
        agencyCredit.addCredits(credits);
        agencyCreditRepository.save(agencyCredit);
        
        // Also create a transaction record
        Agency agency = agencyRepository.findById(agencyId).orElseThrow();
        CreditTransaction agencyTransaction = new CreditTransaction(
            agency.getAdmin(), // Use agency admin as user
            credits,
            CreditTransactionType.AGENCY_PURCHASE,
            description,
            agency
        );
        creditTransactionRepository.save(agencyTransaction);
    }

	/**
	 * Verify user can purchase for agency
	 */
	private void verifyCanPurchaseForAgency(User user, Agency agency) {
		// Agency admin can always purchase
		if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()
				&& user.getOwnedAgency().get().getId().equals(agency.getId())) {
			return;
		}

		// Check if user is agent with billing permission
		Agent agent = agentRepository.findByUserIdAndAgencyId(user.getId(), agency.getId())
				.orElseThrow(() -> new BusinessRuleException("You are not an agent in this agency"));

		if (!agent.getCanManageBilling() && agent.getRole() != AgentRole.OWNER) {
			throw new BusinessRuleException("You don't have permission to purchase credits");
		}
	}
	
	/**
	 * Get team credit summary
	 */
	/**
	 * Get team credit summary
	 */
	public TeamCreditSummaryDTO getTeamCreditSummary(Long agencyId) {
	    Agency agency = agencyRepository.findById(agencyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

	    List<Agent> agents = agentRepository.findByAgencyAndIsActive(agency, true);

	    TeamCreditSummaryDTO summary = new TeamCreditSummaryDTO();
	    summary.setAgencyId(agencyId);
	    summary.setAgencyName(agency.getName());
	    summary.setTotalAgents(agents.size());

	    // Calculate team credit totals
	    int totalCredits = 0;
	    int agentWithCredits = 0;

	    for (Agent agent : agents) {
	        UserCredit userCredit = userCreditRepository.findByUserId(agent.getUser().getId()).orElse(null);
	        if (userCredit != null && userCredit.getCurrentBalance() != null) {
	            int balance = userCredit.getCurrentBalance();
	            totalCredits += balance;
	            if (balance > 0) {
	                agentWithCredits++;
	            }
	        }
	    }

	    summary.setTotalTeamCredits(totalCredits);
	    summary.setAgentsWithCredits(agentWithCredits);
	    summary.setAverageCreditsPerAgent(agents.isEmpty() ? 0 : totalCredits / agents.size());

	    // Get recent agency transactions and convert to DTOs
	    Pageable pageable = PageRequest.of(0, 5);
	    Page<CreditTransaction> transactionsPage = creditTransactionRepository
	            .findByAgencyOrderByCreatedAtDesc(agency, pageable);
	    
	    List<CreditTransactionResponseDTO> transactionDTOs = transactionsPage.getContent()
	            .stream()
	            .map(CreditTransaction::toDTO)
	            .collect(Collectors.toList());
	    
	    summary.setRecentTransactions(transactionDTOs);

	    return summary;
	}
}