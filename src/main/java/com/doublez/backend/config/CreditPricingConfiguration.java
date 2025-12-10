package com.doublez.backend.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CreditPricingConfiguration {
    
    // SIMPLE 1:1 CONVERSION - 1 credit = 1 RSD
    public static final BigDecimal CREDIT_TO_RSD_RATE = BigDecimal.ONE;
    public static final int CREDITS_PER_RSD = 1;
    
    // ===== SUBSCRIPTIONS (Monthly Platform Access) =====
    public static final int OWNER_SUBSCRIPTION_MONTHLY = 1990;    // 1,990 RSD - 3 listings per month
    public static final int BUSINESS_SUBSCRIPTION_MONTHLY = 2990; // 2,990 RSD - 10 listings per month
    public static final int INVESTOR_SUBSCRIPTION_MONTHLY = 1490; // 1,490 RSD - 5 projects per month
    public static final int CONTRACTOR_SUBSCRIPTION_MONTHLY = 990; // 990 RSD - Profile visibility per month
    
    // Agencies use UserTier enum for subscription pricing
    
    // ===== BOOSTS (One-Time Premium Features - Available to ALL Subscribers) =====
    
    // LISTING VISIBILITY BOOSTS
    public static final int TOP_POSITIONING_BOOST_7DAYS = 600;    // 600 RSD - Better search placement for 7 days
    public static final int URGENT_BADGE_14DAYS = 360;           // 360 RSD - "Urgent" badge and priority for 14 days
    public static final int HIGHLIGHTED_LISTING_30DAYS = 480;    // 480 RSD - Colored highlight for 30 days
    public static final int FEATURED_IN_CATEGORY_15DAYS = 750;   // 750 RSD - Featured in category section for 15 days
    
    // PROFILE BOOSTS
    public static final int VERIFIED_BADGE_30DAYS = 1200;        // 1,200 RSD - "Verified" badge on profile for 30 days
    public static final int PREMIUM_PROFILE_BADGE_30DAYS = 900;  // 900 RSD - "Premium" badge on profile for 30 days
    public static final int AGENCY_FEATURED_PROFILE_15DAYS = 1500; // 1,500 RSD - Agency featured in directory for 15 days
    
    // ===== FUTURE FEATURES (TODO: Implement integration) =====
    public static final int SOCIAL_MEDIA_PROMOTION = 800;        // TODO: Integrate with social media APIs
    public static final int NEWSLETTER_FEATURE = 1000;           // TODO: Implement newsletter system
    public static final int MOBILE_PUSH_NOTIFICATION = 450;      // TODO: Implement push notifications
    public static final int CROSS_PROMOTION = 600;               // TODO: Show to similar property browsers
    
    // ===== BOOST PACKAGES (Discounted Bundles) =====
    public static final int BRONZE_BOOST_PACKAGE = 860;          // 860 RSD - Top Positioning + Urgent Badge (960 RSD value) - Save 100 RSD
    public static final int SILVER_BOOST_PACKAGE = 1700;         // 1,700 RSD - Top + Urgent + Highlighted + Category Featured (2,190 RSD value) - Save 490 RSD
    public static final int GOLD_BOOST_PACKAGE = 3000;           // 3,000 RSD - All boosts + Verified Badge (4,230 RSD value) - Save 1,230 RSD
    
    // ===== AGENCY-SPECIFIC BOOSTS =====
    public static final int MULTIPLE_LISTING_BOOST_7DAYS = 1500;   // 1,500 RSD - Boost 3 listings simultaneously
    public static final int AGENCY_SHOWCASE_FEATURE_30DAYS = 2000; // 2,000 RSD - Featured agency showcase on homepage
    public static final int PREMIUM_AGENCY_BADGE_30DAYS = 1200;    // 1,200 RSD - "Premium Agency" trust badge
    public static final int AGENCY_PRIORITY_SUPPORT_30DAYS = 800;  // 800 RSD - TODO Dedicated support line
    
    // ===== EXTRA ALLOWANCES =====
    public static final int EXTRA_LISTING_SLOT = 700;            // 700 RSD - One additional listing
    public static final int EXTRA_IMAGE_SLOT_10IMAGES = 300;     // TODO: 300 RSD - 10 additional images (implementation needed)
    public static final int PREMIUM_SUPPORT_7DAYS = 250;         // TODO: 250 RSD - Priority customer support for 7 days
    
    // ===== FREE ALLOWANCES =====
    public static final int OWNER_FREE_LISTINGS = 3;             // Owners get 3 listings with subscription
    public static final int BUSINESS_FREE_LISTINGS = 10;         // Business users get 10 listings with subscription
    public static final int INVESTOR_FREE_PROJECTS = 5;          // Investors get 5 projects with subscription
    public static final int AGENCY_TRIAL_CREDITS = 5000;         // 5,000 RSD for agencies during trial period
    
    // ===== IMAGE LIMITATIONS =====
    public static final int OWNER_MAX_IMAGES_TOTAL = 50;      // Owners: 50 images total across all listings
    public static final int BUSINESS_MAX_IMAGES_TOTAL = 200;  // Business: 200 images total
    public static final int INVESTOR_MAX_IMAGES_TOTAL = 100;  // Investors: 100 images total  
    public static final int CONTRACTOR_MAX_IMAGES_TOTAL = 50; // Contractors: 50 images total

    public static final int MAX_IMAGES_PER_LISTING = 20;      // All users: max 20 images per individual listing
    
    // ===== CREDIT PACKAGES (For Purchasing Credits) =====
    public static final Map<Integer, BigDecimal> CREDIT_PACKAGES = Map.of(
        1000, new BigDecimal("1000"),   // 1,000 credits for 1,000 RSD
        2500, new BigDecimal("2250"),   // 2,500 credits for 2,250 RSD (10% discount)
        5000, new BigDecimal("4000"),   // 5,000 credits for 4,000 RSD (20% discount)
        10000, new BigDecimal("7000")   // 10,000 credits for 7,000 RSD (30% discount)
    );
    
    // ===== HELPER METHODS =====
    public static int rsdToCredits(int rsd) {
        return rsd; // 1:1 conversion
    }
    
    public static int creditsToRsd(int credits) {
        return credits; // 1:1 conversion
    }
    
    public static BigDecimal creditsToMoney(int credits) {
        return new BigDecimal(credits); // Returns RSD amount
    }
    
    // ===== BOOST PACKAGE CONTENTS =====
    public static Map<String, List<String>> getBoostPackageContents() {
        return Map.of(
            "BRONZE", List.of("TOP_POSITIONING_BOOST_7DAYS", "URGENT_BADGE_14DAYS"),
            "SILVER", List.of("TOP_POSITIONING_BOOST_7DAYS", "URGENT_BADGE_14DAYS", 
                             "HIGHLIGHTED_LISTING_30DAYS", "FEATURED_IN_CATEGORY_15DAYS"),
            "GOLD", List.of("TOP_POSITIONING_BOOST_7DAYS", "URGENT_BADGE_14DAYS",
                           "HIGHLIGHTED_LISTING_30DAYS", "FEATURED_IN_CATEGORY_15DAYS",
                           "VERIFIED_BADGE_30DAYS", "PREMIUM_PROFILE_BADGE_30DAYS")
        );
    }
    
    // ===== SUBSCRIPTION BENEFITS DESCRIPTION =====
    public static Map<String, String> getSubscriptionBenefits() {
        return Map.of(
            "OWNER", "3 listings per month, basic features, contact management",
            "BUSINESS", "10 listings per month, business profile badge, all owner features",
            "INVESTOR", "5 projects per month, investment tools, project management",
            "CONTRACTOR", "Profile visibility, service listing, contact features"
        );
    }
}