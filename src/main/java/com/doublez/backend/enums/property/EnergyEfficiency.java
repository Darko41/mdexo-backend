package com.doublez.backend.enums.property;

public enum EnergyEfficiency {
    A_PLUS("A+", "Najviši standard"),
    A("A", "Veoma efikasno"),
    B("B", "Efikasno"),
    C("C", "Dobro"),
    D("D", "Zadovoljavajuće"),
    E("E", "Loše"),
    F("F", "Veoma loše"),
    G("G", "Izuzetno loše"),
    NOT_RATED("Nije ocenjeno", "Nema energetski sertifikat");
    
    private final String displayName;
    private final String description;
    
    EnergyEfficiency(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
