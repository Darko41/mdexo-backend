package com.doublez.backend.dto.trial;

import java.time.LocalDateTime;

public class TrialInfoDTO {
    private Boolean trialUsed;
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private Boolean inTrialPeriod;
    private Long daysRemaining;

    // Constructors
    public TrialInfoDTO() {}

    public TrialInfoDTO(Boolean trialUsed, LocalDateTime trialStartDate, LocalDateTime trialEndDate, 
                       Boolean inTrialPeriod, Long daysRemaining) {
        this.trialUsed = trialUsed;
        this.trialStartDate = trialStartDate;
        this.trialEndDate = trialEndDate;
        this.inTrialPeriod = inTrialPeriod;
        this.daysRemaining = daysRemaining;
    }

    // Helper methods
    public boolean isTrialActive() {
        return Boolean.TRUE.equals(inTrialPeriod);
    }

    public boolean isTrialExpired() {
        return Boolean.TRUE.equals(trialUsed) && !Boolean.TRUE.equals(inTrialPeriod);
    }

    // Getters and Setters
    public Boolean getTrialUsed() { return trialUsed; }
    public void setTrialUsed(Boolean trialUsed) { this.trialUsed = trialUsed; }
    public LocalDateTime getTrialStartDate() { return trialStartDate; }
    public void setTrialStartDate(LocalDateTime trialStartDate) { this.trialStartDate = trialStartDate; }
    public LocalDateTime getTrialEndDate() { return trialEndDate; }
    public void setTrialEndDate(LocalDateTime trialEndDate) { this.trialEndDate = trialEndDate; }
    public Boolean getInTrialPeriod() { return inTrialPeriod; }
    public void setInTrialPeriod(Boolean inTrialPeriod) { this.inTrialPeriod = inTrialPeriod; }
    public Long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Long daysRemaining) { this.daysRemaining = daysRemaining; }
}
