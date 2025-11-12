package com.doublez.backend.controller.usage;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.service.usage.UsageTrackingService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/usage-tracking")
//@CrossOrigin(origins = "http://localhost:5173")
public class UsageTrackingController {
 
 private final UsageTrackingService usageTrackingService;
 private final UserService userService;
 
 public UsageTrackingController(UsageTrackingService usageTrackingService,
                              UserService userService) {
     this.usageTrackingService = usageTrackingService;
     this.userService = userService;
 }
 
 @GetMapping("/detailed")
 public ResponseEntity<Map<String, Object>> getDetailedUsage() {
     try {
         Long userId = userService.getCurrentUserId();
         Map<String, Object> usage = usageTrackingService.getDetailedUsage(userId);
         return ResponseEntity.ok(usage);
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @GetMapping("/can-create-realestate")
 public ResponseEntity<Map<String, Boolean>> canCreateRealEstate() {
     try {
         Long userId = userService.getCurrentUserId();
         boolean canCreate = usageTrackingService.canCreateRealEstate(userId);
         return ResponseEntity.ok(Collections.singletonMap("canCreate", canCreate));
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @GetMapping("/can-upload-images")
 public ResponseEntity<Map<String, Boolean>> canUploadImages(@RequestParam int imageCount) {
     try {
         Long userId = userService.getCurrentUserId();
         boolean canUpload = usageTrackingService.canUploadImage(userId, imageCount);
         return ResponseEntity.ok(Collections.singletonMap("canUpload", canUpload));
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
 
 @GetMapping("/can-feature-listing")
 public ResponseEntity<Map<String, Boolean>> canFeatureListing() {
     try {
         Long userId = userService.getCurrentUserId();
         boolean canFeature = usageTrackingService.canFeatureListing(userId);
         return ResponseEntity.ok(Collections.singletonMap("canFeature", canFeature));
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
}
