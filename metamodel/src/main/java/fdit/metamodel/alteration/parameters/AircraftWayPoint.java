package fdit.metamodel.alteration.parameters;

import fdit.metamodel.coordinates.Coordinates;

public class AircraftWayPoint {

    private final Coordinates coordinates;
    private final int altitude;
    private final long time;
    private final boolean latitudeOffset;
    private final boolean longitudeOffset;
    private final boolean altitudeOffset;

    public AircraftWayPoint(final Coordinates coordinates, final int altitude, final long time) {
        this.coordinates = coordinates;
        this.altitude = altitude;
        this.time = time;
        latitudeOffset = false;
        longitudeOffset = false;
        altitudeOffset = false;
    }

    public AircraftWayPoint(final Coordinates coordinates,
                            final int altitude,
                            final long time,
                            final boolean latitudeOffset,
                            final boolean longitudeOffset,
                            final boolean altitudeOffset) {
        this.coordinates = coordinates;
        this.altitude = altitude;
        this.time = time;
        this.latitudeOffset = latitudeOffset;
        this.longitudeOffset = longitudeOffset;
        this.altitudeOffset = altitudeOffset;
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

    public boolean isLatitudeOffset() {
        return latitudeOffset;
    }

    public boolean isLongitudeOffset() {
        return longitudeOffset;
    }

    public boolean isAltitudeOffset() {
        return altitudeOffset;
    }
}