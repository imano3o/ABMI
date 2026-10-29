package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** The hospital using the app. In SAP it is a plant. */
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
