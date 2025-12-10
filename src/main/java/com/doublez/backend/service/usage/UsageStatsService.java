package com.doublez.backend.service.usage;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.analytics.AgencyUsageStats;
import com.doublez.backend.dto.analytics.UserUsageDashboard;
import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserTier;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.repository.credit.CreditTransactionRepository;
import com.doublez.backend.service.credit.CreditService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsageStatsService {
    
    private final TierLimitationService tierLimitationService;
    private final RealEstateRepository realEstateRepository;
    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final CreditService creditService;

    public UsageStatsService(TierLimitationService tierLimitationService,
                           RealEstateRepository realEstateRepository,
                           UserRepository userRepository,
                           CreditTransactionRepository creditTransactionRepository,
                           CreditService creditService) {
        this.tierLimitationService = tierLimitationService;
        this.realEstateRepository = realEstateRepository;
        this.userRepository = userRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.creditService = creditService;
    }

    /**
     * Get simple usage stats for frontend with REAL image counting
     */
    public UsageStatsDTO getSimpleUsageStats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        UserTier effectiveTier = user.getEffectiveTier();
        
        long currentListings = realEstateRepository.countActiveRealEstatesByUser(userId);
        long currentImages = realEstateRepository.getTotalImageCountByUserId(userId);
        long currentFeatured = realEstateRepository.countActiveFeaturedRealEstatesByUser(userId);
        
        return new UsageStatsDTO(
            currentListings,
            currentImages,
            currentFeatured,
            effectiveTier,
            user.isBusiness(),
            effectiveTier.getMaxListingsSafe(),
            effectiveTier.getMaxImagesSafe(),
            3 // Max featured listings
        );
    }
    
    // 🆕 METHOD TO VALIDATE IMAGE UPLOAD
    public boolean canUploadImage(Long userId, Long realEstateId, int newImageCount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()) {
            Agency agency = user.getOwnedAgency().get();
            return tierLimitationService.canAgencyUploadImage(agency.getId(), newImageCount);
        }
        
        // For individual users, check per-listing limits
        Integer currentImages = realEstateRepository.getImageCountByRealEstateId(realEstateId);
        UserTier tier = user.getEffectiveTier();
        return (currentImages + newImageCount) <= tier.getMaxImagesPerListing();
    }

    /**
     * Get agency detailed stats
     */
    public AgencyUsageStats getAgencyDetailedStats(Long agencyId) {
        return tierLimitationService.getAgencyUsageStats(agencyId);
    }

    /**
     * Get comprehensive usage dashboard for user
     */
    public UserUsageDashboard getUsageDashboard(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        UserUsageDashboard dashboard = new UserUsageDashboard();
        dashboard.setUserId(userId);
        dashboard.setUserTier(user.getEffectiveTier());
        dashboard.setCurrentCredits(user.getCurrentCreditBalance());
        
        // Simple stats for quick overview
        dashboard.setSimpleStats(getSimpleUsageStats(userId));
        
        // Agency-specific data
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()) {
            Agency agency = user.getOwnedAgency().get();
            dashboard.setAgencyUsageStats(getAgencyDetailedStats(agency.getId()));
            dashboard.setUpgradeSuggestions(
                tierLimitationService.getAgencyUpgradeSuggestions(agency.getId())
            );
        }
        
        // Credit transaction history
        List<CreditTransaction> recentTransactions = 
            creditTransactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        dashboard.setRecentTransactions(recentTransactions.stream()
            .map(CreditTransaction::toDTO)
            .collect(Collectors.toList()));
        
        return dashboard;
    }

    /**
     * Track listing creation and update usage statistics
     */
    public void trackListingCreation(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        
        if (user.isOwner() && user.getOwnerProfile() != null) {
            user.getOwnerProfile().incrementPropertiesListed();
            userRepository.save(user);
        }
        
        // Log the activity
        // You can add more detailed tracking here
    }

    /**
     * Track credit usage for features
     */
    public void trackCreditUsage(Long userId, Integer credits, String feature, String description) {
        // This integrates with CreditService
        // You can add additional tracking logic here if needed
    }
}
