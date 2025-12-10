package com.doublez.backend.enums.service;

import java.util.List;

// TODO DODAJ ARCHITECT

public enum ServiceCategory {
    // COMPLETE PROJECTS - "Ključ u ruke"
    COMPLETE_RENOVATION("Kompletno renoviranje - ključ u ruke"),
    COMPLETE_CONSTRUCTION("Kompletna izgradnja - ključ u ruke"),

    // Construction & Major Works
    CONSTRUCTION("Građevinarstvo"),
    CONSTRUCTION_NEW("Novogradnja"),
    DEMOLITION("Rušenje objekata"),
    STRUCTURAL_WORK("Konstruktivni radovi"),
    
    // Electrical & Systems
    ELECTRICAL("Električarski radovi"),
    ELECTRICAL_INSTALLATION("Instalacija električnih sistema"),
    ELECTRICAL_REPAIR("Popravka električnih instalacija"),
    SMART_HOME("Pametna kuća - instalacije"),
    
    // Plumbing & HVAC
    PLUMBING("Vodovodski radovi"),
    PLUMBING_INSTALLATION("Instalacija vodovoda"),
    PLUMBING_REPAIR("Popravka vodovodnih instalacija"),
    HEATING_INSTALLATION("Instalacija grejanja"),
    AIR_CONDITIONING("Klima uređaji"),
    
    // Walls & Surfaces
    PLASTERING("Gipsarski radovi"),
    DRYWALL("Montaženi zidovi - gips ploče"),
    WALLPAPERING("Tapetiranje"),
    
    // Painting - Differentiated Types
    PAINTING_INTERIOR("Farbanje enterijera"),
    PAINTING_EXTERIOR("Farbanje fasada"),
    PAINTING_DECORATIVE("Dekorativno farbanje"),
    PAINTING_INDUSTRIAL("Industrijsko farbanje"),
    
    // Flooring - Detailed Categories
    FLOORING_WOOD("Drveni podovi"),
    FLOORING_PARQUET("Parket - postavljanje i renoviranje"),
    FLOORING_LAMINATE("Laminat - postavljanje"),
    FLOORING_CERAMIC("Keramičke pločice"),
    FLOORING_MARBLE("Mermer i prirodni kamen"),
    FLOORING_VINYL("Vinil i PVC podovi"),
    FLOORING_CARPET("Teppih - postavljanje"),
    
    // Tiling & Stone Work
    TILING_WALL("Oblaganje zidova pločicama"),
    TILING_FLOOR("Oblaganje podova pločicama"),
    TILING_MOSAIC("Mozaik radovi"),
    STONE_WORK("Radovi sa prirodnim kamenom"),
    
    // Roofing
    ROOFING("Krovopokrivački radovi"),
    ROOFING_TILE("Crep krovovi"),
    ROOFING_SHEET("Lim i sendvič krovovi"),
    ROOFING_FLAT("Ravni krovovi"),
    ROOF_REPAIR("Popravka krovova"),
    
    // Carpentry & Woodwork
    CARPENTRY("Stolarija"),
    CARPENTRY_FURNITURE("Nameštaj po meri"),
    CARPENTRY_DOORS("Vrata - izrada i ugradnja"),
    CARPENTRY_WINDOWS("Prozori - izrada i ugradnja"),
    CARPENTRY_KITCHEN("Kuhinje po meri"),
    
    // Metalwork
    METALWORK("Bravarija"),
    METALWORK_GATES("Kapije i ograde"),
    METALWORK_RAILINGS("Ograda i rukohvati"),
    METALWORK_STRUCTURAL("Konstrukcije od čelika"),
    
    // Landscaping & Outdoor
    LANDSCAPING("Hortikultura i uređenje okoline"),
    LANDSCAPING_GARDEN("Uređenje bašta"),
    LANDSCAPING_IRRIGATION("Sistemi za navodnjavanje"),
    LANDSCAPING_PAVING("Popločavanje"),
    FENCE_INSTALLATION("Ograda - postavljanje"),
    
    // Cleaning & Maintenance
    CLEANING_POST_CONSTRUCTION("Čišćenje nakon gradnje"),
    CLEANING_RESIDENTIAL("Kućno čišćenje"),
    CLEANING_COMMERCIAL("Komercijalno čišćenje"),
    CLEANING_WINDOWS("Čišćenje prozora"),
    
    // Moving & Logistics
    MOVING("Selidbe"),
    MOVING_RESIDENTIAL("Stambene selidbe"),
    MOVING_COMMERCIAL("Komercijalne selidbe"),
    MOVING_HEAVY("Transport teške opreme"),
    
    // Specialized Services
    INSULATION("Termo i zvučna izolacija"),
    WATERPROOFING("Hidroizolacija"),
    PEST_CONTROL("Dezinsekcija i deratizacija"),
    FLOOR_SANDING("Brušenje podova"),
    
    // Other
    CONSULTING("Stručno savetovanje"),
    PROJECT_MANAGEMENT("Upravljanje projektima"),
    PARTIAL_RENOVATION("Delimično renoviranje"), 
    OTHER("Ostalo");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public static List<ServiceCategory> getMainCategories() {
        return List.of(
            COMPLETE_RENOVATION,
            COMPLETE_CONSTRUCTION,
            ELECTRICAL, 
            PLUMBING,
            PLASTERING, 
            PAINTING_INTERIOR, 
            FLOORING_WOOD,
            ROOFING, 
            CARPENTRY, 
            LANDSCAPING, 
            CLEANING_POST_CONSTRUCTION
        );
    }
}


