package com.doublez.backend.service.credit;

import org.springframework.stereotype.Service;

import com.doublez.backend.dto.credit.CreditBalanceDTO;
import com.doublez.backend.dto.credit.CreditTransactionCreateDTO;
import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.credit.CreditPackage;
import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.entity.credit.UserCredit;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.repository.credit.CreditPackageRepository;
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
    private final CreditPackageRepository creditPackageRepository;

    public CreditService(UserCreditRepository userCreditRepository,
                        CreditTransactionRepository creditTransactionRepository,
                        UserRepository userRepository,
                        CreditPackageRepository creditPackageRepository) {
        this.userCreditRepository = userCreditRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.userRepository = userRepository;
        this.creditPackageRepository = creditPackageRepository;
    }
    
    /**
     * Create credit transaction (NEW METHOD NEEDED BY TeamCreditService)
     */
    public CreditTransactionResponseDTO createCreditTransaction(CreditTransactionCreateDTO transactionDTO) {
        // Create final copy of user for use in lambda
        final User transactionUser;
        Agency agency = null;
        
        // Find user if userId is provided
        if (transactionDTO.getUserId() != null) {
            transactionUser = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + transactionDTO.getUserId()));
        } else {
            transactionUser = null;
        }
        
        // Find credit package
        CreditPackage creditPackage = creditPackageRepository.findById(transactionDTO.getCreditPackageId())
            .orElseThrow(() -> new EntityNotFoundException("Credit package not found: " + transactionDTO.getCreditPackageId()));
        
        // Create transaction
        CreditTransaction transaction = new CreditTransaction();
        transaction.setUser(transactionUser);
        transaction.setCreditPackage(creditPackage);
        transaction.setCreditChange(transactionDTO.getCreditChange());
        transaction.setTransactionType(transactionDTO.getTransactionType());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setReferenceNumber(transactionDTO.getReferenceNumber());
        transaction.setMetadata(transactionDTO.getMetadata());
        
        // If agency purchase, handle agency credit
        if (transactionDTO.getAgencyId() != null) {
            // This would need agency service injection
            // For now, just store in metadata
            transaction.setMetadata("agency_id:" + transactionDTO.getAgencyId());
        }
        
        // Update user credit if needed
        if (transactionUser != null && transactionDTO.getCreditChange() != null && transactionDTO.getCreditChange() > 0) {
            UserCredit userCredit = userCreditRepository.findByUserId(transactionUser.getId())
                .orElseGet(() -> new UserCredit(transactionUser));
            
            userCredit.addCredits(transactionDTO.getCreditChange(), transactionDTO.getDescription());
            userCreditRepository.save(userCredit);
            
            transaction.setBalanceAfter(userCredit.getCurrentBalance());
        }
        
        transaction.completeTransaction();
        creditTransactionRepository.save(transaction);
        
        // Convert to response DTO
        return mapToResponseDTO(transaction);
    }
    
    /**
     * Add credits to user (overloaded version without transactionType)
     */
    public void addCreditsToUser(Long userId, Integer credits, String description) {
        addCredits(userId, credits, description, CreditTransactionType.BONUS);
    }
    
    /**
     * Add credits to user (existing method)
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
     * Map transaction to response DTO
     */
    private CreditTransactionResponseDTO mapToResponseDTO(CreditTransaction transaction) {
        CreditTransactionResponseDTO dto = new CreditTransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser() != null ? transaction.getUser().getId() : null);
        dto.setCreditChange(transaction.getCreditChange());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setReferenceNumber(transaction.getReferenceNumber());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        
        if (transaction.getCreditPackage() != null) {
            dto.setCreditPackageId(transaction.getCreditPackage().getId());
            dto.setCreditPackageName(transaction.getCreditPackage().getName());
        }
        
        return dto;
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