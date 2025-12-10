package com.doublez.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.property.EnergyEfficiency;
import com.doublez.backend.enums.property.PropertyType;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate> {

    // ===== BASIC CRUD & COUNT METHODS =====
    long count();
    boolean existsByOwner(User owner);
    boolean existsByPropertyIdAndOwnerId(Long propertyId, Long ownerId);

    // ===== COUNT BY CRITERIA METHODS =====
    long countByIsActive(Boolean isActive);
    long countByIsFeatured(Boolean isFeatured);
    long countByListingType(ListingType listingType);
    
    // ===== BULK OPERATIONS =====
    @Modifying
    @Query("UPDATE RealEstate r SET r.owner.id = :newOwnerId WHERE r.owner.id = :oldOwnerId")
    void reassignAllPropertiesFromUser(@Param("oldOwnerId") Long oldOwnerId, @Param("newOwnerId") Long newOwnerId);

    // ===== SEARCH & FILTER METHODS =====
    @Query("SELECT re FROM RealEstate re WHERE " +
           "(LOWER(re.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(re.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "re.isActive = true")
    Page<RealEstate> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT re FROM RealEstate re WHERE re.owner.id = :userId")
    List<RealEstate> findByUserId(@Param("userId") Long userId);

    // NEW: Paginated version of findByUserId
    @Query("SELECT re FROM RealEstate re WHERE re.owner.id = :userId")
    Page<RealEstate> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // ===== AGENCY PROPERTIES =====
    @Query("SELECT re FROM RealEstate re WHERE re.agency.id = :agencyId")
    List<RealEstate> findByAgencyId(@Param("agencyId") Long agencyId);

    // NEW: Paginated version of findByAgencyId
    @Query("SELECT re FROM RealEstate re WHERE re.agency.id = :agencyId")
    Page<RealEstate> findByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    @Query("SELECT re FROM RealEstate re WHERE re.agency.id = :agencyId AND re.isActive = true")
    List<RealEstate> findActiveRealEstatesByAgency(@Param("agencyId") Long agencyId);

    // ===== LIMITATION & USAGE TRACKING METHODS =====
    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.owner.id = :userId AND re.isActive = true")
    Long countActiveRealEstatesByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.agency.id = :agencyId AND re.isActive = true")
    Long countActiveRealEstatesByAgency(@Param("agencyId") Long agencyId);

    // ===== FEATURED LISTING METHODS =====
    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.owner.id = :userId AND re.isFeatured = true AND re.isActive = true")
    Long countFeaturedRealEstatesByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.owner.id = :userId AND re.isFeatured = true AND " +
           "(re.featuredUntil IS NULL OR re.featuredUntil > CURRENT_TIMESTAMP) AND re.isActive = true")
    Long countActiveFeaturedRealEstatesByUser(@Param("userId") Long userId);

    @Query("SELECT re FROM RealEstate re WHERE re.isFeatured = true AND " +
           "(re.featuredUntil IS NULL OR re.featuredUntil > CURRENT_TIMESTAMP) AND re.isActive = true " +
           "ORDER BY re.featuredAt DESC")
    List<RealEstate> findActiveFeaturedRealEstates(Pageable pageable);

    // NEW: Find featured active properties (renamed for clarity)
    @Query("SELECT re FROM RealEstate re WHERE re.isFeatured = true AND " +
           "(re.featuredUntil IS NULL OR re.featuredUntil > CURRENT_TIMESTAMP) AND re.isActive = true " +
           "ORDER BY re.featuredAt DESC")
    List<RealEstate> findFeaturedActiveProperties(Pageable pageable);

    @Query("SELECT re FROM RealEstate re WHERE re.owner.id = :userId AND re.isFeatured = true " +
           "ORDER BY re.featuredAt DESC")
    List<RealEstate> findFeaturedRealEstatesByUser(@Param("userId") Long userId);

    @Query("SELECT re FROM RealEstate re WHERE re.isFeatured = true AND re.featuredUntil < CURRENT_TIMESTAMP")
    List<RealEstate> findExpiredFeaturedRealEstates();

    // ===== ADMIN METHODS =====
    @Query("SELECT re FROM RealEstate re WHERE re.isActive = false")
    List<RealEstate> findInactiveRealEstates();

    @Query("SELECT re FROM RealEstate re WHERE re.isActive = true")
    List<RealEstate> findActiveRealEstates();

    // ===== NEW ENHANCED METHODS =====

    // NEW: Find popular properties by view count
    @Query("SELECT re FROM RealEstate re WHERE re.isActive = true ORDER BY re.viewCount DESC, re.createdAt DESC")
    List<RealEstate> findPopularProperties(Pageable pageable);

    // NEW: Find recently added properties
    @Query("SELECT re FROM RealEstate re WHERE re.isActive = true ORDER BY re.createdAt DESC")
    List<RealEstate> findRecentlyAddedProperties(Pageable pageable);

    // NEW: Find similar properties (excludes current property)
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.propertyType = :propertyType AND " +
           "LOWER(re.city) = LOWER(:city) AND " +
           "re.price BETWEEN :minPrice AND :maxPrice AND " +
           "re.propertyId != :excludeId AND " +
           "re.isActive = true " +
           "ORDER BY ABS(re.price - :targetPrice), re.viewCount DESC")
    List<RealEstate> findSimilarProperties(
            @Param("propertyType") PropertyType propertyType,
            @Param("city") String city,
            @Param("targetPrice") BigDecimal targetPrice,
            @Param("excludeId") Long excludeId,
            Pageable pageable);

    // NEW: Find properties by multiple criteria for advanced search
    @Query("SELECT re FROM RealEstate re WHERE " +
           "(:propertyType IS NULL OR re.propertyType = :propertyType) AND " +
           "(:listingType IS NULL OR re.listingType = :listingType) AND " +
           "(:city IS NULL OR LOWER(re.city) = LOWER(:city)) AND " +
           "(:minPrice IS NULL OR re.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR re.price <= :maxPrice) AND " +
           "(:minBedrooms IS NULL OR re.roomCount >= :minBedrooms) AND " +
           "(:maxBedrooms IS NULL OR re.roomCount <= :maxBedrooms) AND " +
           "(:hasParking IS NULL OR re.hasParking = :hasParking) AND " +
           "(:hasElevator IS NULL OR re.hasElevator = :hasElevator) AND " +
           "(:energyEfficiency IS NULL OR re.energyEfficiency = :energyEfficiency) AND " +
           "re.isActive = true")
    Page<RealEstate> findByMultipleCriteria(
            @Param("propertyType") PropertyType propertyType,
            @Param("listingType") ListingType listingType,
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minBedrooms") BigDecimal minBedrooms,
            @Param("maxBedrooms") BigDecimal maxBedrooms,
            @Param("hasParking") Boolean hasParking,
            @Param("hasElevator") Boolean hasElevator,
            @Param("energyEfficiency") EnergyEfficiency energyEfficiency,
            Pageable pageable);

    // NEW: Find properties with furniture status
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.isActive = true AND " +
           "(:isFurnished IS NULL OR re.isFurnished = :isFurnished) AND " +
           "(:isSemiFurnished IS NULL OR re.isSemiFurnished = :isSemiFurnished)")
    Page<RealEstate> findByFurnitureStatus(
            @Param("isFurnished") Boolean isFurnished,
            @Param("isSemiFurnished") Boolean isSemiFurnished,
            Pageable pageable);

    // NEW: Count properties by property type
    @Query("SELECT re.propertyType, COUNT(re) FROM RealEstate re WHERE re.isActive = true GROUP BY re.propertyType")
    List<Object[]> countByPropertyType();

    // NEW: Count properties by city
    @Query("SELECT re.city, COUNT(re) FROM RealEstate re WHERE re.isActive = true GROUP BY re.city")
    List<Object[]> countByCity();

    // NEW: Find properties created within date range
    @Query("SELECT re FROM RealEstate re WHERE re.createdAt BETWEEN :startDate AND :endDate")
    List<RealEstate> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    // NEW: Find properties that need featuring renewal (expiring soon)
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.isFeatured = true AND " +
           "re.featuredUntil IS NOT NULL AND " +
           "re.featuredUntil BETWEEN CURRENT_TIMESTAMP AND :thresholdDate")
    List<RealEstate> findFeaturedPropertiesExpiringSoon(@Param("thresholdDate") LocalDateTime thresholdDate);

    // NEW: Find properties with high engagement
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.isActive = true AND " +
           "(re.viewCount > :minViews OR re.contactCount > :minContacts OR re.favoriteCount > :minFavorites) " +
           "ORDER BY (re.viewCount + re.contactCount * 2 + re.favoriteCount * 3) DESC")
    List<RealEstate> findHighEngagementProperties(
            @Param("minViews") Long minViews,
            @Param("minContacts") Long minContacts,
            @Param("minFavorites") Long minFavorites,
            Pageable pageable);

    // NEW: Find properties by owner with additional filters
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.owner.id = :ownerId AND " +
           "(:isActive IS NULL OR re.isActive = :isActive) AND " +
           "(:listingType IS NULL OR re.listingType = :listingType)")
    Page<RealEstate> findByOwnerWithFilters(
            @Param("ownerId") Long ownerId,
            @Param("isActive") Boolean isActive,
            @Param("listingType") ListingType listingType,
            Pageable pageable);

    // NEW: Bulk update methods
    @Modifying
    @Query("UPDATE RealEstate re SET re.isActive = :isActive WHERE re.propertyId IN :propertyIds")
    int bulkUpdateActiveStatus(@Param("propertyIds") List<Long> propertyIds, 
                              @Param("isActive") Boolean isActive);

    @Modifying
    @Query("UPDATE RealEstate re SET re.isFeatured = :isFeatured, re.featuredAt = :featuredAt, re.featuredUntil = :featuredUntil WHERE re.propertyId IN :propertyIds")
    int bulkUpdateFeaturedStatus(@Param("propertyIds") List<Long> propertyIds,
                                @Param("isFeatured") Boolean isFeatured,
                                @Param("featuredAt") LocalDateTime featuredAt,
                                @Param("featuredUntil") LocalDateTime featuredUntil);

    // NEW: Analytics methods
    @Query("SELECT AVG(re.price) FROM RealEstate re WHERE re.isActive = true AND re.listingType = :listingType")
    BigDecimal findAveragePriceByListingType(@Param("listingType") ListingType listingType);

    @Query("SELECT MIN(re.price), MAX(re.price), AVG(re.price) FROM RealEstate re WHERE re.isActive = true AND re.propertyType = :propertyType")
    Object[] findPriceStatsByPropertyType(@Param("propertyType") PropertyType propertyType);

    // NEW: Find properties near location (approximate)
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.latitude BETWEEN :minLat AND :maxLat AND " +
           "re.longitude BETWEEN :minLng AND :maxLng AND " +
           "re.isActive = true")
    List<RealEstate> findPropertiesInArea(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            Pageable pageable);
    
    // 🆕 IMAGE COUNTING METHODS
    @Query("SELECT COALESCE(SUM(re.imageCount), 0) FROM RealEstate re WHERE re.agency.id = :agencyId AND re.isActive = true")
    Long getTotalImageCountByAgencyId(@Param("agencyId") Long agencyId);

    @Query("SELECT COALESCE(SUM(re.imageCount), 0) FROM RealEstate re WHERE re.owner.id = :userId AND re.isActive = true")
    Long getTotalImageCountByUserId(@Param("userId") Long userId);

    @Query("SELECT re.imageCount FROM RealEstate re WHERE re.id = :realEstateId")
    Integer getImageCountByRealEstateId(@Param("realEstateId") Long realEstateId);
    
 // Custom query methods for visibility
    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.isActive = true OR " +
           "re.owner.id = :userId OR " +
           "(re.agency IS NOT NULL AND re.agency.id IN (SELECT a.agency.id FROM Agent a WHERE a.user.id = :userId AND a.isActive = true))")
    Page<RealEstate> findVisibleToUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT re FROM RealEstate re WHERE " +
           "re.isActive = true OR " +
           "re.agency.id IN :agencyIds")
    Page<RealEstate> findVisibleToAgencies(@Param("agencyIds") List<Long> agencyIds, Pageable pageable);

    List<RealEstate> findByAgency(Agency agency);

    List<RealEstate> findByAgencyAndListingAgent(Agency agency, Agent listingAgent);

    @Query("SELECT COUNT(re) FROM RealEstate re WHERE re.agency = :agency AND re.listingAgent = :agent")
    Long countByAgencyAndAgent(@Param("agency") Agency agency, @Param("agent") Agent agent);
}