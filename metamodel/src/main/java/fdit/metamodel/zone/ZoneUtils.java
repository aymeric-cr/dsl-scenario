package fdit.metamodel.zone;


import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.AircraftCriterion;
import fdit.metamodel.aircraft.NullLoaderException;
import fdit.metamodel.aircraft.OutOfDateException;

import java.util.UUID;
import java.util.function.Predicate;

import static java.util.Collections.EMPTY_LIST;
import static java.util.UUID.fromString;

public final class ZoneUtils {

    public static final Zone NO_ZONE_ITEM = new FditPolygon(null, fromString("1-1-1-1-1"), -1.0, -1.0, EMPTY_LIST);

    private ZoneUtils() {
    }

    public static Predicate<Zone> withZoneId(final UUID id) {
        return zone -> zone.getId().equals(id);
    }

    public static boolean aircraftEventuallyInZone(final Zone zone,
                                                   final Aircraft aircraft,
                                                   final boolean inside,
                                                   final long interval) throws NullLoaderException {
        zone.createShapeFromZone();
        long timeCount = (long) aircraft.getFirstCriterionAppearance(AircraftCriterion.LATITUDE);
        long timeEnd = (long) aircraft.getLastCriterionAppearance(AircraftCriterion.LATITUDE);
        boolean result = false;
        while (timeCount <= timeEnd) {
            try {
                result = positionInZone(zone, aircraft.query(timeCount, AircraftCriterion.LATITUDE),
                        aircraft.query(timeCount, AircraftCriterion.LONGITUDE),
                        aircraft.query(timeCount, AircraftCriterion.ALTITUDE));
            } catch (OutOfDateException ex) {
                result = !inside;
            }
            if (result && inside) {
                return true;
            } else if (!result && !inside) {
                return true;
            }
            timeCount += interval;
            if (timeCount > timeEnd && timeCount < (timeEnd + interval)) timeCount = timeEnd;
        }
        return result;
    }

    public static boolean aircraftAlwaysInZone(final Zone zone,
                                               final Aircraft aircraft,
                                               final boolean inside,
                                               final long interval) throws NullLoaderException {
        zone.createShapeFromZone();
        long timeCount = (long) aircraft.getFirstCriterionAppearance(AircraftCriterion.LATITUDE);
        long timeEnd = (long) aircraft.getLastCriterionAppearance(AircraftCriterion.LATITUDE);
        boolean result = false;
        while (timeCount <= timeEnd) {
            try {
                result = positionInZone(zone, aircraft.query(timeCount, AircraftCriterion.LATITUDE),
                        aircraft.query(timeCount, AircraftCriterion.LONGITUDE),
                        aircraft.query(timeCount, AircraftCriterion.ALTITUDE));
            } catch (OutOfDateException ex) {
                result = inside;
            }
            if (result && !inside) {
                return false;
            } else if (!result && inside) {
                return false;
            }
            timeCount += interval;
            if (timeCount > timeEnd && timeCount < (timeEnd + interval)) timeCount = timeEnd;
        }
        return result == inside;
    }

    public static boolean positionInZone(final Zone zone, double latitude, double longitude, double altitude) {
        return altitude >= zone.getAltitudeLowerBound()
                && altitude <= zone.getAltitudeUpperBound()
                && zone.getZoneShape().contains(longitude, latitude);
    }
}