package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Hospital that uses the app.
 *
 * <p>In SAP the hospital is a plant, so {@code plant} is part of every SAP call.
 */
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
