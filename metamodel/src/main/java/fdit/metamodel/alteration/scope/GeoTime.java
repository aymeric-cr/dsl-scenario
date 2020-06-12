package fdit.metamodel.alteration.scope;

import fdit.metamodel.zone.Zone;

public class GeoTime implements Scope {

    private final Zone zone;
    private final long timeMillis;

    public GeoTime(final Zone zone,
                   final long timeMillis) {
        this.zone = zone;
        this.timeMillis = timeMillis;
    }

    public Zone getZone() {
        return zone;
    }

    public long getTimeMillis() {
        return timeMillis;
    }
}
