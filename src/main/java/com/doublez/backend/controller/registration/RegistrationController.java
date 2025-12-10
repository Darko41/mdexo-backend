package com.doublez.backend.controller.registration;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.entity.user.UserRole;
import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.service.usage.TrialService;
import com.doublez.backend.service.user.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {
    
    private final UserService userService;
    private final TrialService trialService;

    public RegistrationController(UserService userService, TrialService trialService) {
        this.userService = userService;
        this.trialService = trialService;
    }

    @GetMapping("/trial-availability")
    public ResponseEntity<Map<String, Object>> checkTrialAvailability() {
        return ResponseEntity.ok(trialService.getTrialAvailability());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserCreateDTO createDto) {
        // Check if trial is still available
        if (createDto.getRole() == UserRole.AGENCY && createDto.getAgency() != null) {
            Map<String, Object> trialAvailability = trialService.getTrialAvailability();
            if (!(Boolean) trialAvailability.get("trialAvailable")) {
                throw new IllegalOperationException("Trial registration is no longer available");
            }
        }
        
        UserResponseDTO user = userService.registerUser(createDto, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}