package com.doublez.backend.service.usage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.analytics.AgencyUsageStats;
import com.doublez.backend.entity.ContractorProfile;
import com.doublez.backend.entity.OwnerProfile;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.exception.BusinessRuleException;
import com.doublez.backend.exception.LimitExceededException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.credit.CreditService;
import com.doublez.backend.upgrade.UpgradeSuggestion;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class TierLimitationService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final RealEstateRepository realEstateRepository;
    private final CreditService creditService;

    public TierLimitationService(UserRepository userRepository, 
                               AgencyRepository agencyRepository,
                               RealEstateRepository realEstateRepository,
                               CreditService creditService) {
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
        this.realEstateRepository = realEstateRepository;
        this.creditService = creditService;
    }

    // ========================
    // AGENCY TIER LIMITATIONS
    // ========================

    /**
     * Check if agency can create more listings based on tier limits
     */
    public boolean canAgencyCreateListing(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
            .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencyId));
        
        UserTier effectiveTier = agency.getEffectiveTier();
        long currentListingsCount = realEstateRepository.countActiveRealEstatesByAgency(agencyId);
        
        return currentListingsCount < effectiveTier.getMaxListingsSafe();
    }

    /**
     * Check if agency can upload more images (simplified - you'll need image tracking)
     */
    public boolean canAgencyUploadImage(Long agencyId, int newImageCount) {
        Agency agency = agencyRepository.findById(agencyId)
            .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencyId));
        
        UserTier effectiveTier = agency.getEffectiveTier();
        
        // Check per-listing image limit
        if (newImageCount > effectiveTier.getMaxImagesPerListing()) {
            return false;
        }
        
        // For now, we'll assume basic validation
        // You'll need to implement actual image counting per agency
        return true;
    }

    /**
     * Get agency usage statistics
     */
    public AgencyUsageStats getAgencyUsageStats(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
            .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencyId));
        
        UserTier effectiveTier = agency.getEffectiveTier();
        long listingCount = realEstateRepository.countActiveRealEstatesByAgency(agencyId);
        long totalImages = realEstateRepository.getTotalImageCountByAgencyId(agencyId); 
        
        // Calculate usage percentages
        double listingUsagePercentage = calculatePercentage(listingCount, effectiveTier.getMaxListingsSafe());
        double imageUsagePercentage = calculatePercentage(totalImages, effectiveTier.getMaxImagesSafe());
        
        return AgencyUsageStats.builder()
            .agencyId(agencyId)
            .effectiveTier(effectiveTier)
            .currentListings(listingCount)
            .maxListings(effectiveTier.getMaxListingsSafe())
            .currentTotalImages(totalImages) 
            .maxTotalImages(effectiveTier.getMaxImagesSafe())
            .maxImagesPerListing(effectiveTier.getMaxImagesPerListing())
            .listingUsagePercentage(listingUsagePercentage)
            .imageUsagePercentage(imageUsagePercentage) 
            .isInTrial(agency.isInTrialPeriod())
            .trialDaysRemaining(agency.getTrialDaysRemaining())
            .build();
    }

    // ========================
    // OWNER LIMITATIONS
    // ========================

    /**
     * Check if owner can create more free listings
     */
    public boolean canOwnerCreateFreeListing(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        if (!user.isOwner()) {
            throw new BusinessRuleException("User is not an owner");
        }
        
        OwnerProfile ownerProfile = user.getOwnerProfile();
        if (ownerProfile == null) {
            return true; // No profile yet, can create first listing
        }
        
        return ownerProfile.getPropertiesListed() < 3; // 3 free listings for owners
    }

    /**
     * Check if owner can feature a listing (requires credits)
     */
    public boolean canOwnerFeatureListing(Long userId) {
        return creditService.hasSufficientCredits(userId, 50); // 50 credits for featuring
    }

    // ========================
    // INVESTOR LIMITATIONS
    // ========================

    /**
     * Check if investor can post more projects
     */
    public boolean canInvestorPostProject(Long userId) {
        return creditService.hasSufficientCredits(userId, 10); // 10 credits per project
    }

    /**
     * Check if investor can feature a project
     */
    public boolean canInvestorFeatureProject(Long userId) {
        return creditService.hasSufficientCredits(userId, 30); // 30 credits for featuring
    }

    // ========================
    // CONTRACTOR LIMITATIONS
    // ========================

    /**
     * Check if contractor can make profile visible (requires subscription)
     */
    public boolean canContractorToggleVisibility(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        if (!user.isContractor()) {
            throw new BusinessRuleException("User is not a contractor");
        }
        
        ContractorProfile profile = user.getContractorProfile();
        if (profile == null) {
            return false; // No profile to make visible
        }
        
        // Check if they have active visibility subscription or can purchase
        return creditService.hasSufficientCredits(userId, 20) || Boolean.TRUE.equals(profile.getIsVisible());
    }

    // ========================
    // VALIDATION METHODS
    // ========================

    /**
     * Validate listing creation for any user type
     */
    public void validateListingCreation(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()) {
            Agency agency = user.getOwnedAgency().get();
            if (!canAgencyCreateListing(agency.getId())) {
                throw new LimitExceededException(
                    "Agency has reached listing limit for tier: " + agency.getEffectiveTier().getDisplayName()
                );
            }
        } else if (user.isOwner()) {
            if (!canOwnerCreateFreeListing(userId)) {
                throw new LimitExceededException(
                    "Owner has reached free listing limit (3). Please use credits for additional listings."
                );
            }
        }
        // Add other user type validations as needed
    }

    // ========================
    // UPGRADE SUGGESTIONS
    // ========================

    /**
     * Get upgrade suggestions for agency based on usage
     */
    public List<UpgradeSuggestion> getAgencyUpgradeSuggestions(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
            .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencyId));
        
        AgencyUsageStats stats = getAgencyUsageStats(agencyId);
        List<UpgradeSuggestion> suggestions = new ArrayList<>();
        
        UserTier currentTier = agency.getTier(); // Actual tier, not effective
        
        // Check if agency is approaching limits
        if (stats.getListingUsagePercentage() > 80) {
            suggestions.add(createListingLimitSuggestion(currentTier, stats));
        }
        
        // Check if trial is ending
        if (agency.isInTrialPeriod() && agency.getTrialDaysRemaining() <= 7) {
            suggestions.add(createTrialEndingSuggestion(agency));
        }
        
        return suggestions;
    }

    // ========================
    // HELPER METHODS
    // ========================

    private double calculatePercentage(long current, long max) {
        if (max == 0) return 0;
        return (double) current / max * 100;
    }

    private UpgradeSuggestion createListingLimitSuggestion(UserTier currentTier, AgencyUsageStats stats) {
        UserTier nextTier = getNextTier(currentTier);
        return UpgradeSuggestion.builder()
            .type("LISTING_LIMIT")
            .message(String.format("Iskoristili ste %d/%d oglasa (%d%%). Razmislite o nadogradnji na %s za %d oglasa.",
                stats.getCurrentListings(), stats.getMaxListings(), 
                (int) stats.getListingUsagePercentage(), nextTier.getDisplayName(), nextTier.getMaxListingsSafe()))
            .suggestedTier(nextTier)
            .priority("HIGH")
            .build();
    }

    private UpgradeSuggestion createTrialEndingSuggestion(Agency agency) {
        return UpgradeSuggestion.builder()
            .type("TRIAL_ENDING")
            .message(String.format("Vaš probni period ističe za %d dana. Odaberite paket da nastavite korišćenje platforme.",
                agency.getTrialDaysRemaining()))
            .suggestedTier(UserTier.AGENCY_BASIC) // Suggest BASIC as minimum
            .priority("URGENT")
            .build();
    }

    private UserTier getNextTier(UserTier currentTier) {
        return switch (currentTier) {
            case AGENCY_FREE -> UserTier.AGENCY_BASIC;
            case AGENCY_BASIC -> UserTier.AGENCY_PRO;
            case AGENCY_PRO -> UserTier.AGENCY_PREMIUM;
            default -> UserTier.AGENCY_PREMIUM; // Already at highest
        };
    }
    
    /**
     * Get maximum number of agents allowed for an agency based on tier
     */
    public int getMaxAgentsForAgency(Agency agency) {
        UserTier effectiveTier = agency.getEffectiveTier();
        
        switch (effectiveTier) {
            case AGENCY_FREE:
                return 1; // Only the owner
            case AGENCY_BASIC:
                return 3; // Owner + 2 agents
            case AGENCY_PRO:
                return 10; // Owner + 9 agents
            case AGENCY_PREMIUM:
                return 25; // Owner + 24 agents
            case ADMIN:
                return 100; // Unlimited for admin
            default:
                return 1;
        }
    }

    /**
     * Get maximum number of super agents allowed for an agency based on tier
     */
    public int getMaxSuperAgentsForAgency(Agency agency) {
        UserTier effectiveTier = agency.getEffectiveTier();
        
        switch (effectiveTier) {
            case AGENCY_FREE:
                return 0; // No super agents on free tier
            case AGENCY_BASIC:
                return 1; // 1 super agent
            case AGENCY_PRO:
                return 3; // 3 super agents
            case AGENCY_PREMIUM:
                return 5; // 5 super agents
            case ADMIN:
                return 10; // 10 for admin
            default:
                return 0;
        }
    }

    /**
     * Get maximum listings per agent based on agency tier and agent role
     */
    public int getMaxListingsPerAgent(Agency agency, AgentRole agentRole) {
        UserTier effectiveTier = agency.getEffectiveTier();
        
        switch (effectiveTier) {
            case AGENCY_FREE:
                return agentRole == AgentRole.OWNER ? 5 : 0;
            case AGENCY_BASIC:
                return agentRole == AgentRole.OWNER ? 20 : 
                       agentRole == AgentRole.SUPER_AGENT ? 15 : 10;
            case AGENCY_PRO:
                return agentRole == AgentRole.OWNER ? 50 : 
                       agentRole == AgentRole.SUPER_AGENT ? 30 : 20;
            case AGENCY_PREMIUM:
                return agentRole == AgentRole.OWNER ? 100 : 
                       agentRole == AgentRole.SUPER_AGENT ? 50 : 30;
            case ADMIN:
                return 1000; // Essentially unlimited
            default:
                return 10;
        }
    }

    /**
     * Check if agency can add more agents
     */
    public boolean canAgencyAddAgent(Agency agency, AgentRole roleToAdd) {
        // This will be called from TeamService
        return true; // Actual validation done in TeamService
    }
}