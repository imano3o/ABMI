package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/**
 * Minimum stock of a material on a station.
 *
 * <p>Only the minimum is stored in our database; the current stock always comes from SAP.
 * Target quantity and package size drive the order proposal: refill up to the target, in whole packages.
 */
@Entity
public class MinimumStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String station;
    private String material;
    private BigDecimal minimumQuantity;
    /** Stock to refill up to; if empty, twice the minimum is used */
    private BigDecimal targetQuantity;
    /** Pieces per package; orders are rounded up to whole packages, empty means 1 */
    private BigDecimal packageSize;

    /** Required by JPA. */
    protected MinimumStock() {
    }

    public Long getId() { return id; }
    public String getStation() { return station; }
    public String getMaterial() { return material; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }

    public BigDecimal getTargetQuantity() {
        return targetQuantity != null ? targetQuantity : minimumQuantity.multiply(BigDecimal.TWO);
    }

    public BigDecimal getPackageSize() {
        return packageSize != null ? packageSize : BigDecimal.ONE;
    }

}
