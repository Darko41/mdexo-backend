package com.doublez.backend.enums.property;

public enum OwnershipType {
	FREEHOLD("Svojinska", "Puno vlasništvo"),
	LEASEHOLD("Zakup", "Pravo korišćenja na određeno vreme"),
	STATE_OWNED("Državna", "Vlasništvo države"),
	COOPERATIVE("Zadružna", "Zadružno vlasništvo"),
	OTHER("Ostalo", "Drugačiji oblik vlasništva");

	private final String displayName;
	private final String description;

	OwnershipType(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}
}
