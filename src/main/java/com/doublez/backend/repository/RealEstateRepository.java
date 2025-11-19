package com.doublez.backend.repository;

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
import com.doublez.backend.entity.user.User;

@Repository
public interface RealEstateRepository extends JpaRepository<RealEstate, Long>, JpaSpecificationExecutor<RealEstate> {

    // ===== BASIC CRUD & COUNT METHODS =====
    long count();
    boolean existsByOwner(User owner);
    boolean existsByPropertyIdAndOwnerId(Long propertyId, Long ownerId);

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

    // ===== AGENCY PROPERTIES =====
    @Query("SELECT re FROM RealEstate re WHERE re.agency.id = :agencyId")
    List<RealEstate> findByAgencyId(@Param("agencyId") Long agencyId);

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
}