package com.doublez.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doublez.backend.dto.InvestorProfileDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.entity.InvestorProfile;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.mapper.UserMapper;
import com.doublez.backend.repository.UserRepository;
import com.doublez.backend.service.user.RolePromotionService;
import com.doublez.backend.service.user.UserService;

@RestController
@RequestMapping("/api/investor")
//@CrossOrigin(origins = "http://localhost:5173")
public class InvestorProfileController {

	private final RolePromotionService rolePromotionService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    
    public InvestorProfileController(RolePromotionService rolePromotionService,
                                   UserRepository userRepository,
                                   UserService userService,
                                   UserMapper userMapper) {
        this.rolePromotionService = rolePromotionService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/apply")
    public ResponseEntity<UserDTO> applyAsInvestor(@RequestBody InvestorProfileDTO investorProfileDto) {
        try {
            Long userId = userService.getCurrentUserId();
            UserDTO updatedUser = rolePromotionService.promoteToInvestor(userId, investorProfileDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

	@PutMapping("/profile")
	public ResponseEntity<UserDTO> updateInvestorProfile(@RequestBody InvestorProfileDTO investorProfileDto) {
		try {
			Long userId = userService.getCurrentUserId();
			User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

			if (!user.isInvestor()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			InvestorProfile investorProfile = user.getOrCreateInvestorProfile();
			userMapper.updateInvestorProfileFromDTO(investorProfileDto, investorProfile);
			user.setInvestorProfile(investorProfile);

			User updatedUser = userRepository.save(user);
			return ResponseEntity.ok(userMapper.toDTO(updatedUser));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/profile")
	public ResponseEntity<InvestorProfileDTO> getInvestorProfile() {
		try {
			Long userId = userService.getCurrentUserId();
			User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

			if (!user.isInvestor() || user.getInvestorProfile() == null) {
				return ResponseEntity.notFound().build();
			}

			InvestorProfileDTO profileDto = userMapper.toInvestorProfileDTO(user.getInvestorProfile());
			return ResponseEntity.ok(profileDto);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
