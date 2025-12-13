package com.doublez.backend.entity.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.doublez.backend.entity.Role;
import com.doublez.backend.entity.agency.Agency;
import com.doublez.backend.entity.agency.Agent;
import com.doublez.backend.entity.agency.Invitation;
import com.doublez.backend.entity.credit.UserCredit;
import com.doublez.backend.entity.profile.ContractorProfile;
import com.doublez.backend.entity.profile.InvestorProfile;
import com.doublez.backend.entity.profile.OwnerProfile;

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
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Core authentication
    @Email 
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;

    // Personal info (combined - no separate UserProfile)
    private String firstName;
    private String lastName;
    
    @Pattern(regexp = "^[0-9\\s\\-\\/\\(\\)]{5,20}$")
    private String phone;
    
    private String bio;

    // Roles & Relationships
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    // Role-specific profiles (not all users have these)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InvestorProfile investorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OwnerProfile ownerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ContractorProfile contractorProfile;

    // Agency relationship - ONLY for ROLE_AGENCY users
    @OneToOne(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Agency ownedAgency;  // Changed from List<Agency> to single Agency

    // Credit system relationship
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserCredit userCredit;

    // Timestamps (using LocalDateTime)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Trial fields
    @Column(name = "trial_start_date")
    private LocalDateTime trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "trial_used")
    private Boolean trialUsed = false;

    // ========================
    // SECURITY FIELDS (NEW)
    // ========================

    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "suspended")
    private Boolean suspended = false;
    
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @Column(name = "account_expiry_date")
    private LocalDateTime accountExpiryDate;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;
    
    // TIER FIELD FOR NON-AGENCY USERS
    @Enumerated(EnumType.STRING)
    @Column(name = "tier")
    private UserTier tier = UserTier.USER_FREE;
    
    // boosts and features
    @Column(name = "verified_badge_until")
    private LocalDateTime verifiedBadgeUntil;

    @Column(name = "has_verified_badge")
    private Boolean hasVerifiedBadge = false;

    @Column(name = "premium_badge_until")
    private LocalDateTime premiumBadgeUntil;

    @Column(name = "has_premium_badge")
    private Boolean hasPremiumBadge = false;
    
    // A fields to track agency membership
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Agent> agentMemberships = new ArrayList<>();
    
    @OneToMany(mappedBy = "invitedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invitation> sentInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "invitedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invitation> receivedInvitations = new ArrayList<>();

    // ========================
    // CONSTRUCTORS
    // ========================

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.isActive = true;
        this.emailVerified = false;
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
    }

    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = true;
        this.emailVerified = false;
        this.accountLocked = false;
        this.failedLoginAttempts = 0;
    }

    // ========================
    // LIFECYCLE METHODS
    // ========================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.emailVerified == null) this.emailVerified = false;
        if (this.accountLocked == null) this.accountLocked = false;
        if (this.failedLoginAttempts == null) this.failedLoginAttempts = 0;
        if (this.passwordChangedAt == null) this.passwordChangedAt = LocalDateTime.now();
    }

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================
    // SECURITY METHODS
    // ========================
    
    public UserTier getEffectiveTier() {
        if (isAgencyAdmin() && getOwnedAgency().isPresent()) {
            return getOwnedAgency().get().getEffectiveTier();
        }
        return this.tier;
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedUntil = null;
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
        this.lastFailedLoginAt = LocalDateTime.now();
        
        // Auto-lock account after 5 failed attempts for 30 minutes
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public void lockAccountTemporarily(int minutes) {
        this.accountLocked = true;
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    public void lockAccountPermanently() {
        this.accountLocked = true;
        this.accountLockedUntil = null; // null means permanent lock
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.failedLoginAttempts = 0; // Reset failed attempts on password change
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void suspendAccount() {
        this.suspended = true;
    }

    public void unsuspendAccount() {
        this.suspended = false;
    }

    public void deactivateAccount() {
        this.isActive = false;
    }

    public void activateAccount() {
        this.isActive = true;
    }

    // Security status checks
    public boolean isAccountNonExpired() {
        if (this.accountExpiryDate != null) {
            return LocalDateTime.now().isBefore(this.accountExpiryDate);
        }
        return true;
    }

    public boolean isAccountNonLocked() {
        if (Boolean.TRUE.equals(this.accountLocked)) {
            // Check if temporary lock has expired
            if (this.accountLockedUntil != null && LocalDateTime.now().isAfter(this.accountLockedUntil)) {
                this.unlockAccount(); // Auto-unlock if time has passed
                return true;
            }
            return false; // Account is locked (permanently or temporarily)
        }
        return true;
    }

    public boolean isCredentialsNonExpired() {
        if (this.passwordChangedAt != null) {
            // Password expires after 90 days
            LocalDateTime passwordExpiryDate = this.passwordChangedAt.plusDays(90);
            return LocalDateTime.now().isBefore(passwordExpiryDate);
        }
        return true; // If never changed password, it's not expired
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.isActive) && 
               Boolean.TRUE.equals(this.emailVerified) && 
               !Boolean.TRUE.equals(this.suspended);
    }

    public Long getDaysUntilPasswordExpiry() {
        if (this.passwordChangedAt == null) return null;
        LocalDateTime expiryDate = this.passwordChangedAt.plusDays(90);
        return ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
    }

    public boolean isPasswordExpiryWarning() {
        Long daysUntilExpiry = getDaysUntilPasswordExpiry();
        return daysUntilExpiry != null && daysUntilExpiry <= 7 && daysUntilExpiry > 0;
    }

    // ========================
    // BUSINESS METHODS
    // ========================

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isAgencyAdmin() {
        return hasRole("ROLE_AGENCY");
    }

    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        if (roles == null) return false;
        return roles.stream()
                .map(Role::getName)
                .anyMatch(role -> Arrays.asList(roleNames).contains(role));
    }

    // Investor logic
    public boolean isInvestor() {
        return hasRole("ROLE_INVESTOR");
    }

    public boolean isOwner() {
        return hasRole("ROLE_OWNER");
    }

    public boolean isContractor() {
        return hasRole("ROLE_CONTRACTOR");
    }

    public boolean isBusiness() {
        return hasRole("ROLE_BUSINESS");
    }

    public boolean isAgencyUser() {
        return isAgencyAdmin() && ownedAgency != null;
    }

    // Profile helpers
    public InvestorProfile getOrCreateInvestorProfile() {
        if (this.investorProfile == null) {
            this.investorProfile = new InvestorProfile(this);
        }
        return this.investorProfile;
    }

    public OwnerProfile getOrCreateOwnerProfile() {
        if (this.ownerProfile == null) {
            this.ownerProfile = new OwnerProfile(this);
        }
        return this.ownerProfile;
    }

    public ContractorProfile getOrCreateContractorProfile() {
        if (this.contractorProfile == null) {
            this.contractorProfile = new ContractorProfile(this);
        }
        return this.contractorProfile;
    }

    // Trial management methods
    public boolean isInTrialPeriod() {
        if (!Boolean.TRUE.equals(trialUsed) || trialEndDate == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(trialEndDate);
    }

    public boolean isTrialExpired() {
        return Boolean.TRUE.equals(trialUsed) &&
               trialEndDate != null &&
               LocalDateTime.now().isAfter(trialEndDate);
    }

    public long getTrialDaysRemaining() {
        if (!isInTrialPeriod()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), trialEndDate);
    }

    public void startTrial(int trialDays) {
        this.trialUsed = true;
        this.trialStartDate = LocalDateTime.now();
        this.trialEndDate = LocalDateTime.now().plusDays(trialDays);
    }
    
    public boolean hasActiveVerifiedBadge() {
        return Boolean.TRUE.equals(hasVerifiedBadge) && verifiedBadgeUntil != null && 
               LocalDateTime.now().isBefore(verifiedBadgeUntil);
    }

    public boolean hasActivePremiumBadge() {
        return Boolean.TRUE.equals(hasPremiumBadge) && premiumBadgeUntil != null && 
               LocalDateTime.now().isBefore(premiumBadgeUntil);
    }

    public void checkAndResetExpiredBadges() {
        LocalDateTime now = LocalDateTime.now();
        if (verifiedBadgeUntil != null && now.isAfter(verifiedBadgeUntil)) {
            verifiedBadgeUntil = null;
            hasVerifiedBadge = false;
        }
        if (premiumBadgeUntil != null && now.isAfter(premiumBadgeUntil)) {
            premiumBadgeUntil = null;
            hasPremiumBadge = false;
        }
    }

    // Credit helper methods
    public boolean hasSufficientCredits(Integer requiredCredits) {
        if (this.userCredit == null) return false;
        return this.userCredit.hasSufficientCredits(requiredCredits);
    }

    public Integer getCurrentCreditBalance() {
        return this.userCredit != null ? this.userCredit.getCurrentBalance() : 0;
    }
    
    // Agency membership helper methods
    public boolean isAgentInAgency(Long agencyId) {
        if (agentMemberships == null) return false;
        return agentMemberships.stream()
                .filter(Agent::getIsActive)
                .anyMatch(agent -> agent.getAgency().getId().equals(agencyId));
    }

    public List<Agent> getActiveAgentMemberships() {
        if (agentMemberships == null) return new ArrayList<>();
        return agentMemberships.stream()
                .filter(Agent::getIsActive)
                .collect(Collectors.toList());
    }

    public Optional<Agent> getAgentForAgency(Long agencyId) {
        if (agentMemberships == null) return Optional.empty();
        return agentMemberships.stream()
                .filter(Agent::getIsActive)
                .filter(agent -> agent.getAgency().getId().equals(agencyId))
                .findFirst();
    }
    
    // ========================
    // GETTERS AND SETTERS
    // ========================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public InvestorProfile getInvestorProfile() { return investorProfile; }
    public void setInvestorProfile(InvestorProfile investorProfile) { this.investorProfile = investorProfile; }

    public OwnerProfile getOwnerProfile() { return ownerProfile; }
    public void setOwnerProfile(OwnerProfile ownerProfile) { this.ownerProfile = ownerProfile; }

    public ContractorProfile getContractorProfile() { return contractorProfile; }
    public void setContractorProfile(ContractorProfile contractorProfile) { this.contractorProfile = contractorProfile; }

    public Optional<Agency> getOwnedAgency() {return Optional.ofNullable(this.ownedAgency); }
    public void setOwnedAgency(Agency ownedAgency) { this.ownedAgency = ownedAgency; }

	public UserCredit getUserCredit() { return userCredit; }
    public void setUserCredit(UserCredit userCredit) { this.userCredit = userCredit; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDateTime trialStartDate) { this.trialStartDate = trialStartDate; }

    public LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }

    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }

    // Security field getters and setters
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getSuspended() { return suspended; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public LocalDateTime getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }

    public LocalDateTime getAccountExpiryDate() { return accountExpiryDate; }
    public void setAccountExpiryDate(LocalDateTime accountExpiryDate) { this.accountExpiryDate = accountExpiryDate; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getLastFailedLoginAt() { return lastFailedLoginAt; }
    public void setLastFailedLoginAt(LocalDateTime lastFailedLoginAt) { this.lastFailedLoginAt = lastFailedLoginAt; }

    public UserTier getTier() { return tier; }
	public void setTier(UserTier tier) { this.tier = tier; }

	public LocalDateTime getVerifiedBadgeUntil() { return verifiedBadgeUntil; }
	public void setVerifiedBadgeUntil(LocalDateTime verifiedBadgeUntil) { this.verifiedBadgeUntil = verifiedBadgeUntil; }

	public Boolean getHasVerifiedBadge() { return hasVerifiedBadge; }
	public void setHasVerifiedBadge(Boolean hasVerifiedBadge) { this.hasVerifiedBadge = hasVerifiedBadge; }

	public LocalDateTime getPremiumBadgeUntil() { return premiumBadgeUntil; }
	public void setPremiumBadgeUntil(LocalDateTime premiumBadgeUntil) { this.premiumBadgeUntil = premiumBadgeUntil; }

	public Boolean getHasPremiumBadge() { return hasPremiumBadge; }
	public void setHasPremiumBadge(Boolean hasPremiumBadge) { this.hasPremiumBadge = hasPremiumBadge; }
	
	public List<Agent> getAgentMemberships() { return agentMemberships; }
	public void setAgentMemberships(List<Agent> agentMemberships) { this.agentMemberships = agentMemberships; }

	public List<Invitation> getSentInvitations() { return sentInvitations; }
	public void setSentInvitations(List<Invitation> sentInvitations) { this.sentInvitations = sentInvitations; }

	public List<Invitation> getReceivedInvitations() { return receivedInvitations; }
	public void setReceivedInvitations(List<Invitation> receivedInvitations) { this.receivedInvitations = receivedInvitations; }

	@Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isActive=" + isActive +
                ", emailVerified=" + emailVerified +
                ", accountLocked=" + accountLocked +
                ", roles=" + (roles != null ? roles.stream().map(Role::getName).collect(Collectors.toList()) : "null") +
                '}';
    }
}