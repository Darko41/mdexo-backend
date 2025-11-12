package com.doublez.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.entity.user.User;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/trial")
//@CrossOrigin(origins = "http://localhost:5173")
public class TrialController {
 
 private final TrialService trialService;
 private final UserService userService;
 
 public TrialController(TrialService trialService, UserService userService) {
     this.trialService = trialService;
     this.userService = userService;
 }
 
 @GetMapping("/my-status")
 @PreAuthorize("hasRole('USER')")
 public ResponseEntity<Map<String, Object>> getMyTrialStatus() {
     try {
         Long userId = userService.getCurrentUserId();
         User user = userService.getUserEntityById(userId);
         
         Map<String, Object> status = new HashMap<>();
         status.put("inTrial", trialService.isInTrial(user));
         status.put("trialExpired", trialService.isTrialExpired(user));
         status.put("daysRemaining", trialService.getTrialDaysRemaining(user));
         status.put("progressPercentage", trialService.getTrialProgressPercentage(user));
         status.put("tier", user.getTier());
         
         if (user.getTrialStartDate() != null) {
             status.put("trialStartDate", user.getTrialStartDate());
         }
         if (user.getTrialEndDate() != null) {
             status.put("trialEndDate", user.getTrialEndDate());
         }
         
         return ResponseEntity.ok(status);
         
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
     }
 }
}
