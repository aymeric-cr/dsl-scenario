package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;

public abstract class AircraftStateBuilder<B extends AircraftStateBuilder, T extends AircraftState> {

    protected long relativeDate;
    protected Double latitude;
    protected Double longitude;
    protected Double altitude;

    public B withRelativeDate(final long relativeDate) {
        this.relativeDate = relativeDate;
        return (B) this;
    }

    public B withPosition(final Coordinates position) {
        latitude = position.getLatitude();
        longitude = position.getLongitude();
        return (B) this;
    }

    public B withLatitude(final Double latitude) {
        this.latitude = latitude;
        return (B) this;
    }

    public B withLongitude(final Double longitude) {
        this.longitude = longitude;
        return (B) this;
    }

    public B withAltitude(final Double altitude) {
        this.altitude = altitude;
        return (B) this;
    }

    public abstract void fillMissingWith(final T state);

    public abstract T build();

    public abstract boolean isFilled();

}
