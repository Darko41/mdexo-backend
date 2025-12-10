package com.doublez.backend.repository.credit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.credit.CreditTransaction;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.enums.PaymentStatus;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<CreditTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(Long userId, CreditTransactionType transactionType);
    
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.user.id = :userId AND ct.createdAt BETWEEN :startDate AND :endDate")
    List<CreditTransaction> findUserTransactionsInPeriod(@Param("userId") Long userId, 
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ct.creditChange) FROM CreditTransaction ct WHERE ct.user.id = :userId AND ct.transactionType = :transactionType")
    Optional<Integer> sumCreditsByUserAndType(@Param("userId") Long userId, 
                                             @Param("transactionType") CreditTransactionType transactionType);
    
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.paymentStatus = :status")
    List<CreditTransaction> findByPaymentStatus(@Param("status") PaymentStatus status);
    
    List<CreditTransaction> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT ct FROM CreditTransaction ct WHERE ct.user.id = :userId ORDER BY ct.createdAt DESC")
    Page<CreditTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}