package com.doublez.backend.service.user;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.FeaturedUsageStatsDTO;
import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserLimitationRepository;
import com.doublez.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class LimitationService {

	private final UserRepository userRepository;
	private final UserLimitationRepository limitationRepository;
	private final RealEstateRepository realEstateRepository;

	public LimitationService(UserRepository userRepository,
			UserLimitationRepository limitationRepository,
			RealEstateRepository realEstateRepository) {
		this.userRepository = userRepository;
		this.limitationRepository = limitationRepository;
		this.realEstateRepository = realEstateRepository;
	}

	public UserLimitation getLimitationsForUser(User user) {
	    // During trial, users get their free tier limitations
	    // After trial, they get their actual paid tier limitations (trial logic is handled at USER LEVEL)
	    UserTier effectiveTier = user.getTier();
	    
	    return limitationRepository.findByTier(effectiveTier)
	            .orElseThrow(() -> new RuntimeException("Limitations not found for tier: " + effectiveTier));
	}

	public UsageStatsDTO getUsageStats(Long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new UserNotFoundException(userId));
	    
	    Long realEstateCount = userRepository.countActiveRealEstatesByUser(userId);
	    Long imageCount = userRepository.countImagesByUser(userId);
	    UserLimitation limitations = getLimitationsForUser(user);
	    FeaturedUsageStatsDTO featuredStats = getFeaturedUsageStats(userId);
	    
	    return new UsageStatsDTO(realEstateCount, imageCount, limitations, featuredStats);
	}

	public boolean canCreateRealEstate(Long userId) {
		UsageStatsDTO stats = getUsageStats(userId);
		return stats.isCanCreateListing();
	}

	public boolean canUploadImage(Long userId) {
		UsageStatsDTO stats = getUsageStats(userId);
		return stats.isCanUploadImage();
	}

	public boolean canFeatureRealEstate(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		UserLimitation limitations = getLimitationsForUser(user);

		if (!limitations.getCanFeatureListings()) {
			return false;
		}

		// Count current featured real estates
		Long featuredCount = realEstateRepository.countFeaturedRealEstatesByUser(userId);
		return featuredCount < limitations.getMaxFeaturedListings();
	}
	
	public FeaturedUsageStatsDTO getFeaturedUsageStats(Long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new UserNotFoundException(userId));
	    UserLimitation limitations = getLimitationsForUser(user);
	    
	    Long currentFeatured = realEstateRepository.countFeaturedRealEstatesByUser(userId);
	    boolean canFeatureMore = currentFeatured < limitations.getMaxFeaturedListings();
	    
	    return new FeaturedUsageStatsDTO(currentFeatured, limitations.getMaxFeaturedListings(), canFeatureMore);
	}
}