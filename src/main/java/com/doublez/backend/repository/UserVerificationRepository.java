package com.doublez.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.user.UserVerification;
import com.doublez.backend.enums.DocumentType;
import com.doublez.backend.enums.VerificationStatus;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    
    List<UserVerification> findByUserId(Long userId);
    
    List<UserVerification> findByStatus(VerificationStatus status);
    
    List<UserVerification> findByStatusIn(List<VerificationStatus> statuses);
    
    Optional<UserVerification> findByUserIdAndDocumentType(Long userId, DocumentType documentType);
    
    @Query("SELECT uv FROM UserVerification uv WHERE uv.licenseExpiry < CURRENT_DATE AND uv.status = 'APPROVED'")
    List<UserVerification> findExpiredLicenses();
    
    @Query("SELECT COUNT(uv) FROM UserVerification uv WHERE uv.user.id = :userId AND uv.status = 'APPROVED'")
    Long countApprovedVerificationsByUser(@Param("userId") Long userId);
    
    List<UserVerification> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
