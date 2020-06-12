package fdit.metamodel.alteration.scope;

import fdit.metamodel.zone.Zone;

public class GeoTimeWindow implements Scope {

    private final Zone zone;
    private final long lowerBoundMillis;
    private final long upperBoundMillis;

    public GeoTimeWindow(final Zone zone,
                         final long lowerBoundMillis,
                         final long upperBoundMillis) {
        this.zone = zone;
        this.lowerBoundMillis = lowerBoundMillis;
        this.upperBoundMillis = upperBoundMillis;
    }

    public Zone getZone() {
        return zone;
    }

    public long getLowerBoundMillis() {
        return lowerBoundMillis;
    }

    public long getUpperBoundMillis() {
        return upperBoundMillis;
    }
}
