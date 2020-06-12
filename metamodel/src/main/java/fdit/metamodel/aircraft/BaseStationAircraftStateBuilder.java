package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;

public class BaseStationAircraftStateBuilder extends AircraftStateBuilder<BaseStationAircraftStateBuilder, BaseStationAircraftState> {

    private Double groundSpeed;
    private Double track;
    private Double verticalRate;
    private Short squawk;
    private String alert;
    private String emergency;
    private String spi;
    private String isOnGround;

    public BaseStationAircraftStateBuilder withGroundSpeed(final Double groundSpeed) {
        this.groundSpeed = groundSpeed;
        return this;
    }

    public BaseStationAircraftStateBuilder withTrack(final Double track) {
        this.track = track;
        return this;
    }

    public BaseStationAircraftStateBuilder withVerticalRate(final Double verticalRate) {
        this.verticalRate = verticalRate;
        return this;
    }

    public BaseStationAircraftStateBuilder withSquawk(final Short squawk) {
        this.squawk = squawk;
        return this;
    }

    public BaseStationAircraftStateBuilder withAlert(final String alert) {
        this.alert = alert;
        return this;
    }

    public BaseStationAircraftStateBuilder withEmergency(final String emergency) {
        this.emergency = emergency;
        return this;
    }

    public BaseStationAircraftStateBuilder withSpi(final String spi) {
        this.spi = spi;
        return this;
    }

    public BaseStationAircraftStateBuilder withIsOnGround(final String isOnGround) {
        this.isOnGround = isOnGround;
        return this;
    }

    @Override
    public void fillMissingWith(final BaseStationAircraftState state) {
        state.getGroundSpeed().ifPresent(groundSpeed -> {
            if (this.groundSpeed == null) {
                this.groundSpeed = groundSpeed;
            }
        });
        state.getTrack().ifPresent(track -> {
            if (this.track == null) {
                this.track = track;
            }
        });
        state.getVerticalRate().ifPresent(verticalRate -> {
            if (this.verticalRate == null) {
                this.verticalRate = verticalRate;
            }
        });
        state.getSquawk().ifPresent(squawk -> {
            if (this.squawk == null) {
                this.squawk = squawk;
            }
        });
        state.getAlert().ifPresent(alert -> {
            if (this.alert == null) {
                this.alert = alert;
            }
        });
        state.getEmergency().ifPresent(emergency -> {
            if (this.emergency == null) {
                this.emergency = emergency;
            }
        });
        state.getSpi().ifPresent(spi -> {
            if (this.spi == null) {
                this.spi = spi;
            }
        });
        state.getIsOnGround().ifPresent(isOnGround -> {
            if (this.isOnGround == null) {
                this.isOnGround = isOnGround;
            }
        });
    }

    @Override
    public boolean isFilled() {
        return groundSpeed != null &&
                track != null &&
                verticalRate != null &&
                squawk != null &&
                alert != null &&
                emergency != null &&
                spi != null &&
                isOnGround != null;
    }

    @Override
    public BaseStationAircraftState build() {
        final Coordinates position = latitude != null && longitude != null ? new Coordinates(latitude, longitude) : null;
        return new BaseStationAircraftState(relativeDate, position, altitude,
                groundSpeed, track, verticalRate, squawk, alert, emergency, spi, isOnGround);
    }
}