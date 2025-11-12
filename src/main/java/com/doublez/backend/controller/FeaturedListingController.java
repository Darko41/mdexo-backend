package com.doublez.backend.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.exception.LimitationExceededException;
import com.doublez.backend.service.realestate.FeaturedListingService;
import com.doublez.backend.utils.SecurityUtils;

@RestController
@RequestMapping("/api/featured")
//@CrossOrigin(origins = "http://localhost:5173")
public class FeaturedListingController {
 
 private final FeaturedListingService featuredListingService;
 private final SecurityUtils securityUtils;
 
 public FeaturedListingController(FeaturedListingService featuredListingService,
                                SecurityUtils securityUtils) {
     this.featuredListingService = featuredListingService;
     this.securityUtils = securityUtils;
 }
 
 private Long getCurrentUserId() {
     // Use the same method from UsageController
     String username = securityUtils.getCurrentUsername();
     // You'll need to implement this method in your service
     // return userService.getCurrentUserId();
     throw new UnsupportedOperationException("Implement user ID retrieval");
 }
 
 @PostMapping("/{realEstateId}")
 public ResponseEntity<?> featureListing(@PathVariable Long realEstateId,
                                       @RequestParam(defaultValue = "30") Integer featuredDays) {
     try {
         Long userId = getCurrentUserId();
         RealEstate featured = featuredListingService.featureRealEstate(userId, realEstateId, featuredDays);
         return ResponseEntity.ok(featured);
     } catch (LimitationExceededException e) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @DeleteMapping("/{realEstateId}")
 public ResponseEntity<?> unfeatureListing(@PathVariable Long realEstateId) {
     try {
         Long userId = getCurrentUserId();
         RealEstate unfeatured = featuredListingService.unfeatureRealEstate(userId, realEstateId);
         return ResponseEntity.ok(unfeatured);
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @GetMapping("/can-feature/{realEstateId}")
 public ResponseEntity<Map<String, Boolean>> canFeatureListing(@PathVariable Long realEstateId) {
     try {
         Long userId = getCurrentUserId();
         boolean canFeature = featuredListingService.canFeatureRealEstate(userId, realEstateId);
         return ResponseEntity.ok(Collections.singletonMap("canFeature", canFeature));
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @GetMapping("/active")
 public ResponseEntity<List<RealEstate>> getActiveFeaturedListings(
         @RequestParam(defaultValue = "10") Integer limit) {
     try {
         List<RealEstate> featured = featuredListingService.getActiveFeaturedListings(limit);
         return ResponseEntity.ok(featured);
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
}