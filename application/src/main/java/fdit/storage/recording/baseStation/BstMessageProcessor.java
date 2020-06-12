package fdit.storage.recording.baseStation;

import fdit.metamodel.aircraft.BaseStationAircraftState;
import fdit.metamodel.aircraft.BaseStationAircraftStateBuilder;
import fdit.metamodel.coordinates.Coordinates;
import fdit.storage.recording.RecordingMessageProcessor;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static fdit.metamodel.coordinates.CoordinatesUtils.areValidCoordinates;
import static fdit.storage.recording.baseStation.BaseStationParserUtils.*;
import static java.lang.Double.parseDouble;
import static java.lang.Short.parseShort;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isBlank;

public final class BstMessageProcessor implements RecordingMessageProcessor<List<String>, BaseStationAircraftState> {

    private static boolean isMalformedMessage(final Collection<String> message) {
        return message.size() < 22;
    }

    private static Optional<Coordinates> parseValidCoordinates(final List<String> message) {
        final Optional<Coordinates> position = parseCoordinates(message);
        if (position.isPresent() && areValidCoordinates(position.get())) {
            return position;
        }
        return empty();
    }

    private static Optional<Coordinates> parseCoordinates(final List<String> message) {
        if (message.get(14).isEmpty() || message.get(15).isEmpty()) {
            return empty();
        }
        try {
            final Coordinates coordinates = new Coordinates(parseDouble(message.get(14)), parseDouble(message.get(15)));
            return areValidCoordinates(coordinates) ? of(coordinates) : empty();
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<Double> parseValidAltitude(final List<String> message) {
        final Optional<Double> altitudeOpt = parseAltitude(message);
        if (!altitudeOpt.isPresent()) {
            return empty();
        }
        if (altitudeOpt.get() < 0) {
            return empty();
        }
        return altitudeOpt;
    }

    private static Optional<Double> parseAltitude(final List<String> message) {
        try {
            if (message.get(11).isEmpty()) {
                return empty();
            }
            final double altitude = parseDouble(message.get(11));
            if (altitude < 0) {
                return of(0.0);
            }
            return of(altitude);
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<Double> parseGroundSpeed(final List<String> message) {
        if (message.get(13).isEmpty()) {
            return empty();
        }
        try {
            return of(parseDouble(message.get(12)));
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<Double> parseTrack(final List<String> message) {
        if (message.get(13).isEmpty()) {
            return empty();
        }
        try {
            return of(parseDouble(message.get(13)));
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<Double> parseVerticalRate(final List<String> message) {
        if (message.get(16).isEmpty()) {
            return empty();
        }
        try {
            return of(parseDouble(message.get(16)));
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<Short> parseSquawk(final List<String> message) {
        if (message.get(17).isEmpty()) {
            return empty();
        }
        try {
            return of(parseShort(message.get(17)));
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<String> parseAlert(final List<String> message) {
        if (message.get(18).isEmpty()) {
            return empty();
        }
        try {
            final String alert = message.get(18);
            if (isBlank(alert)) {
                return empty();
            }
            return of(alert);
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<String> parseEmergency(final List<String> message) {
        if (message.get(19).isEmpty()) {
            return empty();
        }
        try {
            final String emergency = message.get(19);
            if (isBlank(emergency)) {
                return empty();
            }
            return of(emergency);
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<String> parseSpi(final List<String> message) {
        if (message.get(20).isEmpty()) {
            return empty();
        }
        try {
            final String spi = message.get(20);
            if (isBlank(spi)) {
                return empty();
            }
            return of(spi);
        } catch (final Exception e) {
            return empty();
        }
    }

    private static Optional<String> parseIsOnGround(final List<String> message) {
        if (message.get(21).isEmpty()) {
            return empty();
        }
        try {
            final String isOnGround = message.get(21);
            if (isBlank(isOnGround)) {
                return empty();
            }
            return of(isOnGround);
        } catch (final Exception e) {
            return empty();
        }
    }

    @Override
    public Optional<List<String>> messagePreprocessing(final String message) {
        final List<String> messageSplit = splitMessage(message);
        return isMalformedMessage(messageSplit) ? empty() : of(messageSplit);
    }

    @Override
    public int extractAircraftId(final List<String> message) {
        return parseAircraftId(message);
    }

    @Override
    public int extractIcao(final List<String> message) {
        return parseIcao(message);
    }

    @Override
    public Optional<String> extractCallSign(final List<String> message) {
        try {
            if ("1".equals(message.get(1))) {
                return of(message.get(10));
            }
        } catch (final Exception ignored) {
        }
        return empty();
    }

    @Override
    public Optional<BaseStationAircraftState> extractAircraftState(final List<String> message) {
        try {
            final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
            final Optional<String> isOnGround = parseIsOnGround(message);
            isOnGround.ifPresent(stateBuilder::withIsOnGround);
            parseValidCoordinates(message).ifPresent(stateBuilder::withPosition);
            parseValidAltitude(message).ifPresent(stateBuilder::withAltitude);
            parseGroundSpeed(message).ifPresent(stateBuilder::withGroundSpeed);
            parseTrack(message).ifPresent(stateBuilder::withTrack);
            parseVerticalRate(message).ifPresent(stateBuilder::withVerticalRate);
            parseSquawk(message).ifPresent(stateBuilder::withSquawk);
            parseAlert(message).ifPresent(stateBuilder::withAlert);
            parseEmergency(message).ifPresent(stateBuilder::withEmergency);
            parseSpi(message).ifPresent(stateBuilder::withSpi);

            final BaseStationAircraftState state = stateBuilder.build();
            if (!state.getPosition().isPresent()
                    && !state.getAltitude().isPresent()
                    && !state.getGroundSpeed().isPresent()
                    && !state.getTrack().isPresent()
                    && !state.getVerticalRate().isPresent()
                    && !state.getSquawk().isPresent()
                    && !state.getAlert().isPresent()) {
                return empty();
            }
            return of(state);
        } catch (final Exception ex) {
            return empty();
        }
    }

    @Override
    public long extractMessageDate(final List<String> message) {
        try {
            return parseDateMessageGenerated(message.get(6) + ',' + message.get(7));
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }
}