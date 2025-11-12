package com.doublez.backend.service.usage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.email.EmailService;
import com.doublez.backend.service.user.LimitationService;

import jakarta.transaction.Transactional;

//service/TrialService.java
@Service
@Transactional
public class TrialService {

	private static final Logger logger = LoggerFactory.getLogger(TrialService.class);

	private final UserRepository userRepository;
	private final LimitationService limitationService;
	private final EmailService emailService;
	private final int TRIAL_MONTHS = 6;

	public TrialService(UserRepository userRepository, LimitationService limitationService, EmailService emailService) {
		this.userRepository = userRepository;
		this.limitationService = limitationService;
		this.emailService = emailService;
	}

	// A method to get effective limitations considering trial status
	public UserLimitation getEffectiveLimitations(User user) {
		UserLimitation baseLimitations = limitationService.getLimitationsForUser(user);

		// Apply trial-specific enhancements
		if (isInTrial(user)) {
			// Create enhanced limitations for trial users
			return createEnhancedTrialLimitations(baseLimitations, user);
		}

		return baseLimitations;
	}

	private UserLimitation createEnhancedTrialLimitations(UserLimitation baseLimits, User user) {
		UserLimitation enhancedLimits = new UserLimitation();

		// Copy all base properties
		enhancedLimits.setId(baseLimits.getId());
		enhancedLimits.setTier(baseLimits.getTier());
		enhancedLimits.setPricePerMonth(baseLimits.getPricePerMonth());

		// 🎁 ENHANCE LIMITS FOR TRIAL USERS
		// Give trial users better limits to showcase premium features
		enhancedLimits.setMaxListings(baseLimits.getMaxListings() + 5); // Extra listings
		enhancedLimits.setMaxImages(baseLimits.getMaxImages() + 20); // Extra images
		enhancedLimits.setMaxImagesPerListing(baseLimits.getMaxImagesPerListing() + 3); // More images per listing
		enhancedLimits.setCanFeatureListings(true); // Always allow featuring during trial
		enhancedLimits.setMaxFeaturedListings(Math.max(3, baseLimits.getMaxFeaturedListings())); // At least 3 featured

		return enhancedLimits;
	}

	// Start trial for new users
	public void startTrial(User user) {
		logger.info("Starting {} month trial for user: {}", TRIAL_MONTHS, user.getEmail());

		user.setTrialStartDate(LocalDate.now());
		user.setTrialEndDate(LocalDate.now().plusMonths(TRIAL_MONTHS));
		user.setTrialUsed(true);

		// Set appropriate free tier based on user type
		if (user.isAgent()) {
			user.setTier(UserTier.FREE_AGENT);
		} else if (user.isInvestor()) {
			user.setTier(UserTier.FREE_INVESTOR);
		} else {
			user.setTier(UserTier.FREE_USER);
		}

		userRepository.save(user);

		// Send welcome email with trial information
		try {
			emailService.sendTrialStartedEmail(user.getEmail(), getUserDisplayName(user), TRIAL_MONTHS);
		} catch (Exception e) {
			logger.warn("Failed to send trial started email: {}", e.getMessage());
		}

		logger.info("Trial started for user: {} until {}", user.getEmail(), user.getTrialEndDate());
	}

	// Check if user is in trial period
	public boolean isInTrial(User user) {
		return user.isInTrialPeriod();
	}

	// Check if user has trial expired
	public boolean isTrialExpired(User user) {
		return user.isTrialExpired();
	}

	// Get days remaining in trial
	public long getTrialDaysRemaining(User user) {
		return user.getTrialDaysRemaining();
	}

	// Get trial progress percentage (0-100)
	public int getTrialProgressPercentage(User user) {
		if (!user.getTrialUsed() || user.getTrialStartDate() == null || user.getTrialEndDate() == null) {
			return 0;
		}

		long totalDays = ChronoUnit.DAYS.between(user.getTrialStartDate(), user.getTrialEndDate());
		long daysPassed = ChronoUnit.DAYS.between(user.getTrialStartDate(), LocalDate.now());

		if (totalDays <= 0)
			return 100;

		int percentage = (int) ((daysPassed * 100) / totalDays);
		return Math.min(Math.max(percentage, 0), 100);
	}

	// Force expire trial (for testing or admin actions)
	public void expireTrial(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		user.setTrialEndDate(LocalDate.now().minusDays(1)); // Set to yesterday
		userRepository.save(user);

		handleTrialExpiration(user);
		logger.info("Trial manually expired for user: {}", user.getEmail());
	}

	// Extend trial (admin function)
	public void extendTrial(Long userId, int additionalMonths) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

		if (user.getTrialEndDate() == null) {
			user.setTrialEndDate(LocalDate.now().plusMonths(additionalMonths));
		} else {
			user.setTrialEndDate(user.getTrialEndDate().plusMonths(additionalMonths));
		}

		userRepository.save(user);

		try {
			emailService.sendTrialExtendedEmail(user.getEmail(), getUserDisplayName(user), additionalMonths);
		} catch (Exception e) {
			logger.warn("Failed to send trial extended email: {}", e.getMessage());
		}

		logger.info("Trial extended for user: {} by {} months", user.getEmail(), additionalMonths);
	}

	// 🟢 SCHEDULED TASKS

	// Check for expiring trials (run daily at 8 AM)
	@Scheduled(cron = "0 0 8 * * ?")
	public void checkExpiringTrials() {
		logger.info("Starting daily trial expiration check...");

		LocalDate warningDate = LocalDate.now().plusDays(7); // Warn 7 days before
		LocalDate tomorrow = LocalDate.now().plusDays(1); // Warn 1 day before

		// Find users whose trials expire soon
		List<User> expiringIn7Days = userRepository.findByTrialEndDate(warningDate);
		List<User> expiringTomorrow = userRepository.findByTrialEndDate(tomorrow);
		List<User> expiringToday = userRepository.findByTrialEndDate(LocalDate.now());

		// Send appropriate warnings
		for (User user : expiringIn7Days) {
			if (isInTrial(user)) {
				sendTrialExpirationWarning(user, 7);
			}
		}

		for (User user : expiringTomorrow) {
			if (isInTrial(user)) {
				sendTrialExpirationWarning(user, 1);
			}
		}

		for (User user : expiringToday) {
			if (isInTrial(user)) {
				sendTrialExpirationWarning(user, 0);
			}
		}

		// Handle expired trials
		List<User> expired = userRepository.findByTrialEndDateBefore(LocalDate.now());
		for (User user : expired) {
			if (isTrialExpired(user)) {
				handleTrialExpiration(user);
			}
		}

		logger.info("Trial check completed: {} expiring soon, {} expired",
				expiringIn7Days.size() + expiringTomorrow.size() + expiringToday.size(), expired.size());
	}

	// 🟢 PRIVATE HELPER METHODS

	private void sendTrialExpirationWarning(User user, int daysRemaining) {
		try {
			emailService.sendTrialExpiringEmail(user.getEmail(), getUserDisplayName(user), daysRemaining);
			logger.info("Trial expiration warning sent to user: {} ({} days remaining)", user.getEmail(),
					daysRemaining);
		} catch (Exception e) {
			logger.warn("Failed to send trial expiration email to {}: {}", user.getEmail(), e.getMessage());
		}
	}

	private void handleTrialExpiration(User user) {
		logger.info("Handling trial expiration for user: {}", user.getEmail());

		// Determine appropriate basic tier based on user's roles
		UserTier newTier;
		if (user.isAgent()) {
			newTier = UserTier.BASIC_AGENT;
		} else if (user.isInvestor()) {
			newTier = UserTier.BASIC_INVESTOR;
		} else {
			newTier = UserTier.BASIC_USER;
		}

		// Update user tier
		user.setTier(newTier);
		userRepository.save(user);

		// Send expiration notification
		try {
			emailService.sendTrialExpiredEmail(user.getEmail(), getUserDisplayName(user), newTier);
		} catch (Exception e) {
			logger.warn("Failed to send trial expired email: {}", e.getMessage());
		}

		logger.info("User {} downgraded to {} after trial expiration", user.getEmail(), newTier);
	}

	private String getUserDisplayName(User user) {
		if (user.getUserProfile() != null && user.getUserProfile().getFirstName() != null) {
			return user.getUserProfile().getFirstName() + " "
					+ (user.getUserProfile().getLastName() != null ? user.getUserProfile().getLastName() : "");
		}
		return user.getEmail();
	}

	// 🟢 STATISTICS METHODS

	public Map<String, Object> getTrialStatistics() {
		List<User> allUsers = userRepository.findAll();

		long totalUsers = allUsers.size();
		long inTrial = allUsers.stream().filter(this::isInTrial).count();
		long trialExpired = allUsers.stream().filter(this::isTrialExpired).count();
		long neverUsedTrial = allUsers.stream().filter(u -> !u.getTrialUsed()).count();

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalUsers", totalUsers);
		stats.put("usersInTrial", inTrial);
		stats.put("trialExpired", trialExpired);
		stats.put("neverUsedTrial", neverUsedTrial);
		stats.put("trialUtilizationRate", totalUsers > 0 ? (inTrial * 100.0 / totalUsers) : 0);

		return stats;
	}
}
