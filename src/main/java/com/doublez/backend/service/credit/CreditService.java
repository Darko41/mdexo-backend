package com.doublez.backend.service.credit;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.credit.CreditBalanceDTO;
import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.entity.credit.UserCredit;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.repository.credit.CreditTransactionRepository;
import com.doublez.backend.repository.credit.UserCreditRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreditService {

    private final UserCreditRepository userCreditRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    public CreditService(UserCreditRepository userCreditRepository,
                        CreditTransactionRepository creditTransactionRepository,
                        UserRepository userRepository) {
        this.userCreditRepository = userCreditRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if user has sufficient credits
     */
    public boolean hasSufficientCredits(Long userId, Integer requiredCredits) {
        UserCredit userCredit = userCreditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User credit not found for user: " + userId));
        
        return userCredit.hasSufficientCredits(requiredCredits);
    }

    /**
     * Deduct credits from user balance
     */
    public boolean deductCredits(Long userId, Integer credits, String description) {
        UserCredit userCredit = userCreditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User credit not found for user: " + userId));
        
        boolean success = userCredit.deductCredits(credits, description);
        if (success) {
            userCreditRepository.save(userCredit);
            
            // Log transaction
            CreditTransaction transaction = new CreditTransaction(
                userCredit.getUser(), -credits, CreditTransactionType.PURCHASE, description
            );
            transaction.setBalanceAfter(userCredit.getCurrentBalance()); 
            transaction.completeTransaction();
            creditTransactionRepository.save(transaction);
        }
        
        return success;
    }

    /**
     * Add credits to user balance
     */
    public void addCredits(Long userId, Integer credits, String description, CreditTransactionType transactionType) {
        UserCredit userCredit = userCreditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User credit not found for user: " + userId));
        
        userCredit.addCredits(credits, description);
        userCreditRepository.save(userCredit);
        
        // Log transaction
        CreditTransaction transaction = new CreditTransaction(
            userCredit.getUser(), credits, transactionType, description
        );
        transaction.setBalanceAfter(userCredit.getCurrentBalance()); 
        transaction.completeTransaction();
        creditTransactionRepository.save(transaction);
    }

    /**
     * Get current credit balance
     */
    public Integer getCurrentBalance(Long userId) {
        UserCredit userCredit = userCreditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User credit not found for user: " + userId));
        
        return userCredit.getCurrentBalance();
    }

    /**
     * Get credit balance DTO
     */
    public CreditBalanceDTO getCreditBalance(Long userId) {
        UserCredit userCredit = userCreditRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("User credit not found for user: " + userId));
        
        return userCredit.toBalanceDTO();
    }
}