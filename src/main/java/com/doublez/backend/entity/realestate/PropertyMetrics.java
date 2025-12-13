package com.doublez.backend.entity.realestate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.doublez.backend.utils.JsonUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.Index;

@Entity
@Table(name = "property_metrics",
       indexes = {
           @Index(name = "idx_metrics_property", columnList = "property_id", unique = true),
           @Index(name = "idx_view_count", columnList = "view_count DESC"),
           @Index(name = "idx_last_viewed", columnList = "last_viewed_at DESC")
       })
public class PropertyMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === PROPERTY REFERENCE ===
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", unique = true)
    private RealEstate property;
    
    // === COUNTERS ===
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;
    
    @Column(name = "favorite_count", nullable = false)
    private Long favoriteCount = 0L;
    
    @Column(name = "contact_count", nullable = false)
    private Long contactCount = 0L;
    
    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;
    
    @Column(name = "lead_count", nullable = false)
    private Long leadCount = 0L; // Leads generated from this property
    
    // === LAST ACTION TIMESTAMPS ===
    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;
    
    @Column(name = "last_favorited_at")
    private LocalDateTime lastFavoritedAt;
    
    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;
    
    @Column(name = "last_shared_at")
    private LocalDateTime lastSharedAt;
    
    @Column(name = "last_lead_at")
    private LocalDateTime lastLeadAt;
    
    // === TIME-SERIES DATA ===
    @Column(name = "daily_views_json", columnDefinition = "JSON")
    private String dailyViewsJson; // {"2024-01-01": 15, "2024-01-02": 20}
    
    @Column(name = "weekly_views_json", columnDefinition = "JSON")
    private String weeklyViewsJson; // {"2024-W01": 150, "2024-W02": 180}
    
    @Column(name = "hourly_pattern_json", columnDefinition = "JSON")
    private String hourlyPatternJson; // {"8": 5, "9": 10, "10": 15}
    
    // === AGGREGATES ===
    @Column(name = "views_today", nullable = false)
    private Integer viewsToday = 0;
    
    @Column(name = "views_this_week", nullable = false)
    private Integer viewsThisWeek = 0;
    
    @Column(name = "views_this_month", nullable = false)
    private Integer viewsThisMonth = 0;
    
    @Column(name = "views_last_7_days", nullable = false)
    private Integer viewsLast7Days = 0;
    
    @Column(name = "views_last_30_days", nullable = false)
    private Integer viewsLast30Days = 0;
    
    // === PERFORMANCE METRICS ===
    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate; // views → leads
    
    @Column(name = "engagement_rate", precision = 5, scale = 2)
    private BigDecimal engagementRate; // views → favorites/contacts
    
    @Column(name = "performance_score", precision = 3, scale = 0)
    private Integer performanceScore; // 0-100 based on all metrics
    
    // === COMPARISON METRICS ===
    @Column(name = "views_vs_average", precision = 5, scale = 2)
    private BigDecimal viewsVsAverage; // % vs similar properties
    
    @Column(name = "favorites_vs_average", precision = 5, scale = 2)
    private BigDecimal favoritesVsAverage;
    
    // === TRENDS ===
    @Column(name = "view_trend_7d", precision = 5, scale = 2)
    private BigDecimal viewTrend7d; // % change vs previous 7 days
    
    @Column(name = "view_trend_30d", precision = 5, scale = 2)
    private BigDecimal viewTrend30d;
    
    @Column(name = "is_trending_up")
    private Boolean isTrendingUp;
    
    @Column(name = "trend_started_at")
    private LocalDateTime trendStartedAt;
    
    // === AUDIT FIELDS ===
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt; // When metrics were recalculated
    
    @Version
    private Integer version;
    
    // === LIFECYCLE ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // === HELPER METHODS ===
    
    /**
     * Increment favorite count
     */
    public void incrementFavoriteCount() {
        this.favoriteCount++;
        this.lastFavoritedAt = LocalDateTime.now();
    }
    
    /**
     * Decrement favorite count
     */
    public void decrementFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }
    
    /**
     * Increment contact count
     */
    public void incrementContactCount() {
        this.contactCount++;
        this.lastContactedAt = LocalDateTime.now();
    }
    
    /**
     * Increment lead count
     */
    public void incrementLeadCount() {
        this.leadCount++;
        this.lastLeadAt = LocalDateTime.now();
    }
    
    /**
     * Get daily views as map (convenience method)
     */
    @Transient
    public Map<String, Integer> getDailyViews() {
        return JsonUtils.parseMap(dailyViewsJson, String.class, Integer.class);
    }

    /**
     * Set daily views from map (convenience method)
     */
    public void setDailyViews(Map<String, Integer> dailyViews) {
        this.dailyViewsJson = JsonUtils.toJson(dailyViews);
    }

    /**
     * Get weekly views as map
     */
    @Transient
    public Map<String, Integer> getWeeklyViews() {
        return JsonUtils.parseMap(weeklyViewsJson, String.class, Integer.class);
    }

    /**
     * Set weekly views from map
     */
    public void setWeeklyViews(Map<String, Integer> weeklyViews) {
        this.weeklyViewsJson = JsonUtils.toJson(weeklyViews);
    }

    /**
     * Get hourly pattern as map
     */
    @Transient
    public Map<String, Integer> getHourlyPattern() {
        return JsonUtils.parseMap(hourlyPatternJson, String.class, Integer.class);
    }

    /**
     * Set hourly pattern from map
     */
    public void setHourlyPattern(Map<String, Integer> hourlyPattern) {
        this.hourlyPatternJson = JsonUtils.toJson(hourlyPattern);
    }

    /**
     * Increment view count for today
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.viewsToday++;
        this.viewsThisWeek++;
        this.viewsThisMonth++;
        this.viewsLast7Days++;
        this.viewsLast30Days++;
        this.lastViewedAt = LocalDateTime.now();
        
        // Update daily views map
        String today = LocalDate.now().toString();
        Map<String, Integer> dailyViews = getDailyViews();
        dailyViews.put(today, dailyViews.getOrDefault(today, 0) + 1);
        setDailyViews(dailyViews);
    }

    /**
     * Calculate conversion rate
     */
    @Transient
    public BigDecimal calculateConversionRate() {
        if (viewCount == 0 || leadCount == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(leadCount)
                .divide(BigDecimal.valueOf(viewCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate engagement rate
     */
    @Transient
    public BigDecimal calculateEngagementRate() {
        if (viewCount == 0) return BigDecimal.ZERO;
        long engagements = favoriteCount + contactCount;
        return BigDecimal.valueOf(engagements)
                .divide(BigDecimal.valueOf(viewCount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get views per day average
     */
    @Transient
    public BigDecimal getAverageViewsPerDay() {
        if (createdAt == null) return BigDecimal.ZERO;
        long days = Duration.between(createdAt, LocalDateTime.now()).toDays();
        if (days == 0) return BigDecimal.valueOf(viewCount);
        return BigDecimal.valueOf(viewCount).divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    /**
     * Check if property is popular (above threshold)
     */
    @Transient
    public boolean isPopular(Integer threshold) {
        return viewCount >= threshold;
    }

    /**
     * Reset daily counter (called by scheduled job)
     */
    public void resetDailyCounters() {
        this.viewsToday = 0;
    }

    /**
     * Update trending status
     */
    public void updateTrendStatus(BigDecimal threshold) {
        if (viewTrend7d == null) {
            this.isTrendingUp = false;
            return;
        }
        
        boolean wasTrending = Boolean.TRUE.equals(isTrendingUp);
        boolean isNowTrending = viewTrend7d.compareTo(threshold) > 0;
        
        this.isTrendingUp = isNowTrending;
        
        if (isNowTrending && !wasTrending) {
            this.trendStartedAt = LocalDateTime.now();
        } else if (!isNowTrending) {
            this.trendStartedAt = null;
        }
    }
    
    // Getters and setters...

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RealEstate getProperty() {
		return property;
	}

	public void setProperty(RealEstate property) {
		this.property = property;
	}

	public Long getViewCount() {
		return viewCount;
	}

	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}

	public Long getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(Long favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public Long getContactCount() {
		return contactCount;
	}

	public void setContactCount(Long contactCount) {
		this.contactCount = contactCount;
	}

	public Long getShareCount() {
		return shareCount;
	}

	public void setShareCount(Long shareCount) {
		this.shareCount = shareCount;
	}

	public Long getLeadCount() {
		return leadCount;
	}

	public void setLeadCount(Long leadCount) {
		this.leadCount = leadCount;
	}

	public LocalDateTime getLastViewedAt() {
		return lastViewedAt;
	}

	public void setLastViewedAt(LocalDateTime lastViewedAt) {
		this.lastViewedAt = lastViewedAt;
	}

	public LocalDateTime getLastFavoritedAt() {
		return lastFavoritedAt;
	}

	public void setLastFavoritedAt(LocalDateTime lastFavoritedAt) {
		this.lastFavoritedAt = lastFavoritedAt;
	}

	public LocalDateTime getLastContactedAt() {
		return lastContactedAt;
	}

	public void setLastContactedAt(LocalDateTime lastContactedAt) {
		this.lastContactedAt = lastContactedAt;
	}

	public LocalDateTime getLastSharedAt() {
		return lastSharedAt;
	}

	public void setLastSharedAt(LocalDateTime lastSharedAt) {
		this.lastSharedAt = lastSharedAt;
	}

	public LocalDateTime getLastLeadAt() {
		return lastLeadAt;
	}

	public void setLastLeadAt(LocalDateTime lastLeadAt) {
		this.lastLeadAt = lastLeadAt;
	}

	public String getDailyViewsJson() {
		return dailyViewsJson;
	}

	public void setDailyViewsJson(String dailyViewsJson) {
		this.dailyViewsJson = dailyViewsJson;
	}

	public String getWeeklyViewsJson() {
		return weeklyViewsJson;
	}

	public void setWeeklyViewsJson(String weeklyViewsJson) {
		this.weeklyViewsJson = weeklyViewsJson;
	}

	public String getHourlyPatternJson() {
		return hourlyPatternJson;
	}

	public void setHourlyPatternJson(String hourlyPatternJson) {
		this.hourlyPatternJson = hourlyPatternJson;
	}

	public Integer getViewsToday() {
		return viewsToday;
	}

	public void setViewsToday(Integer viewsToday) {
		this.viewsToday = viewsToday;
	}

	public Integer getViewsThisWeek() {
		return viewsThisWeek;
	}

	public void setViewsThisWeek(Integer viewsThisWeek) {
		this.viewsThisWeek = viewsThisWeek;
	}

	public Integer getViewsThisMonth() {
		return viewsThisMonth;
	}

	public void setViewsThisMonth(Integer viewsThisMonth) {
		this.viewsThisMonth = viewsThisMonth;
	}

	public Integer getViewsLast7Days() {
		return viewsLast7Days;
	}

	public void setViewsLast7Days(Integer viewsLast7Days) {
		this.viewsLast7Days = viewsLast7Days;
	}

	public Integer getViewsLast30Days() {
		return viewsLast30Days;
	}

	public void setViewsLast30Days(Integer viewsLast30Days) {
		this.viewsLast30Days = viewsLast30Days;
	}

	public BigDecimal getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(BigDecimal conversionRate) {
		this.conversionRate = conversionRate;
	}

	public BigDecimal getEngagementRate() {
		return engagementRate;
	}

	public void setEngagementRate(BigDecimal engagementRate) {
		this.engagementRate = engagementRate;
	}

	public Integer getPerformanceScore() {
		return performanceScore;
	}

	public void setPerformanceScore(Integer performanceScore) {
		this.performanceScore = performanceScore;
	}

	public BigDecimal getViewsVsAverage() {
		return viewsVsAverage;
	}

	public void setViewsVsAverage(BigDecimal viewsVsAverage) {
		this.viewsVsAverage = viewsVsAverage;
	}

	public BigDecimal getFavoritesVsAverage() {
		return favoritesVsAverage;
	}

	public void setFavoritesVsAverage(BigDecimal favoritesVsAverage) {
		this.favoritesVsAverage = favoritesVsAverage;
	}

	public BigDecimal getViewTrend7d() {
		return viewTrend7d;
	}

	public void setViewTrend7d(BigDecimal viewTrend7d) {
		this.viewTrend7d = viewTrend7d;
	}

	public BigDecimal getViewTrend30d() {
		return viewTrend30d;
	}

	public void setViewTrend30d(BigDecimal viewTrend30d) {
		this.viewTrend30d = viewTrend30d;
	}

	public Boolean getIsTrendingUp() {
		return isTrendingUp;
	}

	public void setIsTrendingUp(Boolean isTrendingUp) {
		this.isTrendingUp = isTrendingUp;
	}

	public LocalDateTime getTrendStartedAt() {
		return trendStartedAt;
	}

	public void setTrendStartedAt(LocalDateTime trendStartedAt) {
		this.trendStartedAt = trendStartedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getLastCalculatedAt() {
		return lastCalculatedAt;
	}

	public void setLastCalculatedAt(LocalDateTime lastCalculatedAt) {
		this.lastCalculatedAt = lastCalculatedAt;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
    
}
