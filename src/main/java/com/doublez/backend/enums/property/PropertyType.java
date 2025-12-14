package com.doublez.backend.enums.property;

public enum PropertyType {

    HOUSE("Kuća"),
    APARTMENT("Stan"),
    VACATION("Vikend / turistički objekat"),
    LAND("Zemljište"),
    GARAGE("Garaža"),
    PARKING("Parking mesto"),
    COMMERCIAL("Poslovni prostor"),
    OFFICE_SPACE("Kancelarijski prostor"),
    WAREHOUSE("Skladište"),
    OTHER("Drugo");

    private final String displayName;

    PropertyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
