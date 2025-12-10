package com.doublez.backend.enums.property;

public enum HeatingType {
    
	CENTRAL("Centralno grejanje"),
    ELECTRIC("Električno grejanje"),
    GAS("Grejanje na gas"),
    HEAT_PUMP("Toplotna pumpa"),
    SOLAR("Grejanje na solarnu energiju"),
    WOOD_PELLET("Grejanje na pelet"),
    OIL("Grejanje naftom"),
    COAL("Grejanje na ugalj"),
    NONE("Bez grejanja"),
    OTHER("Ostalo - molimo opišite");
	
	// In HeatingType entity:  
	// private String otherHeatingTypeDescription;

    private final String displayName;

    HeatingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}