package com.doublez.backend.enums.property;

public enum PropertySubtype {

    // HOUSE
    HOUSE_DETACHED("Samostojeća kuća", PropertyType.HOUSE),
    HOUSE_SEMI_DETACHED("Dvojna kuća", PropertyType.HOUSE),
    HOUSE_ROW("Kuća u nizu", PropertyType.HOUSE),
    HOUSE_VILLA("Vila", PropertyType.HOUSE),
    HOUSE_COUNTRY("Seosko domaćinstvo", PropertyType.HOUSE),

    // APARTMENT
    APARTMENT_STUDIO("Garsonjera", PropertyType.APARTMENT),
    APARTMENT_STANDARD("Standardan stan", PropertyType.APARTMENT),
    APARTMENT_DUPLEX("Duplex", PropertyType.APARTMENT),
    APARTMENT_PENTHOUSE("Penthouse", PropertyType.APARTMENT),

    // VACATION
    VACATION_HOUSE("Vikend kuća", PropertyType.VACATION),
    MOUNTAIN_HOUSE("Planinska kuća", PropertyType.VACATION),
    LAKE_HOUSE("Kuća na jezeru", PropertyType.VACATION),

    // COMMERCIAL
    COMMERCIAL_STORE("Prodavnica", PropertyType.COMMERCIAL),
    COMMERCIAL_RESTAURANT("Restoran", PropertyType.COMMERCIAL),
    COMMERCIAL_HOTEL("Hotel", PropertyType.COMMERCIAL),

    // LAND
    LAND_AGRICULTURAL("Poljoprivredno", PropertyType.LAND),
    LAND_BUILDING("Građevinsko", PropertyType.LAND);

    private final String displayName;
    private final PropertyType parentType;

    PropertySubtype(String displayName, PropertyType parentType) {
        this.displayName = displayName;
        this.parentType = parentType;
    }

    public PropertyType getParentType() {
        return parentType;
    }

    public String getDisplayName() {
        return displayName;
    }
}

