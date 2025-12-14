package com.doublez.backend.validation;

import com.doublez.backend.entity.realestate.RealEstate;
import com.doublez.backend.enums.property.HeatingType;
import com.doublez.backend.enums.property.OwnershipType;
import com.doublez.backend.enums.warnings.DomainErrorCode;
import com.doublez.backend.enums.warnings.DomainWarningCode;

public class RealEstateValidator {

    public ValidationResult validate(RealEstate re) {
        ValidationResult r = new ValidationResult();

        // ---- ERRORS ----
        if (re.getPrice() != null && re.getPrice().signum() < 0) {
            r.addError(DomainErrorCode.PRICE_NEGATIVE);
        }

        if (re.getSizeInSqMt() != null && re.getSizeInSqMt().signum() <= 0) {
            r.addError(DomainErrorCode.SIZE_INVALID);
        }

        if (re.getHeatingType() == HeatingType.OTHER &&
            isBlank(re.getOtherHeatingTypeDescription())) {
            r.addError(DomainErrorCode.HEATING_OTHER_REQUIRES_DESCRIPTION);
        }

        if (re.getOwnershipType() == OwnershipType.OTHER &&
            isBlank(re.getOtherOwnershipTypeDescription())) {
            r.addError(DomainErrorCode.OWNERSHIP_OTHER_REQUIRES_DESCRIPTION);
        }

        // ---- WARNINGS ----
        if (re.getHeatingType() == null) {
            r.addWarning(DomainWarningCode.NO_HEATING_DEFINED);
        }

        if (re.getWaterSources() == null || re.getWaterSources().isEmpty()) {
            r.addWarning(DomainWarningCode.NO_WATER_SOURCE_DEFINED);
        }

        if (re.getEnergyEfficiency() == null) {
            r.addWarning(DomainWarningCode.ENERGY_EFFICIENCY_MISSING);
        }

        if (re.getFurnitureStatus() == null) {
            r.addWarning(DomainWarningCode.FURNITURE_STATUS_MISSING);
        }

        return r;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}


