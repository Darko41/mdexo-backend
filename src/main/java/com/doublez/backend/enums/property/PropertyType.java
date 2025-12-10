package com.doublez.backend.enums.property;

public enum PropertyType {
    HOUSE("Kuća"),
    APARTMENT("Stan"),
    VACATION("Vikend i turistički objekat"),  
    LAND("Zemljište"),
    GARAGE("Garaža"),
    PARKING("Parking mesto"),
    COMMERCIAL("Poslovni prostor - lokal"),         
    OFFICE_SPACE("Kancelarijski prostor"),          
    WAREHOUSE("Skladište"),
    OTHER("Drugo - molimo navedite");
	
	// In Property entity:
	// private String otherPropertyType;  // Required when type is OTHER
    
    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}