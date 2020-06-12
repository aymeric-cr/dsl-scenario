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

final class SbsMessageProcessor implements RecordingMessageProcessor<List<String>, BaseStationAircraftState> {

    private String currentMessageType;

    private static BaseStationAircraftState handleTransmissionType2(final List<String> message) {
        final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
        parseValidCoordinates(message).ifPresent(stateBuilder::withPosition);
        parseAltitude(message).ifPresent(stateBuilder::withAltitude);
        return stateBuilder
                .withGroundSpeed(parseGroundSpeed(message))
                .withTrack(parseTrack(message))
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType3(final List<String> message) {
        final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
        parseValidCoordinates(message).ifPresent(stateBuilder::withPosition);
        parseAltitude(message).ifPresent(stateBuilder::withAltitude);
        return stateBuilder
                .withAlert(parseAlert(message))
                .withEmergency(parseEmergency(message))
                .withSpi(parseSpi(message))
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType4(final List<String> message) {
        return new BaseStationAircraftStateBuilder()
                .withGroundSpeed(parseGroundSpeed(message))
                .withTrack(parseTrack(message))
                .withVerticalRate(parseVerticalRate(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType5(final List<String> message) {
        final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
        return stateBuilder
                .withAlert(parseAlert(message))
                .withSpi(parseSpi(message))
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType6(final List<String> message) {
        final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
        return stateBuilder
                .withSquawk(parseSquawk(message))
                .withAlert(parseAlert(message))
                .withEmergency(parseEmergency(message))
                .withSpi(parseSpi(message))
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType7(final List<String> message) {
        final BaseStationAircraftStateBuilder stateBuilder = new BaseStationAircraftStateBuilder();
        return stateBuilder
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static BaseStationAircraftState handleTransmissionType8(final List<String> message) {
        return new BaseStationAircraftStateBuilder()
                .withIsOnGround(parseIsOnGround(message))
                .build();
    }

    private static boolean isMalformedMessage(final Collection<String> message, final String transmissionType) {
        switch (transmissionType) {
            case "1":
                return message.size() < 11;
            case "2":
            case "8":
            case "7":
            case "6":
            case "5":
            case "3":
                return message.size() < 22;
            case "4":
                return message.size() < 17;
            default:
                return true;
        }
    }

    private static Optional<Coordinates> parseValidCoordinates(final List<String> message) {
        final Optional<Coordinates> position = parseCoordinates(message);
        if (position.isPresent() && areValidCoordinates(position.get())) {
            return position;
        }
        return empty();
    }

    private static Optional<Coordinates> parseCoordinates(final List<String> message) {
        try {
            final Coordinates coordinates = new Coordinates(parseDouble(message.get(14)), parseDouble(message.get(15)));
            return areValidCoordinates(coordinates) ? of(coordinates) : empty();
        } catch (final Exception e) {
            return empty();
        }
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

    private static double parseGroundSpeed(final List<String> message) {
        return parseDouble(message.get(12));
    }

    private static double parseTrack(final List<String> message) {
        return parseDouble(message.get(13));
    }

    private static double parseVerticalRate(final List<String> message) {
        return parseDouble(message.get(16));
    }

    private static short parseSquawk(final List<String> message) {
        return parseShort(message.get(17));
    }

    private static String parseAlert(final List<String> message) {
        return message.get(18);
    }

    private static String parseEmergency(final List<String> message) {
        return message.get(19);
    }

    private static String parseSpi(final List<String> message) {
        return message.get(20);
    }

    private static String parseIsOnGround(final List<String> message) {
        return message.get(21);
    }

    @Override
    public Optional<List<String>> messagePreprocessing(final String message) {
        final List<String> messageSplit = splitMessage(message);
        currentMessageType = parseTransmissionType(messageSplit);
        return isMalformedMessage(messageSplit, currentMessageType) ? empty() : of(messageSplit);
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
        if ("1".equals(currentMessageType)) {
            return of(message.get(10));
        }
        return empty();
    }

    @Override
    public Optional<BaseStationAircraftState> extractAircraftState(final List<String> message) {
        try {
            switch (currentMessageType) {
                case "2":
                    return of(handleTransmissionType2(message));
                case "3":
                    return of(handleTransmissionType3(message));
                case "4":
                    return of(handleTransmissionType4(message));
                case "5":
                    return of(handleTransmissionType5(message));
                case "6":
                    return of(handleTransmissionType6(message));
                case "7":
                    return of(handleTransmissionType7(message));
                case "8":
                    return of(handleTransmissionType8(message));
                default:
                    return empty();
            }
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