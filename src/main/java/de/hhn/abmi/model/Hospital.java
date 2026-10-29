package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Das Krankenhaus. In SAP entspricht es einem Werk (Plant), z. B. "1710".
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
