package com.doublez.backend.specification;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.doublez.backend.entity.RealEstate;

import jakarta.persistence.criteria.Predicate;

public class RealEstateSpecifications {
	
	public static Specification<RealEstate> hasMinPrice(BigDecimal priceMin) {
		return (root, query, criteriaBulder) -> {
			if (priceMin == null) return null;
			return criteriaBulder.greaterThanOrEqualTo(root.get("price"), priceMin);
		};
	}
	
	public static Specification<RealEstate> hasMaxPrice(BigDecimal priceMax) {
		return (root, query, criteriaBulder) -> {
			if (priceMax == null) return null;
			return criteriaBulder.lessThanOrEqualTo(root.get("price"), priceMax);
		};
	}
	
	public static Specification<RealEstate> hasPropertyType(String propertyType) {
		return (root, query, criteriaBulder) -> {
			if (propertyType == null) return null;
			return criteriaBulder.equal(root.get("propertyType"), propertyType);
		};
	}
	
	public static Specification<RealEstate> hasListingType(String listingType) {
		return (root, query, criteriaBulder) -> {
			if (listingType == null) return null;
			return criteriaBulder.equal(root.get("listingType"), listingType);
		};
	}
	
	public static Specification<RealEstate> hasFeatures(List<String> features) {
		return (root, query, criteriaBulder) -> {
			if (features == null || features.isEmpty()) return null;
			return criteriaBulder.isTrue(root.get("features").in(features));
		};
	}
	
	public static Specification<RealEstate> hasLocation(String city, String state, String zipCode) {
		return (root, query, criteriaBulder) -> {
			if (city == null && state == null && zipCode == null) return null;
			Predicate cityPredicate = city != null ? criteriaBulder.equal(root.get("city"), city) : null;
			Predicate statePredicate = state != null ? criteriaBulder.equal(root.get("state"), state) : null;
			Predicate zipCodePredicate = zipCode != null ? criteriaBulder.equal(root.get("zipCode"), zipCode) : null;
			
			return criteriaBulder.and(
					cityPredicate != null ? cityPredicate : criteriaBulder.conjunction(),
					statePredicate != null ? statePredicate : criteriaBulder.conjunction(),
					zipCodePredicate != null ? zipCodePredicate : criteriaBulder.conjunction()
					);
		};
	}

}
