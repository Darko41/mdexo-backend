package com.doublez.backend.entity.credit;

import java.time.LocalDateTime;

import com.doublez.backend.dto.credit.CreditTransactionResponseDTO;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.CreditTransactionType;
import com.doublez.backend.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "credit_change", nullable = false)
    private Integer creditChange; // Positive for additions, negative for deductions

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private CreditTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reference_number")
    private String referenceNumber; // Bank reference or payment ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_package_id")
    private CreditPackage creditPackage;

    @Column(name = "metadata")
    private String metadata; // JSON for additional data

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "admin_notes")
    private String adminNotes;

    // ========================
    // CONSTRUCTORS
    // ========================

    public CreditTransaction() {
    }

    public CreditTransaction(User user, Integer creditChange, CreditTransactionType transactionType, String description) {
        this.user = user;
        this.creditChange = creditChange;
        this.transactionType = transactionType;
        this.description = description;
        this.paymentStatus = transactionType == CreditTransactionType.BONUS ? PaymentStatus.COMPLETED : PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public boolean isCreditAddition() {
        return creditChange > 0;
    }

    public boolean isCreditDeduction() {
        return creditChange < 0;
    }

    public void completeTransaction() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void failTransaction(String notes) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.adminNotes = notes;
        this.processedAt = LocalDateTime.now();
    }

    public void refundTransaction(String notes) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.adminNotes = notes;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return paymentStatus == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    public CreditTransactionResponseDTO toDTO() {
        return new CreditTransactionResponseDTO(
            this.id,
            this.creditChange,
            this.balanceAfter,
            this.transactionType,
            this.paymentStatus,
            this.description,
            this.referenceNumber,
            this.createdAt
        );
    }

    
public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getCreditChange() {
		return creditChange;
	}

	public void setCreditChange(Integer creditChange) {
		this.creditChange = creditChange;
	}

	public Integer getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(Integer balanceAfter) {
		this.balanceAfter = balanceAfter;
	}

	public CreditTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(CreditTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public CreditPackage getCreditPackage() {
		return creditPackage;
	}

	public void setCreditPackage(CreditPackage creditPackage) {
		this.creditPackage = creditPackage;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	public String getAdminNotes() {
		return adminNotes;
	}

	public void setAdminNotes(String adminNotes) {
		this.adminNotes = adminNotes;
	}

@Override
    public String toString() {
        return "CreditTransaction{" +
                "id=" + id +
                ", creditChange=" + creditChange +
                ", transactionType=" + transactionType +
                ", paymentStatus=" + paymentStatus +
                ", description='" + description + '\'' +
                '}';
    }
}
