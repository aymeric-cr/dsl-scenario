package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;
import javafx.util.Pair;

import java.util.Collection;
import java.util.UUID;

import static fdit.metamodel.aircraft.AircraftCriterion.*;
import static java.lang.Integer.MIN_VALUE;

public final class AircraftUtils {

    public static final int MINIMUM_KNOWN_POSITIONS = 5;

    private AircraftUtils() {
    }

    public static String renderAircraftIds(final Collection<Aircraft> aircrafts) {
        if (aircrafts.isEmpty()) {
            return "";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Aircraft aircraft : aircrafts) {
            stringBuilder.append(aircraft.getAircraftId()).append(',');
        }
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    /**
     * @return Double.Nan if aircraft is not present at relativeDate
     */
    public static Pair<Coordinates, Double> computeApproximatedPosition(final Aircraft aircraft,
                                                                        final long relativeDate) throws NullLoaderException {
        double latitude;
        double longitude;
        double altitude;

        try {
            latitude = aircraft.query(relativeDate, LATITUDE);
            longitude = aircraft.query(relativeDate, LONGITUDE);
        } catch (OutOfDateException ex) {
            latitude = Double.NaN;
            longitude = Double.NaN;
        }

        try {
            altitude = aircraft.query(relativeDate, ALTITUDE);
        } catch (OutOfDateException ex) {
            altitude = Double.NaN;
        }
        return new Pair<>(new Coordinates(latitude, longitude), altitude);
    }

    public static boolean inTimeInterval(final Aircraft aircraft,
                                         final TimeInterval interval) {
        return aircraft.getTimeOfFirstAppearance() >= interval.getStart() &&
                aircraft.getTimeOfLastAppearance() <= interval.getEnd();
    }

    public static UUID randomUUID() {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (uuid.hashCode() == MIN_VALUE);
        return uuid;
    }
}