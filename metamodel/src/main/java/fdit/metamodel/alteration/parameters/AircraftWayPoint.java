package fdit.metamodel.alteration.parameters;

import fdit.metamodel.coordinates.Coordinates;

public class AircraftWayPoint {

    private final Coordinates coordinates;
    private final int altitude;
    private final long time;

    public AircraftWayPoint(final Coordinates coordinates, final int altitude, final long time) {
        this.coordinates = coordinates;
        this.altitude = altitude;
        this.time = time;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public long getTime() {
        return time;
    }

    public int getAltitude() {
        return altitude;
    }
}