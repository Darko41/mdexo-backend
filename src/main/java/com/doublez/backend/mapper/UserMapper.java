package com.doublez.backend.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.doublez.backend.dto.agency.AgencyInfoDTO;
import com.doublez.backend.dto.contractor.ContractorProfileCreateDTO;
import com.doublez.backend.dto.contractor.ContractorProfileResponseDTO;
import com.doublez.backend.dto.contractor.ContractorProfileUpdateDTO;
import com.doublez.backend.dto.investor.InvestorProfileCreateDTO;
import com.doublez.backend.dto.investor.InvestorProfileResponseDTO;
import com.doublez.backend.dto.investor.InvestorProfileUpdateDTO;
import com.doublez.backend.dto.owner.OwnerProfileCreateDTO;
import com.doublez.backend.dto.owner.OwnerProfileResponseDTO;
import com.doublez.backend.dto.owner.OwnerProfileUpdateDTO;
import com.doublez.backend.dto.trial.TrialInfoDTO;
import com.doublez.backend.dto.user.UserCreateDTO;
import com.doublez.backend.dto.user.UserProfileDTO;
import com.doublez.backend.dto.user.UserResponseDTO;
import com.doublez.backend.dto.user.UserUpdateDTO;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.profile.ContractorProfile;
import com.doublez.backend.entity.profile.InvestorProfile;
import com.doublez.backend.entity.profile.OwnerProfile;
import com.doublez.backend.entity.user.User;

@Component
public class UserMapper {

    // ===== USER MAPPING =====

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Map profile (direct from User entity - no more UserProfile)
        UserProfileDTO profileDto = new UserProfileDTO(
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getBio()
        );
        dto.setProfile(profileDto);

        // Map investor profile
        if (user.getInvestorProfile() != null) {
            dto.setInvestorProfile(toInvestorProfileResponseDTO(user.getInvestorProfile()));
        }

        // Map owner profile
        if (user.getOwnerProfile() != null) {
            dto.setOwnerProfile(toOwnerProfileResponseDTO(user.getOwnerProfile()));
        }

        // Map contractor profile
        if (user.getContractorProfile() != null) {
            dto.setContractorProfile(toContractorProfileResponseDTO(user.getContractorProfile()));
        }

        // Map administered agency (only for AGENCY users)
        user.getOwnedAgency().ifPresent(agency -> {
            AgencyInfoDTO agencyInfo = new AgencyInfoDTO();
            agencyInfo.setId(agency.getId());
            agencyInfo.setName(agency.getName());
            agencyInfo.setDescription(agency.getDescription());
            agencyInfo.setLogo(agency.getLogo());
            agencyInfo.setContactEmail(agency.getContactEmail());
            agencyInfo.setContactPhone(agency.getContactPhone());
            agencyInfo.setWebsite(agency.getWebsite());
            agencyInfo.setEffectiveTier(agency.getEffectiveTier());
            agencyInfo.setIsActive(agency.getIsActive());
            dto.setAdministeredAgency(agencyInfo);
        });

        // Map trial info
        TrialInfoDTO trialInfo = new TrialInfoDTO();
        trialInfo.setTrialUsed(user.getTrialUsed());
        trialInfo.setTrialStartDate(user.getTrialStartDate());
        trialInfo.setTrialEndDate(user.getTrialEndDate());
        trialInfo.setInTrialPeriod(user.isInTrialPeriod());
        trialInfo.setDaysRemaining(user.getTrialDaysRemaining());
        dto.setTrialInfo(trialInfo);

        return dto;
    }

    public User toEntityFromCreateDTO(UserCreateDTO createDto) {
        if (createDto == null) return null;

        User user = new User();
        user.setEmail(createDto.getEmail());
        user.setPassword(createDto.getPassword()); // Password should be encoded in service
        
        // Set personal info directly on User entity
        user.setFirstName(createDto.getFirstName());
        user.setLastName(createDto.getLastName());
        user.setPhone(createDto.getPhone());
        user.setBio(createDto.getBio());

        return user;
    }

    public void updateEntityFromUpdateDTO(UserUpdateDTO updateDto, User user) {
        if (updateDto == null || user == null) return;

        // Update personal info directly on User entity
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone());
        }
        if (updateDto.getBio() != null) {
            user.setBio(updateDto.getBio());
        }
        
        user.preUpdate(); // Update timestamp
    }

    // ===== INVESTOR PROFILE MAPPING =====

    public InvestorProfileResponseDTO toInvestorProfileResponseDTO(InvestorProfile profile) {
        if (profile == null) return null;

        InvestorProfileResponseDTO dto = new InvestorProfileResponseDTO();
        dto.setId(profile.getId());
        dto.setCompanyName(profile.getCompanyName());
        dto.setPib(profile.getPib());
        dto.setMb(profile.getMb());
        dto.setWebsite(profile.getWebsite());
        dto.setContactPerson(profile.getContactPerson());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setInvestorType(profile.getInvestorType());
        dto.setYearsInBusiness(profile.getYearsInBusiness());
        dto.setPortfolioSize(profile.getPortfolioSize());
        dto.setInvestmentFocus(profile.getInvestmentFocus());
        dto.setPreferredLocations(profile.getPreferredLocationsList());
        dto.setMinInvestmentAmount(profile.getMinInvestmentAmount());
        dto.setMaxInvestmentAmount(profile.getMaxInvestmentAmount());
        dto.setCompletedInvestments(profile.getCompletedInvestments());
        dto.setActiveInvestments(profile.getActiveInvestments());
        dto.setVerificationStatus(profile.getVerificationStatus());
        dto.setVerifiedAt(profile.getVerifiedAt());

        return dto;
    }

    public InvestorProfile toInvestorProfileEntity(InvestorProfileCreateDTO createDto, User user) {
        if (createDto == null) return null;

        InvestorProfile profile = new InvestorProfile(user);
        updateInvestorProfileFromCreateDTO(createDto, profile);
        return profile;
    }

    public void updateInvestorProfileFromCreateDTO(InvestorProfileCreateDTO createDto, InvestorProfile profile) {
        if (createDto == null || profile == null) return;

        if (createDto.getCompanyName() != null) profile.setCompanyName(createDto.getCompanyName());
        if (createDto.getPib() != null) profile.setPib(createDto.getPib());
        if (createDto.getMb() != null) profile.setMb(createDto.getMb());
        if (createDto.getWebsite() != null) profile.setWebsite(createDto.getWebsite());
        if (createDto.getContactPerson() != null) profile.setContactPerson(createDto.getContactPerson());
        if (createDto.getPhoneNumber() != null) profile.setPhoneNumber(createDto.getPhoneNumber());
        if (createDto.getInvestorType() != null) profile.setInvestorType(createDto.getInvestorType());
        if (createDto.getYearsInBusiness() != null) profile.setYearsInBusiness(createDto.getYearsInBusiness());
        if (createDto.getInvestmentFocus() != null) profile.setInvestmentFocus(createDto.getInvestmentFocus());
        if (createDto.getPreferredLocations() != null) profile.setPreferredLocations(createDto.getPreferredLocations());
        if (createDto.getMinInvestmentAmount() != null) profile.setMinInvestmentAmount(createDto.getMinInvestmentAmount());
        if (createDto.getMaxInvestmentAmount() != null) profile.setMaxInvestmentAmount(createDto.getMaxInvestmentAmount());
    }

    public void updateInvestorProfileFromUpdateDTO(InvestorProfileUpdateDTO updateDto, InvestorProfile profile) {
        if (updateDto == null || profile == null) return;

        if (updateDto.getCompanyName() != null) profile.setCompanyName(updateDto.getCompanyName());
        if (updateDto.getWebsite() != null) profile.setWebsite(updateDto.getWebsite());
        if (updateDto.getContactPerson() != null) profile.setContactPerson(updateDto.getContactPerson());
        if (updateDto.getPhoneNumber() != null) profile.setPhoneNumber(updateDto.getPhoneNumber());
        if (updateDto.getInvestorType() != null) profile.setInvestorType(updateDto.getInvestorType());
        if (updateDto.getYearsInBusiness() != null) profile.setYearsInBusiness(updateDto.getYearsInBusiness());
        if (updateDto.getInvestmentFocus() != null) profile.setInvestmentFocus(updateDto.getInvestmentFocus());
        if (updateDto.getPreferredLocations() != null) profile.setPreferredLocations(updateDto.getPreferredLocations());
        if (updateDto.getMinInvestmentAmount() != null) profile.setMinInvestmentAmount(updateDto.getMinInvestmentAmount());
        if (updateDto.getMaxInvestmentAmount() != null) profile.setMaxInvestmentAmount(updateDto.getMaxInvestmentAmount());
    }

    // ===== OWNER PROFILE MAPPING =====

    public OwnerProfileResponseDTO toOwnerProfileResponseDTO(OwnerProfile profile) {
        if (profile == null) return null;

        OwnerProfileResponseDTO dto = new OwnerProfileResponseDTO();
        dto.setId(profile.getId());
        dto.setPropertyOwnershipDocs(profile.getPropertyOwnershipDocs());
        dto.setOwnershipDocsList(profile.getOwnershipDocsList());
        dto.setIdDocumentNumber(profile.getIdDocumentNumber());
        dto.setIdDocumentType(profile.getIdDocumentType());
        dto.setTaxNumber(profile.getTaxNumber());
        dto.setBankAccountNumber(profile.getBankAccountNumber());
        dto.setPreferredContactMethod(profile.getPreferredContactMethod());
        dto.setContactHours(profile.getContactHours());
        dto.setPropertiesOwned(profile.getPropertiesOwned());
        dto.setPropertiesListed(profile.getPropertiesListed());
        dto.setPropertiesSold(profile.getPropertiesSold());
        dto.setVerificationStatus(profile.getVerificationStatus());
        dto.setVerifiedAt(profile.getVerifiedAt());
        dto.setVerificationNotes(profile.getVerificationNotes());

        return dto;
    }

    public OwnerProfile toOwnerProfileEntity(OwnerProfileCreateDTO createDto, User user) {
        if (createDto == null) return null;

        OwnerProfile profile = new OwnerProfile(user);
        updateOwnerProfileFromCreateDTO(createDto, profile);
        return profile;
    }

    public void updateOwnerProfileFromCreateDTO(OwnerProfileCreateDTO createDto, OwnerProfile profile) {
        if (createDto == null || profile == null) return;

        if (createDto.getPropertyOwnershipDocs() != null) profile.setPropertyOwnershipDocs(createDto.getPropertyOwnershipDocs());
        if (createDto.getIdDocumentNumber() != null) profile.setIdDocumentNumber(createDto.getIdDocumentNumber());
        if (createDto.getIdDocumentType() != null) profile.setIdDocumentType(createDto.getIdDocumentType());
        if (createDto.getTaxNumber() != null) profile.setTaxNumber(createDto.getTaxNumber());
        if (createDto.getBankAccountNumber() != null) profile.setBankAccountNumber(createDto.getBankAccountNumber());
        if (createDto.getPreferredContactMethod() != null) profile.setPreferredContactMethod(createDto.getPreferredContactMethod());
        if (createDto.getContactHours() != null) profile.setContactHours(createDto.getContactHours());
    }

    // ===== CONTRACTOR PROFILE MAPPING =====

    public ContractorProfileResponseDTO toContractorProfileResponseDTO(ContractorProfile profile) {
        if (profile == null) return null;

        ContractorProfileResponseDTO dto = new ContractorProfileResponseDTO();
        dto.setId(profile.getId());
        dto.setCompanyName(profile.getCompanyName());
        dto.setPib(profile.getPib());
        dto.setMb(profile.getMb());
        dto.setWebsite(profile.getWebsite());
        dto.setContactPerson(profile.getContactPerson());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setServiceCategories(profile.getServiceCategoriesList());
        dto.setServiceAreas(profile.getServiceAreasList());
        dto.setYearsExperience(profile.getYearsExperience());
        dto.setPortfolioDescription(profile.getPortfolioDescription());
        dto.setCertifications(profile.getCertifications() != null ? Arrays.asList(profile.getCertifications().split(",")) : new ArrayList<>());
        dto.setInsuranceInfo(profile.getInsuranceInfo());
        dto.setHourlyRate(profile.getHourlyRate());
        dto.setMinProjectSize(profile.getMinProjectSize());
        dto.setIsVisible(profile.getIsVisible());
        dto.setIsFeatured(profile.getIsFeatured());
        dto.setRating(profile.getRating());
        dto.setReviewCount(profile.getReviewCount());
        dto.setCompletedProjects(profile.getCompletedProjects());

        return dto;
    }

    public ContractorProfile toContractorProfileEntity(ContractorProfileCreateDTO createDto, User user) {
        if (createDto == null) return null;

        ContractorProfile profile = new ContractorProfile(user);
        updateContractorProfileFromCreateDTO(createDto, profile);
        return profile;
    }

    public void updateContractorProfileFromCreateDTO(ContractorProfileCreateDTO createDto, ContractorProfile profile) {
        if (createDto == null || profile == null) return;

        if (createDto.getCompanyName() != null) profile.setCompanyName(createDto.getCompanyName());
        if (createDto.getPib() != null) profile.setPib(createDto.getPib());
        if (createDto.getMb() != null) profile.setMb(createDto.getMb());
        if (createDto.getWebsite() != null) profile.setWebsite(createDto.getWebsite());
        if (createDto.getContactPerson() != null) profile.setContactPerson(createDto.getContactPerson());
        if (createDto.getPhoneNumber() != null) profile.setPhoneNumber(createDto.getPhoneNumber());
        if (createDto.getServiceCategories() != null) profile.setServiceCategories(createDto.getServiceCategories());
        if (createDto.getServiceAreas() != null) profile.setServiceAreas(createDto.getServiceAreas());
        if (createDto.getYearsExperience() != null) profile.setYearsExperience(createDto.getYearsExperience());
        if (createDto.getPortfolioDescription() != null) profile.setPortfolioDescription(createDto.getPortfolioDescription());
        if (createDto.getCertifications() != null) profile.setCertifications(createDto.getCertifications());
        if (createDto.getInsuranceInfo() != null) profile.setInsuranceInfo(createDto.getInsuranceInfo());
        if (createDto.getHourlyRate() != null) profile.setHourlyRate(createDto.getHourlyRate());
        if (createDto.getMinProjectSize() != null) profile.setMinProjectSize(createDto.getMinProjectSize());
        if (createDto.getIsVisible() != null) profile.setIsVisible(createDto.getIsVisible());
    }
    
    public void updateOwnerProfileFromUpdateDTO(OwnerProfileUpdateDTO updateDto, OwnerProfile profile) {
        if (updateDto == null || profile == null) return;

        if (updateDto.getPropertyOwnershipDocs() != null) profile.setPropertyOwnershipDocs(updateDto.getPropertyOwnershipDocs());
        if (updateDto.getIdDocumentNumber() != null) profile.setIdDocumentNumber(updateDto.getIdDocumentNumber());
        if (updateDto.getIdDocumentType() != null) profile.setIdDocumentType(updateDto.getIdDocumentType());
        if (updateDto.getTaxNumber() != null) profile.setTaxNumber(updateDto.getTaxNumber());
        if (updateDto.getBankAccountNumber() != null) profile.setBankAccountNumber(updateDto.getBankAccountNumber());
        if (updateDto.getPreferredContactMethod() != null) profile.setPreferredContactMethod(updateDto.getPreferredContactMethod());
        if (updateDto.getContactHours() != null) profile.setContactHours(updateDto.getContactHours());
    }
    
    public void updateContractorProfileFromUpdateDTO(ContractorProfileUpdateDTO updateDto, ContractorProfile profile) {
        if (updateDto == null || profile == null) return;

        if (updateDto.getCompanyName() != null) profile.setCompanyName(updateDto.getCompanyName());
        if (updateDto.getWebsite() != null) profile.setWebsite(updateDto.getWebsite());
        if (updateDto.getContactPerson() != null) profile.setContactPerson(updateDto.getContactPerson());
        if (updateDto.getPhoneNumber() != null) profile.setPhoneNumber(updateDto.getPhoneNumber());
        if (updateDto.getServiceCategories() != null) profile.setServiceCategories(updateDto.getServiceCategories());
        if (updateDto.getServiceAreas() != null) profile.setServiceAreas(updateDto.getServiceAreas());
        if (updateDto.getYearsExperience() != null) profile.setYearsExperience(updateDto.getYearsExperience());
        if (updateDto.getPortfolioDescription() != null) profile.setPortfolioDescription(updateDto.getPortfolioDescription());
        if (updateDto.getCertifications() != null) profile.setCertifications(updateDto.getCertifications());
        if (updateDto.getInsuranceInfo() != null) profile.setInsuranceInfo(updateDto.getInsuranceInfo());
        if (updateDto.getHourlyRate() != null) profile.setHourlyRate(updateDto.getHourlyRate());
        if (updateDto.getMinProjectSize() != null) profile.setMinProjectSize(updateDto.getMinProjectSize());
        if (updateDto.getIsVisible() != null) profile.setIsVisible(updateDto.getIsVisible());
    }

    // ===== HELPERS =====

    private List<String> mapRoles(List<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream().map(Role::getName).toList();
    }
}