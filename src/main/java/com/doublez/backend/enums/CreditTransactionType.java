package com.doublez.backend.enums;

public enum CreditTransactionType {
    PURCHASE,   // Buying credits
    USAGE,      // Spending credits on features/tiers
    REFUND,     // Credit refund
    BONUS,      // Free credits (trial, promotions)
    ADJUSTMENT  // Manual adjustment by admin
}
