package com.doublez.backend.service.usage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.user.UsageStatsDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.LimitationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsageTrackingService {

	private final UserRepository userRepository;
	private final RealEstateRepository realEstateRepository;
	private final LimitationService limitationService;

	public UsageTrackingService(UserRepository userRepository, RealEstateRepository realEstateRepository,
			LimitationService limitationService) {
		this.userRepository = userRepository;
		this.realEstateRepository = realEstateRepository;
		this.limitationService = limitationService;
	}

	// Check if user can create a new real estate
	public boolean canCreateRealEstate(Long userId) {
		UsageStatsDTO stats = limitationService.getUsageStats(userId);
		return stats.isCanCreateListing();
	}

	// Check if user can upload more images
	public boolean canUploadImage(Long userId, int additionalImages) {
		UsageStatsDTO stats = limitationService.getUsageStats(userId);
		Long currentImages = userRepository.countImagesByUser(userId);
		return (currentImages + additionalImages) <= stats.getLimitations().getMaxImages();
	}

	// Check if user can feature a listing
	public boolean canFeatureListing(Long userId) {
		UsageStatsDTO stats = limitationService.getUsageStats(userId);
		if (!stats.getLimitations().getCanFeatureListings()) {
			return false;
		}

		Long currentFeatured = realEstateRepository.countFeaturedRealEstatesByUser(userId);
		return currentFeatured < stats.getLimitations().getMaxFeaturedListings();
	}

	// Get detailed usage information
	public Map<String, Object> getDetailedUsage(Long userId) {
		UsageStatsDTO stats = limitationService.getUsageStats(userId);
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		Map<String, Object> usage = new HashMap<>();
		usage.put("userTier", user.getTier());
		usage.put("currentListings", stats.getCurrentListings());
		usage.put("maxListings", stats.getLimitations().getMaxListings());
		usage.put("currentImages", stats.getCurrentImages());
		usage.put("maxImages", stats.getLimitations().getMaxImages());
		usage.put("canCreateListing", stats.isCanCreateListing());
		usage.put("canUploadImage", stats.isCanUploadImage());
		usage.put("canFeatureListing", canFeatureListing(userId));
		usage.put("currentFeatured", realEstateRepository.countFeaturedRealEstatesByUser(userId));
		usage.put("maxFeatured", stats.getLimitations().getMaxFeaturedListings());

		return usage;
	}

	// Throw exception if limit exceeded (for service layer)
	public void validateCanCreateRealEstate(Long userId) {
		if (!canCreateRealEstate(userId)) {
			User user = userRepository.findById(userId).orElseThrow();
			UserLimitation limits = limitationService.getLimitationsForUser(user);
			throw new LimitationExceededException("Real estate limit exceeded",
					userRepository.countActiveRealEstatesByUser(userId), limits.getMaxListings().longValue());
		}
	}

	public void validateCanUploadImages(Long userId, int imageCount) {
		if (!canUploadImage(userId, imageCount)) {
			User user = userRepository.findById(userId).orElseThrow();
			UserLimitation limits = limitationService.getLimitationsForUser(user);
			Long currentImages = userRepository.countImagesByUser(userId);
			throw new LimitationExceededException("Image upload limit exceeded", currentImages + imageCount,
					limits.getMaxImages().longValue());
		}
	}

	public void validateCanFeatureListing(Long userId) {
		if (!canFeatureListing(userId)) {
			User user = userRepository.findById(userId).orElseThrow();
			UserLimitation limits = limitationService.getLimitationsForUser(user);
			Long currentFeatured = realEstateRepository.countFeaturedRealEstatesByUser(userId);
			throw new LimitationExceededException("Featured listing limit exceeded", currentFeatured,
					limits.getMaxFeaturedListings().longValue());
		}
	}

}
