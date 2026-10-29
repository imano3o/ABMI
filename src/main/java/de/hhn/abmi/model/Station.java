package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Station of the hospital.
 *
 * <p>{@code code} is also the SAP storage location,
 * and consumption is charged to {@code costCenter}.
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
