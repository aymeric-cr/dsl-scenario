package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;

import javax.annotation.Nullable;
import java.util.Optional;

public class BaseStationAircraftState extends AircraftState {

    @Nullable
    private final Double groundSpeed;
    @Nullable
    private final Double track;
    @Nullable
    private final Double verticalRate;
    @Nullable
    private final Short squawk;
    @Nullable
    private final String alert;
    @Nullable
    private final String emergency;
    @Nullable
    private final String spi;
    @Nullable
    private final String isOnGround;

    BaseStationAircraftState(final long relativeDate,
                             @Nullable final Coordinates position,
                             @Nullable final Double altitude,
                             @Nullable final Double groundSpeed,
                             @Nullable final Double track,
                             @Nullable final Double verticalRate,
                             @Nullable final Short squawk,
                             @Nullable final String alert,
                             @Nullable final String emergency,
                             @Nullable final String spi,
                             @Nullable final String isOnGround) {
        super(relativeDate, position, altitude);
        this.groundSpeed = groundSpeed;
        this.track = track;
        this.verticalRate = verticalRate;
        this.squawk = squawk;
        this.alert = alert;
        this.emergency = emergency;
        this.spi = spi;
        this.isOnGround = isOnGround;
    }

    public Optional<Double> getGroundSpeed() {
        return Optional.ofNullable(groundSpeed);
    }

    public Optional<Double> getTrack() {
        return Optional.ofNullable(track);
    }

    public Optional<Double> getVerticalRate() {
        return Optional.ofNullable(verticalRate);
    }

    public Optional<Short> getSquawk() {
        return Optional.ofNullable(squawk);
    }

    public Optional<String> getAlert() {
        return Optional.ofNullable(alert);
    }

    public Optional<String> getEmergency() {
        return Optional.ofNullable(emergency);
    }

    public Optional<String> getSpi() {
        return Optional.ofNullable(spi);
    }

    public Optional<String> getIsOnGround() {
        return Optional.ofNullable(isOnGround);
    }

    @Override
    public <T> T accept(final AircraftStateVisitor<T> visitor) {
        return visitor.visitBaseStationAircraftState(this);
    }
}