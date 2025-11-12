package com.doublez.backend.service.realestate;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.RealEstateRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.LimitationService;

import jakarta.transaction.Transactional;

//service/FeaturedListingService.java
@Service
@Transactional
public class FeaturedListingService {

	private final RealEstateRepository realEstateRepository;
	private final LimitationService limitationService;
	private final UserRepository userRepository;

	public FeaturedListingService(RealEstateRepository realEstateRepository, LimitationService limitationService,
			UserRepository userRepository) {
		this.realEstateRepository = realEstateRepository;
		this.limitationService = limitationService;
		this.userRepository = userRepository;
	}

	public boolean canFeatureRealEstate(Long userId, Long realEstateId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		UserLimitation limitations = limitationService.getLimitationsForUser(user);

		// Check if user tier allows featuring
		if (!limitations.getCanFeatureListings()) {
			return false;
		}

		// Check if user hasn't reached the featured limit
		Long currentFeaturedCount = realEstateRepository.countFeaturedRealEstatesByUser(userId);
		return currentFeaturedCount < limitations.getMaxFeaturedListings();
	}

	public RealEstate featureRealEstate(Long userId, Long realEstateId, Integer featuredDays) {
		// Check if user can feature more listings
		if (!canFeatureRealEstate(userId, realEstateId)) {
			User user = userRepository.findById(userId).orElseThrow();
			UserLimitation limitations = limitationService.getLimitationsForUser(user);
			Long currentFeaturedCount = realEstateRepository.countFeaturedRealEstatesByUser(userId);

			throw new LimitationExceededException(
					String.format("Featured listing limit reached. You have %d featured listings out of %d allowed.",
							currentFeaturedCount, limitations.getMaxFeaturedListings()));
		}

		RealEstate realEstate = realEstateRepository.findById(realEstateId)
				.orElseThrow(() -> new RuntimeException("Real estate not found"));

		// Verify ownership
		if (!realEstate.getOwner().getId().equals(userId)) {
			throw new RuntimeException("User does not own this real estate");
		}

		// Set as featured
		realEstate.setFeatured(true, featuredDays);

		return realEstateRepository.save(realEstate);
	}

	public RealEstate unfeatureRealEstate(Long userId, Long realEstateId) {
		RealEstate realEstate = realEstateRepository.findById(realEstateId)
				.orElseThrow(() -> new RuntimeException("Real estate not found"));

		// Verify ownership
		if (!realEstate.getOwner().getId().equals(userId)) {
			throw new RuntimeException("User does not own this real estate");
		}

		realEstate.setFeatured(false, null);

		return realEstateRepository.save(realEstate);
	}

	// Scheduled method to automatically unfeature expired listings
	@Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
	public void unfeatureExpiredListings() {
		List<RealEstate> expiredFeatured = realEstateRepository.findExpiredFeaturedRealEstates();

		for (RealEstate realEstate : expiredFeatured) {
			realEstate.setFeatured(false, null);
			realEstateRepository.save(realEstate);
		}

		if (!expiredFeatured.isEmpty()) {
			System.out.println("Unfeatured " + expiredFeatured.size() + " expired featured listings");
		}
	}

	public List<RealEstate> getActiveFeaturedListings(int limit) {
		return realEstateRepository.findActiveFeaturedRealEstates(PageRequest.of(0, limit));
	}
}