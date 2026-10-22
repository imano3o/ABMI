package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Offene Nachbestellung eines Materials.
 * Pro Material gibt es höchstens eine offene Bestellung, deshalb ist material die @Id.
 */
@Entity
public class OpenOrder {

    @Id
    private String material;
    private String purchaseOrder;
    private BigDecimal quantity;
    private LocalDate orderedAt;

    protected OpenOrder() {
    }

    public OpenOrder(String material, String purchaseOrder, BigDecimal quantity, LocalDate orderedAt) {
        this.material = material;
        this.purchaseOrder = purchaseOrder;
        this.quantity = quantity;
        this.orderedAt = orderedAt;
    }

    public String getMaterial() { return material; }
    public String getPurchaseOrder() { return purchaseOrder; }
    public BigDecimal getQuantity() { return quantity; }
    public LocalDate getOrderedAt() { return orderedAt; }

}
