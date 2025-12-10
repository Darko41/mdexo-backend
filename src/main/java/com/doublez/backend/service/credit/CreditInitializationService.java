package com.doublez.backend.service.credit;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.entity.credit.UserCredit;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserRole;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.repository.credit.CreditPackageRepository;
import com.doublez.backend.repository.credit.CreditTransactionRepository;
import com.doublez.backend.repository.credit.UserCreditRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreditInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(CreditInitializationService.class);
    
    private final UserCreditRepository userCreditRepository;
    private final CreditPackageRepository creditPackageRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public CreditInitializationService(UserCreditRepository userCreditRepository,
                                     CreditPackageRepository creditPackageRepository,
                                     CreditTransactionRepository creditTransactionRepository) {
        this.userCreditRepository = userCreditRepository;
        this.creditPackageRepository = creditPackageRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    // INITIALIZE CREDITS FOR NEW USER
    public void initializeUserCredits(User user) {
        // Check if user already has credit record (shouldn't happen, but safety check)
        if (userCreditRepository.findByUserId(user.getId()).isPresent()) {
            logger.warn("User {} already has credit record, skipping initialization", user.getEmail());
            return;
        }
        
        UserCredit userCredit = new UserCredit(user);
        
        // Welcome bonus for all new users
        giveWelcomeBonus(user, userCredit);
        
        // Trial credits for agency users
        if (user.isAgencyAdmin()) {
            giveTrialCredits(user, userCredit);
        }
        
        userCreditRepository.save(userCredit);
        logger.info("Credit initialization completed for user: {}", user.getEmail());
    }

    private void giveWelcomeBonus(User user, UserCredit userCredit) {
        int welcomeCredits = 100; // Free credits for all new users
        userCredit.addCredits(welcomeCredits, "Welcome bonus");
        
        // Log transaction
        CreditTransaction transaction = new CreditTransaction(
            user, welcomeCredits, CreditTransactionType.BONUS, "Welcome bonus for new registration"
        );
        transaction.completeTransaction(); // Mark as completed immediately
        creditTransactionRepository.save(transaction);
        
        logger.info("Added {} welcome credits to user: {}", welcomeCredits, user.getEmail());
    }

    private void giveTrialCredits(User user, UserCredit userCredit) {
        int trialCredits = 500; // Extra credits for agency trial
        userCredit.addCredits(trialCredits, "Trial period credits");
        
        // Log transaction
        CreditTransaction transaction = new CreditTransaction(
            user, trialCredits, CreditTransactionType.BONUS, "Trial period credits"
        );
        transaction.completeTransaction(); // Mark as completed immediately
        creditTransactionRepository.save(transaction);
        
        logger.info("Added {} trial credits to agency user: {}", trialCredits, user.getEmail());
    }

    // 🆕 GET WELCOME PACKAGE INFO
    public Map<String, Object> getWelcomePackage(UserRole role) {
        Map<String, Object> welcomePackage = new HashMap<>();
        welcomePackage.put("welcomeCredits", 100);
        
        if (role == UserRole.AGENCY) {
            welcomePackage.put("trialCredits", 500);
            welcomePackage.put("totalCredits", 600);
        } else {
            welcomePackage.put("totalCredits", 100);
        }
        
        welcomePackage.put("validUntil", LocalDateTime.now().plusMonths(1));
        return welcomePackage;
    }

    // 🆕 CHECK IF USER HAS CREDITS INITIALIZED
    public boolean hasCreditsInitialized(Long userId) {
        return userCreditRepository.findByUserId(userId).isPresent();
    }

    // 🆕 REINITIALIZE CREDITS (admin function)
    public void reinitializeUserCredits(User user) {
        // Delete existing credit record if exists
        userCreditRepository.findByUserId(user.getId()).ifPresent(userCreditRepository::delete);
        
        // Delete existing bonus transactions
        List<CreditTransaction> bonusTransactions = creditTransactionRepository
            .findByUserIdAndTransactionTypeOrderByCreatedAtDesc(user.getId(), CreditTransactionType.BONUS);
        creditTransactionRepository.deleteAll(bonusTransactions);
        
        // Initialize fresh
        initializeUserCredits(user);
    }
}