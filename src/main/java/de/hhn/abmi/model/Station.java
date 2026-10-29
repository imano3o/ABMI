package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * code is also the SAP storage location, e.g. "ST03".
 * Consumption is charged to costCenter in SAP.
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
