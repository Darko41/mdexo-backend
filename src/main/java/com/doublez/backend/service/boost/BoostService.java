package com.doublez.backend.service.boost;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.doublez.backend.config.CreditPricingConfiguration;
import com.doublez.backend.dto.boost.BoostOption;
import com.doublez.backend.dto.boost.BoostPackageOption;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.FeatureNotImplementedException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.repository.realestate.RealEstateRepository;
import com.doublez.backend.service.credit.CreditService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class BoostService {
    
    private final CreditService creditService;
    private final RealEstateRepository realEstateRepository;
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;

    public BoostService(CreditService creditService,
                       RealEstateRepository realEstateRepository,
                       UserRepository userRepository,
                       AgencyRepository agencyRepository) {
        this.creditService = creditService;
        this.realEstateRepository = realEstateRepository;
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
    }

    // ===== LISTING BOOSTS =====

    public boolean applyTopPositioningBoost(Long listingId, Long userId, int durationDays) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS, 
            "Top positioning boost for listing " + listingId + " for " + durationDays + " days");
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setBoostedUntil(LocalDateTime.now().plusDays(durationDays));
            listing.setBoostType("TOP_POSITIONING");
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    public boolean applyUrgentBadge(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.URGENT_BADGE_14DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.URGENT_BADGE_14DAYS, 
            "Urgent badge for listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setUrgentBadgeUntil(LocalDateTime.now().plusDays(14));
            listing.setHasUrgentBadge(true);
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    public boolean applyHighlightedListing(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.HIGHLIGHTED_LISTING_30DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.HIGHLIGHTED_LISTING_30DAYS, 
            "Highlighted listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setHighlightedUntil(LocalDateTime.now().plusDays(30));
            listing.setIsHighlighted(true);
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    public boolean applyCategoryFeature(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.FEATURED_IN_CATEGORY_15DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.FEATURED_IN_CATEGORY_15DAYS, 
            "Category feature for listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setCategoryFeaturedUntil(LocalDateTime.now().plusDays(15));
            listing.setIsCategoryFeatured(true);
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    // ===== VISIBILITY BOOSTS =====

    public boolean applySocialMediaPromotion(Long listingId, Long userId) {
    	throw new FeatureNotImplementedException("Social media promotion is not yet available. Coming soon!");
    	
//        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.SOCIAL_MEDIA_PROMOTION)) {
//            return false;
//        }
        
//        return creditService.deductCredits(userId, 
//            CreditPricingConfiguration.SOCIAL_MEDIA_PROMOTION, 
//            "Social media promotion for listing " + listingId);
    }

    public boolean applyNewsletterFeature(Long listingId, Long userId) {
    	throw new FeatureNotImplementedException("Newsletter feature is not yet available. Coming soon!");
//        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.NEWSLETTER_FEATURE)) {
//            return false;
//        }
//        
//        return creditService.deductCredits(userId, 
//            CreditPricingConfiguration.NEWSLETTER_FEATURE, 
//            "Newsletter feature for listing " + listingId);
    }

    public boolean applyCrossPromotion(Long listingId, Long userId) {
    	throw new FeatureNotImplementedException("Cross promotion is not yet available. Coming soon!");
//        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.CROSS_PROMOTION)) {
//            return false;
//        }
//        
//        boolean deducted = creditService.deductCredits(userId, 
//            CreditPricingConfiguration.CROSS_PROMOTION, 
//            "Cross-promotion for listing " + listingId);
//            
//        if (deducted) {
//            RealEstate listing = realEstateRepository.findById(listingId)
//                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
//            
//            listing.setCrossPromotionUntil(LocalDateTime.now().plusDays(7));
//            realEstateRepository.save(listing);
//        }
//        
//        return deducted;
    }

    public boolean applyMobilePushNotification(Long listingId, Long userId) {
    	throw new FeatureNotImplementedException("Mobile push notifications are not yet available. Coming soon!");
//        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.MOBILE_PUSH_NOTIFICATION)) {
//            return false;
//        }
//        
//        return creditService.deductCredits(userId, 
//            CreditPricingConfiguration.MOBILE_PUSH_NOTIFICATION, 
//            "Mobile push notification for listing " + listingId);
    }

    // ===== PROFILE BOOSTS =====

    public boolean applyVerifiedBadge(Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.VERIFIED_BADGE_30DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.VERIFIED_BADGE_30DAYS, 
            "Verified badge for user profile");
            
        if (deducted) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            user.setVerifiedBadgeUntil(LocalDateTime.now().plusDays(30));
            user.setHasVerifiedBadge(true);
            userRepository.save(user);
        }
        
        return deducted;
    }

    public boolean applyPremiumProfileBadge(Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.PREMIUM_PROFILE_BADGE_30DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.PREMIUM_PROFILE_BADGE_30DAYS, 
            "Premium profile badge");
            
        if (deducted) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            user.setPremiumBadgeUntil(LocalDateTime.now().plusDays(30));
            user.setHasPremiumBadge(true);
            userRepository.save(user);
        }
        
        return deducted;
    }

    public boolean applyAgencyFeaturedProfile(Long agencyId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.AGENCY_FEATURED_PROFILE_15DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.AGENCY_FEATURED_PROFILE_15DAYS, 
            "Featured agency profile for agency " + agencyId);
            
        if (deducted) {
            Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
            
            agency.setFeaturedUntil(LocalDateTime.now().plusDays(15));
            agency.setIsFeatured(true);
            agencyRepository.save(agency);
        }
        
        return deducted;
    }

    // ===== AGENCY-SPECIFIC BOOSTS =====

    public boolean applyMultipleListingBoost(Long userId, List<Long> listingIds) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.MULTIPLE_LISTING_BOOST_7DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.MULTIPLE_LISTING_BOOST_7DAYS, 
            "Multiple listing boost for " + listingIds.size() + " listings");
            
        if (deducted && listingIds != null && !listingIds.isEmpty()) {
            List<RealEstate> listings = realEstateRepository.findAllById(listingIds);
            for (RealEstate listing : listings) {
                listing.setBoostedUntil(LocalDateTime.now().plusDays(7));
                listing.setBoostType("TOP_POSITIONING");
            }
            realEstateRepository.saveAll(listings);
        }
        
        return deducted;
    }

    public boolean applyAgencyShowcaseFeature(Long agencyId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.AGENCY_SHOWCASE_FEATURE_30DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.AGENCY_SHOWCASE_FEATURE_30DAYS, 
            "Agency showcase feature for agency " + agencyId);
            
        if (deducted) {
            Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
            
            agency.setShowcaseFeaturedUntil(LocalDateTime.now().plusDays(30));
            agency.setIsShowcaseFeatured(true);
            agencyRepository.save(agency);
        }
        
        return deducted;
    }

    public boolean applyPremiumAgencyBadge(Long agencyId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.PREMIUM_AGENCY_BADGE_30DAYS)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.PREMIUM_AGENCY_BADGE_30DAYS, 
            "Premium agency badge for agency " + agencyId);
            
        if (deducted) {
            Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found"));
            
            agency.setPremiumBadgeUntil(LocalDateTime.now().plusDays(30));
            agency.setHasPremiumBadge(true);
            agencyRepository.save(agency);
        }
        
        return deducted;
    }

    public boolean applyAgencyPrioritySupport(Long agencyId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.AGENCY_PRIORITY_SUPPORT_30DAYS)) {
            return false;
        }
        
        return creditService.deductCredits(userId, 
            CreditPricingConfiguration.AGENCY_PRIORITY_SUPPORT_30DAYS,
            "Agency priority support for agency " + agencyId);
    }

    // ===== BOOST PACKAGES =====

    public boolean applyBronzeBoostPackage(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.BRONZE_BOOST_PACKAGE)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.BRONZE_BOOST_PACKAGE, 
            "Bronze boost package for listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setBoostedUntil(LocalDateTime.now().plusDays(7));
            listing.setBoostType("TOP_POSITIONING");
            listing.setUrgentBadgeUntil(LocalDateTime.now().plusDays(14));
            listing.setHasUrgentBadge(true);
            
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    public boolean applySilverBoostPackage(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.SILVER_BOOST_PACKAGE)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.SILVER_BOOST_PACKAGE, 
            "Silver boost package for listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setBoostedUntil(LocalDateTime.now().plusDays(7));
            listing.setBoostType("TOP_POSITIONING");
            listing.setUrgentBadgeUntil(LocalDateTime.now().plusDays(14));
            listing.setHasUrgentBadge(true);
            listing.setHighlightedUntil(LocalDateTime.now().plusDays(30));
            listing.setIsHighlighted(true);
            listing.setCategoryFeaturedUntil(LocalDateTime.now().plusDays(15));
            listing.setIsCategoryFeatured(true);
            
            realEstateRepository.save(listing);
        }
        
        return deducted;
    }

    public boolean applyGoldBoostPackage(Long listingId, Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.GOLD_BOOST_PACKAGE)) {
            return false;
        }
        
        boolean deducted = creditService.deductCredits(userId, 
            CreditPricingConfiguration.GOLD_BOOST_PACKAGE, 
            "Gold boost package for listing " + listingId);
            
        if (deducted) {
            RealEstate listing = realEstateRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found"));
            
            listing.setBoostedUntil(LocalDateTime.now().plusDays(7));
            listing.setBoostType("TOP_POSITIONING");
            listing.setUrgentBadgeUntil(LocalDateTime.now().plusDays(14));
            listing.setHasUrgentBadge(true);
            listing.setHighlightedUntil(LocalDateTime.now().plusDays(30));
            listing.setIsHighlighted(true);
            listing.setCategoryFeaturedUntil(LocalDateTime.now().plusDays(15));
            listing.setIsCategoryFeatured(true);
            
            realEstateRepository.save(listing);
            
            // Apply profile badges to user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
            user.setVerifiedBadgeUntil(LocalDateTime.now().plusDays(30));
            user.setHasVerifiedBadge(true);
            user.setPremiumBadgeUntil(LocalDateTime.now().plusDays(30));
            user.setHasPremiumBadge(true);
            userRepository.save(user);
        }
        
        return deducted;
    }

    // ===== EXTRA ALLOWANCES =====

    public boolean purchaseExtraListingSlot(Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.EXTRA_LISTING_SLOT)) {
            return false;
        }
        
        return creditService.deductCredits(userId, 
            CreditPricingConfiguration.EXTRA_LISTING_SLOT,
            "Extra listing slot purchase");
    }

    public boolean purchaseExtraImageSlots(Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.EXTRA_IMAGE_SLOT_10IMAGES)) {
            return false;
        }
        
        return creditService.deductCredits(userId, 
            CreditPricingConfiguration.EXTRA_IMAGE_SLOT_10IMAGES,
            "Extra image slots (10 images) purchase");
    }

    public boolean purchasePremiumSupport(Long userId) {
        if (!creditService.hasSufficientCredits(userId, CreditPricingConfiguration.PREMIUM_SUPPORT_7DAYS)) {
            return false;
        }
        
        return creditService.deductCredits(userId, 
            CreditPricingConfiguration.PREMIUM_SUPPORT_7DAYS,
            "Premium support for 7 days");
    }

    // ===== VALIDATION & QUERY METHODS =====

    public boolean canApplyBoost(Long userId, int boostCost) {
        return creditService.hasSufficientCredits(userId, boostCost);
    }

    public List<BoostOption> getAvailableBoosts(Long userId) {
        List<BoostOption> boosts = new ArrayList<>();
        int currentCredits = creditService.getCurrentBalance(userId);
        
        // ACTIVE BOOSTS (Ready to use)
        boosts.add(new BoostOption("TOP_POSITIONING", "Top Positioning (7 days)", 
            CreditPricingConfiguration.TOP_POSITIONING_BOOST_7DAYS, currentCredits));
        boosts.add(new BoostOption("URGENT_BADGE", "Urgent Badge (14 days)", 
            CreditPricingConfiguration.URGENT_BADGE_14DAYS, currentCredits));
        boosts.add(new BoostOption("HIGHLIGHTED", "Highlighted Listing (30 days)", 
            CreditPricingConfiguration.HIGHLIGHTED_LISTING_30DAYS, currentCredits));
        boosts.add(new BoostOption("CATEGORY_FEATURE", "Category Feature (15 days)", 
            CreditPricingConfiguration.FEATURED_IN_CATEGORY_15DAYS, currentCredits));
        boosts.add(new BoostOption("VERIFIED_BADGE", "Verified Badge (30 days)", 
            CreditPricingConfiguration.VERIFIED_BADGE_30DAYS, currentCredits));
        boosts.add(new BoostOption("PREMIUM_PROFILE", "Premium Profile Badge (30 days)", 
            CreditPricingConfiguration.PREMIUM_PROFILE_BADGE_30DAYS, currentCredits));
        
        // AGENCY-SPECIFIC BOOSTS (Only show to agencies)
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (user.isAgencyAdmin() && user.getOwnedAgency().isPresent()) {
            boosts.add(new BoostOption("MULTIPLE_LISTING_BOOST", "Multiple Listing Boost (7 days)", 
                CreditPricingConfiguration.MULTIPLE_LISTING_BOOST_7DAYS, currentCredits));
            boosts.add(new BoostOption("AGENCY_SHOWCASE", "Agency Showcase Feature (30 days)", 
                CreditPricingConfiguration.AGENCY_SHOWCASE_FEATURE_30DAYS, currentCredits));
            boosts.add(new BoostOption("PREMIUM_AGENCY_BADGE", "Premium Agency Badge (30 days)", 
                CreditPricingConfiguration.PREMIUM_AGENCY_BADGE_30DAYS, currentCredits));
            boosts.add(new BoostOption("AGENCY_PRIORITY_SUPPORT", "Agency Priority Support (30 days)", 
                CreditPricingConfiguration.AGENCY_PRIORITY_SUPPORT_30DAYS, currentCredits));
        }
        
        return boosts;
    }

    public List<BoostPackageOption> getAvailableBoostPackages(Long userId) {
        List<BoostPackageOption> packages = new ArrayList<>();
        int currentCredits = creditService.getCurrentBalance(userId);
        
        if (currentCredits >= CreditPricingConfiguration.BRONZE_BOOST_PACKAGE) {
            packages.add(new BoostPackageOption("BRONZE", "Bronze Package", 
                CreditPricingConfiguration.BRONZE_BOOST_PACKAGE, 
                "Top Positioning + Urgent Badge", currentCredits));
        }
        
        if (currentCredits >= CreditPricingConfiguration.SILVER_BOOST_PACKAGE) {
            packages.add(new BoostPackageOption("SILVER", "Silver Package", 
                CreditPricingConfiguration.SILVER_BOOST_PACKAGE,
                "Top + Urgent + Highlighted + Category Featured", currentCredits));
        }
        
        if (currentCredits >= CreditPricingConfiguration.GOLD_BOOST_PACKAGE) {
            packages.add(new BoostPackageOption("GOLD", "Gold Package", 
                CreditPricingConfiguration.GOLD_BOOST_PACKAGE,
                "All boosts + Verified Badge + Premium Profile", currentCredits));
        }
        
        return packages;
    }
}