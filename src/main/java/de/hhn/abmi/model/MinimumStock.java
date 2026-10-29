package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/**
 * Mindestbestand eines Materials auf einer Station (Tabelle minimum_stock, Werte aus data.sql).
 * Den aktuellen Bestand kennt nur SAP, den Mindestbestand nur unsere Datenbank.
 */
@Entity
public class MinimumStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String station;
    private String material;
    private BigDecimal minimumQuantity;

    /** Leerer Konstruktor, den JPA zum Laden aus der Datenbank braucht. */
    protected MinimumStock() {
    }

    public Long getId() { return id; }
    public String getStation() { return station; }
    public String getMaterial() { return material; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }

}
