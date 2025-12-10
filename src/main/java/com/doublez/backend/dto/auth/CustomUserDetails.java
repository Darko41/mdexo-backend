package com.doublez.backend.dto.auth;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.doublez.backend.entity.user.User;

public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;
    
    // Security-related fields
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;
    private final LocalDateTime passwordChangedAt;
    private final LocalDateTime accountLockedUntil;
    private final Integer failedLoginAttempts;

    // PRIMARY CONSTRUCTOR - from User entity
    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        
        // Security validations
        this.accountNonExpired = isAccountNonExpiredInternal(user);
        this.accountNonLocked = isAccountNonLockedInternal(user);
        this.credentialsNonExpired = isCredentialsNonExpiredInternal(user);
        this.enabled = isEnabledInternal(user);
        this.passwordChangedAt = user.getPasswordChangedAt(); // You'll need to add this field to User entity
        this.accountLockedUntil = user.getAccountLockedUntil(); // You'll need to add this field to User entity
        this.failedLoginAttempts = user.getFailedLoginAttempts(); // You'll need to add this field to User entity
    }

    // ALTERNATIVE CONSTRUCTOR - for manual creation (for testing or special cases)
    public CustomUserDetails(Long id, String email, String password,
            Collection<? extends GrantedAuthority> authorities, 
            boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, 
            boolean accountNonLocked) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.user = null;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.passwordChangedAt = null;
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    // ========================
    // SECURITY VALIDATION METHODS
    // ========================

    private boolean isAccountNonExpiredInternal(User user) {
        // Check if account has expired based on creation date or specific expiry date
        if (user.getAccountExpiryDate() != null) {
            return LocalDateTime.now().isBefore(user.getAccountExpiryDate());
        }
        
        // Default: accounts don't expire unless specifically set
        return true;
    }

    private boolean isAccountNonLockedInternal(User user) {
        // Check if account is temporarily locked
        if (user.getAccountLockedUntil() != null) {
            return LocalDateTime.now().isAfter(user.getAccountLockedUntil());
        }
        
        // Check if account is permanently locked
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            return false;
        }
        
        // Check failed login attempts (lock after 5 failed attempts)
        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() >= 5) {
            return false;
        }
        
        return true;
    }

    private boolean isCredentialsNonExpiredInternal(User user) {
        // Password expiry check (90 days)
        if (user.getPasswordChangedAt() != null) {
            LocalDateTime passwordExpiryDate = user.getPasswordChangedAt().plusDays(90);
            return LocalDateTime.now().isBefore(passwordExpiryDate);
        }
        
        // If never changed password, force change on first login
        return user.getPasswordChangedAt() != null;
    }

    private boolean isEnabledInternal(User user) {
        // Check if user is active and email is verified
        boolean isActive = user.getIsActive() == null || Boolean.TRUE.equals(user.getIsActive());
        boolean isEmailVerified = Boolean.TRUE.equals(user.getEmailVerified());
        boolean isNotSuspended = !Boolean.TRUE.equals(user.getSuspended());
        
        return isActive && isEmailVerified && isNotSuspended;
    }

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    public boolean hasAnyRole(String... roles) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> Arrays.asList(roles).contains(role));
    }

    public boolean isAgencyAdmin() {
        return hasRole("ROLE_AGENCY");
    }

    public boolean isInvestor() {
        return hasRole("ROLE_INVESTOR");
    }

    public boolean isOwner() {
        return hasRole("ROLE_OWNER");
    }

    public boolean isContractor() {
        return hasRole("ROLE_CONTRACTOR");
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isTrialActive() {
        return user != null && user.isInTrialPeriod();
    }

    public boolean hasSufficientCredits(Integer requiredCredits) {
        if (user == null || user.getUserCredit() == null) {
            return false;
        }
        return user.getUserCredit().hasSufficientCredits(requiredCredits);
    }

    public boolean canPerformPremiumAction() {
        // Check if user has required role and isn't restricted
        return isEnabled() && 
               (isAgencyAdmin() || isInvestor() || isOwner() || isContractor()) &&
               accountNonLocked;
    }

    // ========================
    // SECURITY STATUS METHODS
    // ========================

    public String getSecurityStatus() {
        if (!enabled) return "ACCOUNT_DISABLED";
        if (!accountNonLocked) return "ACCOUNT_LOCKED";
        if (!credentialsNonExpired) return "PASSWORD_EXPIRED";
        if (!accountNonExpired) return "ACCOUNT_EXPIRED";
        return "ACTIVE";
    }

    public boolean isSecurityCompromised() {
        return !enabled || !accountNonLocked || !credentialsNonExpired || !accountNonExpired;
    }

    public Long getDaysUntilPasswordExpiry() {
        if (passwordChangedAt == null) return null;
        
        LocalDateTime expiryDate = passwordChangedAt.plusDays(90);
        return ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
    }

    public boolean isPasswordExpiryWarning() {
        Long daysUntilExpiry = getDaysUntilPasswordExpiry();
        return daysUntilExpiry != null && daysUntilExpiry <= 7 && daysUntilExpiry > 0;
    }

    // ========================
    // GETTERS
    // ========================

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ========================
    // EQUALS & HASHCODE
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", authorities=" + authorities +
                ", securityStatus='" + getSecurityStatus() + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}