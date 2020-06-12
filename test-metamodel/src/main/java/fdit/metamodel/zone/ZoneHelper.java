package fdit.metamodel.zone;

import fdit.metamodel.coordinates.Coordinates;
import fdit.tools.functional.ThrowableConsumer;

import static java.util.Arrays.asList;

public final class ZoneHelper {
    private ZoneHelper() {
    }

    public static ThrowableConsumer<Zone> lowerAltitude(final double altitude) {
        return zone -> zone.setAltitudeLowerBound(altitude);
    }

    public static ThrowableConsumer<Zone> upperAltitude(final double altitude) {
        return zone -> zone.setAltitudeUpperBound(altitude);
    }

    public static ThrowableConsumer<? super FditPolygon> vertices(final Coordinates... coordinates) {
        return polygon -> polygon.setVertices(asList(coordinates));
    }
}