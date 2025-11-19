package com.doublez.backend.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.doublez.backend.entity.user.UserLimitation;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.repository.UserLimitationRepository;

@Component
public class DataInitializer {

	private final UserLimitationRepository limitationRepository;
	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	public DataInitializer(UserLimitationRepository limitationRepository) {
		this.limitationRepository = limitationRepository;
	}

	@EventListener(ContextRefreshedEvent.class)
	public void init() {
		initializeDefaultLimitations();
	}

	private void initializeDefaultLimitations() {
		if (limitationRepository.count() == 0) {
			logger.info("Initializing default user limitations...");

			List<UserLimitation> defaultLimits = Arrays.asList(
					// FREE_USER: 3 real estates, 20 images total, 5 images per real estate
					new UserLimitation(UserTier.FREE_USER, 3, 20, 5, false, 0, BigDecimal.ZERO),

					// BASIC_USER: 5 real estates, 30 images total, 8 images per real estate
					new UserLimitation(UserTier.BASIC_USER, 5, 30, 8, true, 1, BigDecimal.valueOf(390)),

					// PREMIUM_USER: 15 real estates, 100 images total, 15 images per real estate
					new UserLimitation(UserTier.PREMIUM_USER, 15, 100, 15, true, 5, BigDecimal.valueOf(990)),

					// AGENCY_BASIC: 50 real estates, 500 images total, 25 images per real estate
					new UserLimitation(UserTier.AGENCY_BASIC, 50, 500, 25, true, 15, BigDecimal.valueOf(2490)),

					// AGENCY_PREMIUM: 200 real estates, 2000 images total, 50 images per real
					// estate
					new UserLimitation(UserTier.AGENCY_PREMIUM, 200, 2000, 50, true, 50, BigDecimal.valueOf(4990)),
					// 🆕 INVESTOR TIERS
		            // FREE_INVESTOR: 5 real estates, 50 images total, 10 images per real estate
		            new UserLimitation(UserTier.FREE_INVESTOR, 5, 50, 10, true, 3, BigDecimal.ZERO),
		            
		            // BASIC_INVESTOR: 20 real estates, 200 images total, 15 images per real estate
		            new UserLimitation(UserTier.BASIC_INVESTOR, 20, 200, 15, true, 10, BigDecimal.valueOf(1990)),
		            
		            // PREMIUM_INVESTOR: 50 real estates, 500 images total, 20 images per real estate
		            new UserLimitation(UserTier.PREMIUM_INVESTOR, 50, 500, 20, true, 25, BigDecimal.valueOf(3990)),

					// ADMIN: unlimited
					new UserLimitation(UserTier.ADMIN, 10000, 50000, 100, true, 1000, BigDecimal.ZERO));

			limitationRepository.saveAll(defaultLimits);
			logger.info("✅ Default user limitations initialized successfully");
		} else {
			logger.info("✅ User limitations already initialized");
		}
	}
}
