package com.doublez.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.dto.user.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.entity.UserProfile;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        UserProfileDTO profileDto = null;
        if (user.getUserProfile() != null) {
            profileDto = toProfileDto(user.getUserProfile());
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            mapRoles(user.getRoles()),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            profileDto
        );
    }

    public UserProfileDTO toProfileDto(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return new UserProfileDTO(
            profile.getFirstName(),
            profile.getLastName(),
            profile.getPhone(),
            profile.getBio()
        );
    }

    public User toEntity(UserCreateDTO createDto) {
        if (createDto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(createDto.getEmail());
        return user;
    }

    public void updateEntity(UserUpdateDTO updateDto, User user) {
        if (updateDto == null || user == null) {
            return;
        }

        if (updateDto.getEmail() != null && !updateDto.getEmail().isEmpty()) {
            user.setEmail(updateDto.getEmail());
        }

        // Update profile if provided
        if (updateDto.getProfile() != null) {
            updateUserProfile(updateDto.getProfile(), user);
        }
    }

    // === ADD THIS METHOD - was missing ===
    public void updateUserProfile(UserProfileDTO profileDto, User user) {
        UserProfile profile = user.getOrCreateProfile();
        
        if (profileDto.getFirstName() != null) {
            profile.setFirstName(profileDto.getFirstName());
        }
        if (profileDto.getLastName() != null) {
            profile.setLastName(profileDto.getLastName());
        }
        if (profileDto.getPhone() != null) {
            profile.setPhone(profileDto.getPhone());
        }
        if (profileDto.getBio() != null) {
            profile.setBio(profileDto.getBio());
        }
        
        user.setUserProfile(profile);
    }

    private List<String> mapRoles(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toList());
    }
}