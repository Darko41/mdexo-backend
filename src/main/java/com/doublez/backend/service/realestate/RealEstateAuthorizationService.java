package com.doublez.backend.service.realestate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.exception.ResourceNotFoundException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.AgencyRepository;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserLimitationRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.UserService;
import com.doublez.backend.utils.RoleUtils;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RealEstateAuthorizationService {

    private final RealEstateRepository realEstateRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserLimitationRepository limitationRepository;
    private final AgencyRepository agencyRepository; 

    public RealEstateAuthorizationService(
            RealEstateRepository realEstateRepository,
            UserService userService,
            UserRepository userRepository,
            UserLimitationRepository limitationRepository,
            AgencyRepository agencyRepository) {
        this.realEstateRepository = realEstateRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.limitationRepository = limitationRepository;
        this.agencyRepository = agencyRepository;
    }

    // -----------------------
    // Ownership & Basic Auth
    // -----------------------

    /**
     * Returns true if currently authenticated user is the owner of given propertyId.
     * Now supports both individual owners and agency admins.
     */
    public boolean isOwner(Long propertyId) {
        RealEstate property = realEstateRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        
        User currentUser = userService.getAuthenticatedUser();
        
        // Check individual ownership
        User owner = property.getOwner();
        if (owner != null && owner.getId().equals(currentUser.getId())) {
            return true;
        }
        
        // Check agency ownership
        Agency agency = property.getAgency();
        if (agency != null && agency.getAdmin().getId().equals(currentUser.getId())) {
            return true;
        }
        
        return false;
    }

    /**
     * Allow create if user has appropriate role and within limits.
     * ROLE_USER, ROLE_AGENCY_ADMIN, or ROLE_ADMIN can create properties.
     */
    public boolean hasRealEstateCreateAccess() {
        // Admins always can
        if (hasRole("ADMIN")) return true;

        // Agency admins and regular users can create if within limits
        if (hasAnyRole("AGENCY_ADMIN", "USER")) {
            Long currentUserId = userService.getCurrentUserId();
            return canCreateRealEstate(currentUserId);
        }

        return false;
    }

    /**
     * Allow update if admin OR owner of property (individual or agency admin).
     */
    public boolean hasRealEstateUpdateAccess(Long propertyId) {
        if (hasRole("ADMIN")) return true;

        // Agency admins and regular users can update their properties
        if (hasAnyRole("AGENCY_ADMIN", "USER")) {
            return isOwner(propertyId);
        }

        return false;
    }

    /**
     * Allow delete if admin OR owner of property (individual or agency admin).
     */
    public boolean hasRealEstateDeleteAccess(Long propertyId) {
        return hasRealEstateUpdateAccess(propertyId); // Same logic as update
    }

    /**
     * Check if user can feature a listing based on role and limits.
     */
    public boolean hasRealEstateFeatureAccess(Long propertyId) {
        if (!hasRealEstateUpdateAccess(propertyId)) {
            return false; // Must be able to update the property first
        }

        Long currentUserId = userService.getCurrentUserId();
        return canFeatureRealEstate(currentUserId);
    }

    // -----------------------
    // Agency-specific Auth
    // -----------------------

    /**
     * Check if user is admin of any agency
     */
    public boolean isAgencyAdmin() {
        User currentUser = userService.getAuthenticatedUser();
        return currentUser.isAgencyAdmin();
    }

    /**
     * Check if user is admin of the specific agency
     */
    public boolean isAgencyAdmin(Long agencyId) {
        User currentUser = userService.getAuthenticatedUser();
        if (!currentUser.isAgencyAdmin()) return false;
        
        return agencyRepository.findById(agencyId)
                .map(agency -> agency.getAdmin().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    /**
     * Returns UsageStats for given userId.
     * Includes current counts and effective limitations (trial-aware).
     */
    public UsageStatsDTO getUsageStats(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        long currentListings = realEstateRepository.countActiveRealEstatesByUser(userId);
        long currentImages = userService.countImages(userId);
        long currentFeatured = realEstateRepository.countFeaturedRealEstatesByUser(userId);

        UserLimitation limits = getEffectiveLimitations(user);

        UsageStatsDTO stats = new UsageStatsDTO();
        stats.setCurrentListings(currentListings);
        stats.setCurrentImages(currentImages);
        stats.setCurrentFeatured(currentFeatured);
        stats.setMaxListings(limits.getMaxListings());
        stats.setMaxImages(limits.getMaxImages());
        stats.setMaxFeatured(limits.getMaxFeaturedListings());
        stats.setCanCreateListing(currentListings < limits.getMaxListings());
        stats.setCanUploadImage(currentImages < limits.getMaxImages());
        stats.setCanFeatureListing(limits.getCanFeatureListings() && currentFeatured < limits.getMaxFeaturedListings());
        stats.setLimitations(limits);

        return stats;
    }

    /**
     * Returns true if user can create one more real estate (respecting limits).
     */
    public boolean canCreateRealEstate(Long userId) {
        UsageStatsDTO stats = getUsageStats(userId);
        return stats.isCanCreateListing();
    }

    /**
     * Returns true if user can upload given number of images.
     */
    public boolean canUploadImages(Long userId, Integer countToUpload) {
        if (countToUpload == null || countToUpload <= 0) countToUpload = 1;

        UsageStatsDTO stats = getUsageStats(userId);
        long available = stats.getLimitations().getMaxImages() - stats.getCurrentImages(); // ✅ FIXED: getMaxImages()
        return available >= countToUpload;
    }

    /**
     * Returns true if user may feature another listing.
     */
    public boolean canFeatureRealEstate(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserLimitation limits = getEffectiveLimitations(user);
        if (!limits.getCanFeatureListings()) return false;

        long featuredCount = realEstateRepository.countActiveFeaturedRealEstatesByUser(userId);
        return featuredCount < limits.getMaxFeaturedListings();
    }

    // -----------------------
    // Helpers (Keep existing)
    // -----------------------

    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return RoleUtils.hasRole(auth, role);
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return RoleUtils.hasAnyRole(auth, roles);
    }

    /**
     * Compute effective limitations for user, applying trial boosts.
     */
    public UserLimitation getEffectiveLimitations(User user) {
        UserLimitation base = limitationRepository.findByTier(user.getTier())
                .orElseThrow(() -> new RuntimeException("Limitations not found for tier: " + user.getTier()));

        if (user.isInTrialPeriod()) {
            UserLimitation enhanced = new UserLimitation();
            enhanced.setId(base.getId());
            enhanced.setTier(base.getTier());
            enhanced.setPricePerMonth(base.getPricePerMonth());

            enhanced.setMaxListings(safeIntSum(base.getMaxListings(), 5));
            enhanced.setMaxImages(safeIntSum(base.getMaxImages(), 20));
            enhanced.setMaxImagesPerListing(safeIntSum(base.getMaxImagesPerListing(), 3));
            enhanced.setCanFeatureListings(true);
            enhanced.setMaxFeaturedListings(Math.max(3, base.getMaxFeaturedListings() == null ? 0 : base.getMaxFeaturedListings()));

            return enhanced;
        }

        return base;
    }

    private Integer safeIntSum(Integer a, Integer b) {
        int av = a == null ? 0 : a;
        int bv = b == null ? 0 : b;
        return av + bv;
    }
}