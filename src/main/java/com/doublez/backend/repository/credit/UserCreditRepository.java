package com.doublez.backend.repository.credit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.credit.UserCredit;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {
    Optional<UserCredit> findByUserId(Long userId);
    
    @Query("SELECT uc FROM UserCredit uc WHERE uc.user.id = :userId")
    Optional<UserCredit> findUserCreditByUserId(@Param("userId") Long userId);
    
    @Query("SELECT uc FROM UserCredit uc WHERE uc.currentBalance > :minBalance")
    List<UserCredit> findUsersWithBalanceAbove(@Param("minBalance") Integer minBalance);
}
