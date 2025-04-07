package com.doublez.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.UserCreateDTO;
import com.doublez.backend.dto.UserResponseDTO;
import com.doublez.backend.dto.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            mapRoles(user.getRoles()),
            user.getCreatedAt(),
            user.getUpdatedAt()
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
