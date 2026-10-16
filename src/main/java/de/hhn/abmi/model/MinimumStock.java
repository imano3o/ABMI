package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class MinimumStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected MinimumStock(){

    }

    private String material;
    private BigDecimal minimumQuantity;

    public Long getId() { return id; }
    public String getMaterial() { return material; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }


}
