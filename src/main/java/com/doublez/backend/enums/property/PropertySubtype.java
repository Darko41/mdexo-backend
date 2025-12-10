package com.doublez.backend.enums.property;

public enum PropertySubtype {
    
	// HOUSE subtypes
	HOUSE_DETACHED("Samostojeća kuća", PropertyType.HOUSE),
    HOUSE_SEMI_DETACHED("Dvojna kuća", PropertyType.HOUSE),
    HOUSE_ROW("Kuća u nizu", PropertyType.HOUSE),
    HOUSE_VILLA("Vila", PropertyType.HOUSE),
    HOUSE_COUNTRY("Seoska kuća", PropertyType.HOUSE),
    
    // APARTMENT subtypes
    APARTMENT_STUDIO("Garsonjera", PropertyType.APARTMENT),
    APARTMENT_LOFT("Potkrovlje", PropertyType.APARTMENT),          
    APARTMENT_DUPLEX("Duplex stan", PropertyType.APARTMENT),
    APARTMENT_TRIPLEX("Triplex stan", PropertyType.APARTMENT),
    APARTMENT_PENTHOUSE("Penthouse", PropertyType.APARTMENT),
    APARTMENT_GROUND_FLOOR("Stan u prizemlju", PropertyType.APARTMENT),
    APARTMENT_STANDARD("Standardan stan", PropertyType.APARTMENT), 
    
    // VACATION subtypes - works for both sale and rent
    VACATION_HOUSE("Vikend kuća", PropertyType.VACATION),
    VACATION_APARTMENT("Turistički stan", PropertyType.VACATION),
    MOUNTAIN_HOUSE("Planinska kuća", PropertyType.VACATION),
    LAKE_HOUSE("Kuća na jezeru", PropertyType.VACATION),
    COTTAGE("Koliba", PropertyType.VACATION),
    TOURIST_COMPLEX("Turističko naselje", PropertyType.VACATION), 
    HUNTING_LODGE("Lovačka kuća", PropertyType.VACATION),        
    
    // COMMERCIAL/OFFICE subtypes - Business spaces
    OFFICE_SPACE("Kancelarijski prostor", PropertyType.OFFICE_SPACE),
    OFFICE_SUITE("Poslovni suite", PropertyType.OFFICE_SPACE),
    COWORKING_SPACE("Coworking prostor", PropertyType.OFFICE_SPACE),
    BUSINESS_CENTER("Biznis centar", PropertyType.OFFICE_SPACE),
    
    // COMMERCIAL subtypes - Business premises
    COMMERCIAL_RESTAURANT("Restoran", PropertyType.COMMERCIAL),
    COMMERCIAL_CAFE("Kafić", PropertyType.COMMERCIAL),
    COMMERCIAL_BAKERY("Pekara", PropertyType.COMMERCIAL),
    COMMERCIAL_STORE("Prodavnica", PropertyType.COMMERCIAL),
    COMMERCIAL_HAIR_SALON("Frizerski salon", PropertyType.COMMERCIAL),
    COMMERCIAL_PHARMACY("Apoteka", PropertyType.COMMERCIAL),
    COMMERCIAL_SHOWROOM("Izložbeni prostor", PropertyType.COMMERCIAL),
    COMMERCIAL_WORKSHOP("Radnja", PropertyType.COMMERCIAL),
    COMMERCIAL_GYM("Teren za trening", PropertyType.COMMERCIAL),
    COMMERCIAL_HOTEL("Hotel", PropertyType.COMMERCIAL),
    COMMERCIAL_PRINTING_HOUSE("Štamparija", PropertyType.COMMERCIAL),
    COMMERCIAL_AUTO_REPAIR("Auto servis", PropertyType.COMMERCIAL),
    
    // MEDICAL subtypes - Separate category for better filtering
    COMMERCIAL_MEDICAL_CLINIC("Medicinska ordinacija", PropertyType.COMMERCIAL),
    COMMERCIAL_DENTAL_CLINIC("Stomatološka ordinacija", PropertyType.COMMERCIAL),
    COMMERCIAL_VETERINARY_CLINIC("Veterinarska stanica", PropertyType.COMMERCIAL),
    COMMERCIAL_PHYSIOTHERAPY("Fizioterapija", PropertyType.COMMERCIAL),
    COMMERCIAL_OPTICAL_SHOP("Optičarski salon", PropertyType.COMMERCIAL),
    COMMERCIAL_WELLNESS_CENTER("Wellness centar", PropertyType.COMMERCIAL),
    
    // LAND subtypes
    LAND_AGRICULTURAL("Poljoprivredno zemljište", PropertyType.LAND),
    LAND_RESIDENTIAL("Građevinsko zemljište", PropertyType.LAND),
    LAND_COMMERCIAL("Komercijalno zemljište", PropertyType.LAND),
    LAND_FOREST("Šumsko zemljište", PropertyType.LAND),
    LAND_RECREATIONAL("Rekreaciono zemljište", PropertyType.LAND);
    
    private final String displayName;
    private final PropertyType parentType;
    
    PropertySubtype(String displayName, PropertyType parentType) {
        this.displayName = displayName;
        this.parentType = parentType;
    }
    
    public String getDisplayName() { return displayName; }
    public PropertyType getParentType() { return parentType; }
}
