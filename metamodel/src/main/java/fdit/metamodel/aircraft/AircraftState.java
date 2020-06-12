package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;

import javax.annotation.Nullable;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class AircraftState {

    @Nullable
    private final Coordinates position;
    @Nullable
    private final Double altitude;
    private Aircraft aircraft;
    private long relativeDate; // in milliseconds

    AircraftState(final long relativeDate, @Nullable final Coordinates position, @Nullable final Double altitude) {
        this.relativeDate = relativeDate;
        this.position = position;
        this.altitude = altitude;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    void setAircraft(final Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public long getRelativeDate() {
        return relativeDate;
    }

    public void setRelativeDate(final long relativeDate) {
        this.relativeDate = relativeDate;
    }

    public Optional<Coordinates> getPosition() {
        return ofNullable(position);
    }

    public Optional<Double> getAltitude() {
        return ofNullable(altitude);
    }

    public abstract <T> T accept(final AircraftStateVisitor<T> visitor);

    public interface AircraftStateVisitor<T> {

        T visitBaseStationAircraftState(final BaseStationAircraftState state);
    }
}