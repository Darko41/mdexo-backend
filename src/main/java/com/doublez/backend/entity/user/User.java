package com.doublez.backend.entity.user;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.doublez.backend.entity.InvestorProfile;
import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.enums.UserTier;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User {

    // FIELDS
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Password cannot be null")
    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    // ❌ REMOVED: AgencyMemberships list (agent system removed)

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private UserTier tier = UserTier.FREE_USER;

    public UserTier getTier() {
		return tier;
	}

	public void setTier(UserTier tier) {
		this.tier = tier;
	}

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InvestorProfile investorProfile;

    public InvestorProfile getInvestorProfile() {
		return investorProfile;
	}

	public void setInvestorProfile(InvestorProfile investorProfile) {
		this.investorProfile = investorProfile;
	}

	@Column(name = "trial_start_date")
    private LocalDate trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "trial_used", nullable = false)
    private Boolean trialUsed = false;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agency> ownedAgencies = new ArrayList<>();

    // METHODS

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
    }

    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    // Getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    public UserProfile getOrCreateProfile() {
        if (this.userProfile == null) {
            this.userProfile = new UserProfile(this);
        }
        return this.userProfile;
    }

    public boolean isAdmin() {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        return this.roles.stream()
                .map(Role::getName)
                .anyMatch(role -> Arrays.asList(roleNames).contains(role));
    }

    public boolean isAgencyAdmin() {
    	// Show agency management options
        // Allow agency creation
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals("ROLE_AGENCY_ADMIN"));
    }
    
    public boolean isAgencyUser() {
    	// Apply agency listing limits (200 listings)
        // Show agency-specific features
        return this.tier == UserTier.AGENCY_BASIC ||
               this.tier == UserTier.AGENCY_PREMIUM;
    }

    public Optional<Agency> getOwnedAgency() {
        return this.ownedAgencies.stream().findFirst();
    }

    public List<Agency> getOwnedAgencies() { return ownedAgencies; }
    public void setOwnedAgencies(List<Agency> ownedAgencies) { this.ownedAgencies = ownedAgencies; }

    // TIER HELPERS
    public boolean isInFreeTrial() {
        return this.tier == UserTier.FREE_USER || 
               this.tier == UserTier.FREE_INVESTOR;
    }

    // Investor logic stays untouched
    public boolean isInvestor() {
        return this.tier == UserTier.FREE_INVESTOR ||
               this.tier == UserTier.BASIC_INVESTOR ||
               this.tier == UserTier.PREMIUM_INVESTOR;
    }

    public InvestorProfile getOrCreateInvestorProfile() {
        if (this.investorProfile == null) {
            this.investorProfile = new InvestorProfile(this);
        }
        return this.investorProfile;
    }

    // TRIAL HELPERS
    public LocalDate getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDate trialStartDate) { this.trialStartDate = trialStartDate; }

    public LocalDate getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDate trialEndDate) { this.trialEndDate = trialEndDate; }

    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }

    public boolean isInTrialPeriod() {
        if (!Boolean.TRUE.equals(trialUsed) || trialEndDate == null) {
            return false;
        }
        return !LocalDate.now().isAfter(trialEndDate);
    }

    public boolean isTrialExpired() {
        return Boolean.TRUE.equals(trialUsed) &&
               trialEndDate != null &&
               LocalDate.now().isAfter(trialEndDate);
    }

    public long getTrialDaysRemaining() {
        if (!isInTrialPeriod()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), trialEndDate);
    }
}
