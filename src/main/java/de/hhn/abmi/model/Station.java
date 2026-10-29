package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * A station of the hospital. code is also its SAP storage location,
 * consumption is charged to its costCenter.
 */
@Entity
public class Station {

    @Id
    private String code;
    private String name;
    private String costCenter;

    protected Station() {
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCostCenter() { return costCenter; }

}
