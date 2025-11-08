package com.doublez.backend.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.agent.AgencyDTO;
import com.doublez.backend.dto.agent.AgencyInfoDTO;
import com.doublez.backend.dto.agent.AgencyMembershipDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.User;
import com.doublez.backend.entity.UserProfile;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.AgencyMembership;

@Component
public class UserMapper {

    // ===== USER MAPPING METHODS =====

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Map profile if exists
        if (user.getUserProfile() != null) {
            dto.setProfile(toProfileDto(user.getUserProfile()));
        }
        
        // Map agency info if user is an agent with active memberships
        if (user.isAgent() && user.getAgencyMemberships() != null) {
            Optional<AgencyMembership> activeMembership = user.getAgencyMemberships().stream()
                    .filter(m -> m.getStatus() == AgencyMembership.MembershipStatus.ACTIVE)
                    .findFirst();
                    
            if (activeMembership.isPresent()) {
                Agency agency = activeMembership.get().getAgency();
                AgencyInfoDTO agencyInfo = new AgencyInfoDTO();
                agencyInfo.setId(agency.getId());
                agencyInfo.setName(agency.getName());
                agencyInfo.setDescription(agency.getDescription());
                agencyInfo.setMembershipStatus(activeMembership.get().getStatus()); // Same enum
                agencyInfo.setPosition(activeMembership.get().getPosition());
                dto.setCurrentAgency(agencyInfo);
            }
        }
        
        return dto;
    }

    // Convert from UserDTO.Create to User entity
    public User toEntity(UserDTO.Create createDto) {
        if (createDto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(createDto.getEmail());
        
        // Handle profile creation if provided
        if (createDto.getProfile() != null) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            updateProfileFromDTO(createDto.getProfile(), profile);
            user.setUserProfile(profile);
        }
        
        return user;
    }

    // Update entity from UserDTO.Update
    public void updateEntity(UserDTO.Update updateDto, User user) {
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

    // Main profile update method
    public void updateUserProfile(UserProfileDTO profileDto, User user) {
        if (profileDto == null || user == null) {
            return;
        }
        
        UserProfile profile = user.getOrCreateProfile();
        updateProfileFromDTO(profileDto, profile);
        user.setUserProfile(profile);
    }

    // ===== PROFILE MAPPING METHODS =====

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

    // Helper method to update profile fields from DTO
    public void updateProfileFromDTO(UserProfileDTO profileDto, UserProfile profile) {
        if (profileDto == null || profile == null) {
            return;
        }
        
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
    }

    // ===== AGENCY MAPPING METHODS =====

    public AgencyDTO toAgencyDTO(Agency agency) {
        if (agency == null) {
            return null;
        }
        
        AgencyDTO dto = new AgencyDTO();
        dto.setId(agency.getId());
        dto.setName(agency.getName());
        dto.setDescription(agency.getDescription());
        dto.setLogo(agency.getLogo());
        dto.setContactInfo(agency.getContactInfo());
        dto.setCreatedAt(agency.getCreatedAt());
        
        // Map admin
        if (agency.getAdmin() != null) {
            UserDTO adminDto = toDTO(agency.getAdmin());
            dto.setAdmin(adminDto);
        }
        
        return dto;
    }

    public AgencyMembershipDTO toMembershipDTO(AgencyMembership membership) {
        if (membership == null) {
            return null;
        }
        
        AgencyMembershipDTO dto = new AgencyMembershipDTO();
        dto.setId(membership.getId());
        dto.setStatus(membership.getStatus()); // Same enum type
        dto.setPosition(membership.getPosition());
        dto.setJoinDate(membership.getJoinDate());
        dto.setCreatedAt(membership.getCreatedAt());
        
        // Map user
        if (membership.getUser() != null) {
            UserDTO userDto = toDTO(membership.getUser());
            dto.setUser(userDto);
        }
        
        // Map agency
        if (membership.getAgency() != null) {
            AgencyDTO agencyDto = toAgencyDTO(membership.getAgency());
            dto.setAgency(agencyDto);
        }
        
        return dto;
    }

    // Map AgencyDTO.Create to Agency entity
    public Agency toAgencyEntity(AgencyDTO.Create createDto, User admin) {
        if (createDto == null || admin == null) {
            return null;
        }
        
        Agency agency = new Agency();
        agency.setName(createDto.getName());
        agency.setDescription(createDto.getDescription());
        agency.setLogo(createDto.getLogo());
        agency.setContactInfo(createDto.getContactInfo());
        agency.setAdmin(admin);
        
        return agency;
    }

    // ===== BULK MAPPING METHODS =====

    public List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AgencyDTO> toAgencyDTOList(List<Agency> agencies) {
        if (agencies == null) {
            return List.of();
        }
        
        return agencies.stream()
                .map(this::toAgencyDTO)
                .collect(Collectors.toList());
    }
    
    public List<AgencyMembershipDTO> toMembershipDTOList(List<AgencyMembership> memberships) {
        if (memberships == null) {
            return List.of();
        }
        
        return memberships.stream()
                .map(this::toMembershipDTO)
                .collect(Collectors.toList());
    }

    // ===== HELPER METHODS =====

    private List<String> mapRoles(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toList());
    }
    
    // Convert from UserDTO to User entity (for updates)
    public void updateFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }
        
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            user.setEmail(userDTO.getEmail());
        }
        
        if (userDTO.getProfile() != null) {
            updateUserProfile(userDTO.getProfile(), user);
        }
    }
}