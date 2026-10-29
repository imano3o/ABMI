package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** The current stock lives in SAP, only the minimum stock is stored in our database. */
@Entity
public class MinimumStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String station;
    private String material;
    private BigDecimal minimumQuantity;

    /** Required by JPA to load rows from the database. */
    protected MinimumStock() {
    }

    public Long getId() { return id; }
    public String getStation() { return station; }
    public String getMaterial() { return material; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }

}
