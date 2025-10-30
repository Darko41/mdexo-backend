package com.doublez.backend.enums;

public enum PropertyCondition {
	
	NEW_CONSTRUCTION("New Construction"),
    RENOVATED("Renovated"),
    MODERNIZED("Modernized"),
    GOOD("Good Condition"),
    NEEDS_RENOVATION("Needs Renovation"),
    ORIGINAL("Original Condition"),
    LUXURY("Luxury Finish"),
    SHELL("Shell"),
    OTHER("Other");

    private final String displayName;

    PropertyCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
