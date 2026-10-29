package de.hhn.abmi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Eine Station im Krankenhaus.
 * Der code ist gleichzeitig der SAP-Lagerort (StorageLocation), z. B. "ST03".
 * Die Kostenstelle (CostCenter) gibt an, auf wen der Verbrauch in SAP gebucht wird.
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
