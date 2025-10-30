package com.doublez.backend.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.enums.ListingType;
import com.doublez.backend.enums.PropertyType;

import jakarta.persistence.criteria.Predicate;

public class RealEstateSpecifications {

    public static Specification<RealEstate> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<RealEstate> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<RealEstate> hasPropertyType(PropertyType propertyType) {
        return (root, query, cb) -> propertyType == null ? null : cb.equal(root.get("propertyType"), propertyType);
    }

    public static Specification<RealEstate> hasFeatures(List<String> features) {
        return (root, query, cb) -> {
            if (features == null || features.isEmpty()) return null;
            return root.join("features").in(features);
        };
    }

    public static Specification<RealEstate> hasLocation(String city, String state, String zipCode) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (city != null) predicates.add(cb.equal(root.get("city"), city));
            if (state != null) predicates.add(cb.equal(root.get("state"), state));
            if (zipCode != null) predicates.add(cb.equal(root.get("zipCode"), zipCode));
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<RealEstate> hasListingType(ListingType listingType) {
        return (root, query, criteriaBuilder) -> 
            listingType == null ? 
                null : 
                criteriaBuilder.equal(root.get("listingType"), listingType);
    }

}
