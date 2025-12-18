package com.doublez.backend.entity.realestate;

import java.util.Objects;

import com.doublez.backend.enums.property.FeatureCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "property_features")
public class PropertyFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique code for programmatic reference (e.g., "IMMEDIATELY_MOVABLE", "REGISTERED")
     * Must be globally unique across all categories
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Display name shown to users
     * Example: "Terasa", "Uknjižen"
     */
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeatureCategory category;

    /**
     * Display order within category (lower = appears first)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * True = seeded by system (Halooglasi-like)
     * False = user-created
     */
    @Column(nullable = false)
    private boolean systemDefined;

    /**
     * Optional description (admin/internal use)
     */
    @Column(length = 255)
    private String description;

    protected PropertyFeature() {
        // JPA
    }

    private PropertyFeature(
        String code,
        String name,
        FeatureCategory category,
        Integer displayOrder,
        boolean systemDefined,
        String description
    ) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.displayOrder = displayOrder;
        this.systemDefined = systemDefined;
        this.description = description;
    }

    /* ---------- FACTORIES ---------- */

    public static PropertyFeature systemFeature(
        String code,
        String name,
        FeatureCategory category,
        Integer displayOrder
    ) {
        return new PropertyFeature(code, name, category, displayOrder, true, null);
    }

    public static PropertyFeature systemFeature(
        String code,
        String name,
        FeatureCategory category,
        Integer displayOrder,
        String description
    ) {
        return new PropertyFeature(code, name, category, displayOrder, true, description);
    }

    public static PropertyFeature customFeature(
        String name,
        FeatureCategory category,
        Integer displayOrder
    ) {
        String code = generateCodeFromName(name);
        return new PropertyFeature(code, name, category, displayOrder, false, null);
    }

    private static String generateCodeFromName(String name) {
        return name.toUpperCase()
                .replace(" ", "_")
                .replace("Č", "C")
                .replace("Ć", "C")
                .replace("Đ", "DJ")
                .replace("Š", "S")
                .replace("Ž", "Z")
                .replaceAll("[^A-Z0-9_]", "");
    }

    /* ---------- GETTERS ---------- */

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public FeatureCategory getCategory() {
        return category;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public boolean isSystemDefined() {
        return systemDefined;
    }

    public String getDescription() {
        return description;
    }

    /* ---------- SETTERS (for mutable fields) ---------- */

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* ---------- BUSINESS METHODS ---------- */

    public void updateInfo(String name, FeatureCategory category, Integer displayOrder, String description) {
        if (systemDefined) {
            throw new IllegalStateException("Cannot modify system-defined features");
        }
        this.name = name;
        this.category = category;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.description = description;
    }

    /* ---------- EQUALS & HASHCODE ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyFeature that = (PropertyFeature) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "PropertyFeature{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                '}';
    }
}
