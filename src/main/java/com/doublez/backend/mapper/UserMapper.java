package com.doublez.backend.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.InvestorProfileDTO;
import com.doublez.backend.dto.agent.AgencyInfoDTO;
import com.doublez.backend.dto.user.UserDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.entity.InvestorProfile;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.user.User;
import com.doublez.backend.entity.user.UserProfile;

@Component
public class UserMapper {

    // ===== USER MAPPING =====

    public UserDTO toDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setTier(user.getTier());

        // Map profile
        if (user.getUserProfile() != null) {
            dto.setProfile(toProfileDto(user.getUserProfile()));
        }

        // Map investor profile
        if (user.getInvestorProfile() != null) {
        	dto.setInvestorProfile(user.getOrCreateInvestorProfile() != null ? toInvestorProfileDTO(user.getOrCreateInvestorProfile()) : null);
        }

        if (user.isAgencyAdmin() && user.getOwnedAgencies() != null && !user.getOwnedAgencies().isEmpty()) {
            Agency agency = user.getOwnedAgencies().get(0);
            AgencyInfoDTO agencyInfo = new AgencyInfoDTO();
            agencyInfo.setId(agency.getId());
            agencyInfo.setName(agency.getName());
            agencyInfo.setDescription(agency.getDescription());
            dto.setCurrentAgency(agencyInfo);
        }

        return dto;
    }

    public User toEntity(UserDTO.Create createDto) {
        if (createDto == null) return null;

        User user = new User();
        user.setEmail(createDto.getEmail());
        user.setTier(createDto.getTier());

        if (createDto.getProfile() != null) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            updateProfileFromDTO(createDto.getProfile(), profile);
            user.setUserProfile(profile);
        }

        return user;
    }

    public void updateEntity(UserDTO.Update updateDto, User user) {
        if (updateDto == null || user == null) return;

        if (updateDto.getEmail() != null && !updateDto.getEmail().isEmpty()) {
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getProfile() != null) {
            updateUserProfile(updateDto.getProfile(), user);
        }
    }

    // ===== PROFILE MAPPING =====

    public UserProfileDTO toProfileDto(UserProfile profile) {
        if (profile == null) return null;

        return new UserProfileDTO(
            profile.getFirstName(),
            profile.getLastName(),
            profile.getPhone(),
            profile.getBio()
        );
    }

    public void updateUserProfile(UserProfileDTO profileDto, User user) {
        if (profileDto == null || user == null) return;

        UserProfile profile = user.getOrCreateProfile();
        updateProfileFromDTO(profileDto, profile);
        user.setUserProfile(profile);
    }

    public void updateProfileFromDTO(UserProfileDTO profileDto, UserProfile profile) {
        if (profileDto == null || profile == null) return;

        if (profileDto.getFirstName() != null) profile.setFirstName(profileDto.getFirstName());
        if (profileDto.getLastName() != null) profile.setLastName(profileDto.getLastName());
        if (profileDto.getPhone() != null) profile.setPhone(profileDto.getPhone());
        if (profileDto.getBio() != null) profile.setBio(profileDto.getBio());
    }

    // ===== INVESTOR PROFILE =====

    public InvestorProfileDTO toInvestorProfileDTO(InvestorProfile profile) {
        if (profile == null) return null;

        return new InvestorProfileDTO(
            profile.getCompanyName(),
            profile.getPib(),
            profile.getMb(),
            profile.getWebsite(),
            profile.getContactPerson(),
            profile.getPhoneNumber(),
            profile.getInvestorType(),
            profile.getYearsInBusiness(),
            profile.getPortfolioSize(),
            profile.getInvestmentFocus(),
            profile.getPreferredLocations(),
            profile.getMinInvestmentAmount(),
            profile.getMaxInvestmentAmount()
        );
    }

    public void updateInvestorProfileFromDTO(InvestorProfileDTO profileDto, InvestorProfile profile) {
        if (profileDto == null || profile == null) return;

        if (profileDto.getCompanyName() != null) profile.setCompanyName(profileDto.getCompanyName());
        if (profileDto.getPib() != null) profile.setPib(profileDto.getPib());
        if (profileDto.getMb() != null) profile.setMb(profileDto.getMb());
        if (profileDto.getWebsite() != null) profile.setWebsite(profileDto.getWebsite());
        if (profileDto.getContactPerson() != null) profile.setContactPerson(profileDto.getContactPerson());
        if (profileDto.getPhoneNumber() != null) profile.setPhoneNumber(profileDto.getPhoneNumber());
        if (profileDto.getInvestorType() != null) profile.setInvestorType(profileDto.getInvestorType());
        if (profileDto.getYearsInBusiness() != null) profile.setYearsInBusiness(profileDto.getYearsInBusiness());
        if (profileDto.getPortfolioSize() != null) profile.setPortfolioSize(profileDto.getPortfolioSize());
        if (profileDto.getInvestmentFocus() != null) profile.setInvestmentFocus(profileDto.getInvestmentFocus());
        if (profileDto.getPreferredLocations() != null) profile.setPreferredLocations(profileDto.getPreferredLocations());
        if (profileDto.getMinInvestmentAmount() != null) profile.setMinInvestmentAmount(profileDto.getMinInvestmentAmount());
        if (profileDto.getMaxInvestmentAmount() != null) profile.setMaxInvestmentAmount(profileDto.getMaxInvestmentAmount());
    }

    // ===== HELPERS =====

    private List<String> mapRoles(List<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream().map(Role::getName).toList();
    }

    public void updateFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) return;

        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getProfile() != null) {
            updateUserProfile(userDTO.getProfile(), user);
        }
    }
}
