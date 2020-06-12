package fdit.metamodel.alteration.scope;

import fdit.metamodel.zone.Zone;

public class GeoArea implements Scope {

    private final Zone zone;

    public GeoArea(final Zone zone) {
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }
}
