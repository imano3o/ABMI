package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** In SAP a hospital is a plant, e.g. "1710". */
@Entity
public class Hospital {

    @Id
    private String plant;
    private String name;

    protected Hospital() {
    }

    public String getPlant() { return plant; }
    public String getName() { return name; }

}
