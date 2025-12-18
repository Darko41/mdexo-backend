package com.doublez.backend.enums.property;

public enum HeatingType {
    CENTRAL("Centralno grejanje"),
    ELECTRIC("Električno grejanje"),
    GAS("Grejanje na gas"),
    HEAT_PUMP("Toplotna pumpa"),
    SOLAR("Solarno grejanje"),
    WOOD_PELLET("Pelet"),
    OIL("Lož ulje"),
    COAL("Ugalj"),
    NONE("Bez grejanja"),
    OTHER("Ostalo");

    private final String displayName;

    HeatingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
