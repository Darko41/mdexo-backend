package com.doublez.backend.validation;

import java.util.HashSet;
import java.util.Set;

import com.doublez.backend.enums.warnings.DomainErrorCode;
import com.doublez.backend.enums.warnings.DomainWarningCode;

public class ValidationResult {

    private final Set<DomainErrorCode> errors = new HashSet<>();
    private final Set<DomainWarningCode> warnings = new HashSet<>();

    public void addError(DomainErrorCode code) {
        errors.add(code);
    }

    public void addWarning(DomainWarningCode code) {
        warnings.add(code);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public Set<DomainErrorCode> getErrors() {
        return errors;
    }

    public Set<DomainWarningCode> getWarnings() {
        return warnings;
    }
}

