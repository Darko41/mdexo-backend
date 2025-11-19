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

import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.UserTier;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.repository.UserLimitationRepository;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.email.ResendEmailService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TrialService {

    private static final Logger logger = LoggerFactory.getLogger(TrialService.class);
    
    private final UserRepository userRepository;
    private final UserLimitationRepository limitationRepository; // only if you need to compute effective limits (optional)
    private final ResendEmailService resendEmailService;
    private final int TRIAL_MONTHS = 6;

    public TrialService(UserRepository userRepository,
                        UserLimitationRepository limitationRepository,
                        ResendEmailService resendEmailService) {
        this.userRepository = userRepository;
        this.limitationRepository = limitationRepository;
        this.resendEmailService = resendEmailService;
    }

    public void startTrial(User user) {
        logger.info("Starting {} month trial for user: {}", TRIAL_MONTHS, user.getEmail());
        user.setTrialStartDate(LocalDate.now());
        user.setTrialEndDate(LocalDate.now().plusMonths(TRIAL_MONTHS));
        user.setTrialUsed(true);

        // Set tier for trial participants:
        // Regular registrants -> FREE_USER
        // Agency account creators (ROLE_AGENCY_ADMIN) -> AGENCY_BASIC during trial period
        if (user.isAgencyAdmin()) {
            user.setTier(UserTier.AGENCY_BASIC);
        } else if (user.isInvestor()) {
            user.setTier(UserTier.FREE_INVESTOR);
        } else {
            user.setTier(UserTier.FREE_USER);
        }

        userRepository.save(user);

        try {
            resendEmailService.sendTrialStartedEmail(user.getEmail(), getUserDisplayName(user), TRIAL_MONTHS);
        } catch (Exception e) {
        	logger.warn("Failed to send trial started email: {}", e.getMessage());
        }

        logger.info("Trial started for user {} until {}", user.getEmail(), user.getTrialEndDate());
    }

    public boolean isInTrial(User user) { return user.isInTrialPeriod(); }
    public boolean isTrialExpired(User user) { return user.isTrialExpired(); }
    public long getTrialDaysRemaining(User user) { return user.getTrialDaysRemaining(); }

    public int getTrialProgressPercentage(User user) {
        if (!user.getTrialUsed() || user.getTrialStartDate() == null || user.getTrialEndDate() == null) {
            return 0;
        }
        long totalDays = ChronoUnit.DAYS.between(user.getTrialStartDate(), user.getTrialEndDate());
        long daysPassed = ChronoUnit.DAYS.between(user.getTrialStartDate(), LocalDate.now());
        if (totalDays <= 0) return 100;
        int percentage = (int) ((daysPassed * 100) / totalDays);
        return Math.min(Math.max(percentage, 0), 100);
    }

    public void expireTrial(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setTrialEndDate(LocalDate.now().minusDays(1));
        userRepository.save(user);
        handleTrialExpiration(user);
    }

    public void extendTrial(Long userId, int additionalMonths) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getTrialEndDate() == null) {
            user.setTrialEndDate(LocalDate.now().plusMonths(additionalMonths));
        } else {
            user.setTrialEndDate(user.getTrialEndDate().plusMonths(additionalMonths));
        }
        userRepository.save(user);
        try {
            resendEmailService.sendTrialExtendedEmail(user.getEmail(), getUserDisplayName(user), additionalMonths);
        } catch (Exception e) {
            logger.warn("Failed to send trial extended email: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void checkExpiringTrials() {
        LocalDate warning7 = LocalDate.now().plusDays(7);
        LocalDate warning1 = LocalDate.now().plusDays(1);
        
        List<User> exp7 = userRepository.findByTrialEndDate(warning7);
        List<User> exp1 = userRepository.findByTrialEndDate(warning1);
        List<User> exp0 = userRepository.findByTrialEndDate(LocalDate.now());
        
        exp7.stream().filter(this::isInTrial).forEach(u -> sendTrialExpirationWarning(u, 7));
        exp1.stream().filter(this::isInTrial).forEach(u -> sendTrialExpirationWarning(u, 1));
        exp0.stream().filter(this::isInTrial).forEach(u -> sendTrialExpirationWarning(u, 0));
        
        List<User> expired = userRepository.findByTrialEndDateBefore(LocalDate.now());
        expired.stream().filter(this::isTrialExpired).forEach(user -> {
            if (user.isAgencyAdmin() && !user.getOwnedAgencies().isEmpty()) {
                Agency agency = user.getOwnedAgencies().get(0);
                handleAgencyTrialExpiration(user, agency);
            } else {
                handleTrialExpiration(user);
            }
        });
    }

    private void sendTrialExpirationWarning(User user, int daysRemaining) {
        try {
            resendEmailService.sendTrialExpiringEmail(user.getEmail(), getUserDisplayName(user), daysRemaining);
        } catch (Exception e) {
            logger.warn("Failed to send expiration warning to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void handleTrialExpiration(User user) {
        // Decide new tier based on account type
        UserTier newTier;
        if (user.isAgencyAdmin()) {
            newTier = UserTier.AGENCY_BASIC; // keep them on basic agency tier after trial
        } else if (user.isInvestor()) {
            newTier = UserTier.BASIC_INVESTOR;
        } else {
            newTier = UserTier.BASIC_USER;
        }
        user.setTier(newTier);
        userRepository.save(user);
        
        try {
            resendEmailService.sendTrialExpiredEmail(user.getEmail(), getUserDisplayName(user), newTier);
        } catch (Exception e) {
            logger.warn("Failed to send trial expired email: {}", e.getMessage());
        }
    }

    private String getUserDisplayName(User user) {
        if (user.getUserProfile() != null && user.getUserProfile().getFirstName() != null) {
            String last = user.getUserProfile().getLastName() != null ? " " + user.getUserProfile().getLastName() : "";
            return user.getUserProfile().getFirstName() + last;
        }
        return user.getEmail();
    }

    // stats method (same as earlier)
    public Map<String,Object> getTrialStatistics() {
        List<User> all = userRepository.findAll();
        long total = all.size();
        long inTrial = all.stream().filter(this::isInTrial).count();
        long expired = all.stream().filter(this::isTrialExpired).count();
        long never = all.stream().filter(u -> !u.getTrialUsed()).count();
        Map<String,Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("usersInTrial", inTrial);
        stats.put("trialExpired", expired);
        stats.put("neverUsedTrial", never);
        stats.put("trialUtilizationRate", total > 0 ? (inTrial * 100.0 / total) : 0);
        return stats;
    }
    
    // Agency trial logic
    public void startAgencyTrial(User user, Agency agency) {
        logger.info("Starting {} month agency trial for agency: {}", TRIAL_MONTHS, agency.getName());
        
        user.setTrialStartDate(LocalDate.now());
        user.setTrialEndDate(LocalDate.now().plusMonths(TRIAL_MONTHS));
        user.setTrialUsed(true);
        user.setTier(UserTier.AGENCY_BASIC); // Start with agency basic during trial

        userRepository.save(user);

        try {
            // Use existing email method with agency context
            resendEmailService.sendTrialStartedEmail(
                user.getEmail(), 
                getUserDisplayName(user),
                TRIAL_MONTHS
            );
        } catch (Exception e) {
            logger.warn("Failed to send agency trial started email: {}", e.getMessage());
        }

        logger.info("Agency trial started for {} (agency: {}) until {}", 
            user.getEmail(), agency.getName(), user.getTrialEndDate());
    }
    
    // Check if agency is in trial
    public boolean isAgencyInTrial(Agency agency) {
        User admin = agency.getAdmin();
        return isInTrial(admin) && (admin.getTier() == UserTier.AGENCY_BASIC || admin.getTier() == UserTier.AGENCY_PREMIUM);
    }

    // Agency trial expiration handler
    private void handleAgencyTrialExpiration(User user, Agency agency) {
        // After trial, keep them on AGENCY_BASIC but they'll need to pay
        user.setTier(UserTier.AGENCY_BASIC);
        userRepository.save(user);
        
        try {
            // Use existing email method with agency context
            resendEmailService.sendTrialExpiredEmail(
                user.getEmail(),
                getUserDisplayName(user),
                UserTier.AGENCY_BASIC
            );
        } catch (Exception e) {
            logger.warn("Failed to send agency trial expired email: {}", e.getMessage());
        }
    }
    
    // Enhanced trial statistics
    public Map<String, Object> getEnhancedTrialStatistics() {
        List<User> allUsers = userRepository.findAll();
        long total = allUsers.size();
        long inTrial = allUsers.stream().filter(this::isInTrial).count();
        long expired = allUsers.stream().filter(this::isTrialExpired).count();
        long never = allUsers.stream().filter(u -> !u.getTrialUsed()).count();
        
        // 🆕 Agency-specific stats
        long agencyUsers = allUsers.stream().filter(User::isAgencyAdmin).count();
        long agencyInTrial = allUsers.stream()
                .filter(User::isAgencyAdmin)
                .filter(this::isInTrial)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("usersInTrial", inTrial);
        stats.put("trialExpired", expired);
        stats.put("neverUsedTrial", never);
        stats.put("trialUtilizationRate", total > 0 ? (inTrial * 100.0 / total) : 0);
        
        // 🆕 Agency stats
        stats.put("agencyUsers", agencyUsers);
        stats.put("agencyUsersInTrial", agencyInTrial);
        stats.put("agencyTrialRate", agencyUsers > 0 ? (agencyInTrial * 100.0 / agencyUsers) : 0);
        
        return stats;
    }
}
