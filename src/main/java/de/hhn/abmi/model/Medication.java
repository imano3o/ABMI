package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Display name of a material.
 *
 * <p>SAP's material stock API returns only the material number,
 * so the name the nurses see is part of our master data.
 */
@Entity
public class Medication {

    @Id
    private String material;
    private String name;

    /** Required by JPA. */
    protected Medication() {
    }

    public String getMaterial() { return material; }
    public String getName() { return name; }

}
