package de.hhn.abmi.dto.api;

/** Krankenhaus für die Anzeige im Frontend; name kann null sein, wenn nicht gepflegt. */
public record HospitalResponse(
        String name
) {
}
