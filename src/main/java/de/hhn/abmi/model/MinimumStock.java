package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** Minimum stock of a material on a station. Only this is stored in our database, the current stock comes from SAP. */
@Entity
public class MinimumStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String station;
    private String material;
    private BigDecimal minimumQuantity;

    /** Required by JPA. */
    protected MinimumStock() {
    }

    public Long getId() { return id; }
    public String getStation() { return station; }
    public String getMaterial() { return material; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }

}
