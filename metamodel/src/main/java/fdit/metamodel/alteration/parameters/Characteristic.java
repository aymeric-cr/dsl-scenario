package fdit.metamodel.alteration.parameters;

import fdit.metamodel.recording.Recording;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.recording.Recording.RecordingSwitch;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.regex.Pattern.compile;

public enum Characteristic {
    ICAO,
    CALL_SIGN,
    ALTITUDE,
    GROUND_SPEED,
    TRACK,
    LATITUDE,
    LONGITUDE,
    VERTICAL_RATE,
    SQUAWK,
    ALERT,
    EMERGENCY,
    SPI,
    IS_ON_GROUND,
    EAST_WEST_VELOCITY,
    NORTH_SOUTH_VELOCITY,
    TIMESTAMP_NANO;

    private static final Pattern FLOAT_01_PATTERN = Pattern.compile("^(\\d+)?([.]?\\d?)$");
    private static final Pattern SQUAWK_PATTERN = Pattern.compile("^([0-7]{4})$");
    private static final Pattern CALLSIGN_PATTERN = Pattern.compile("^([0-9A-Z]{1,8})$");
    private static final Pattern ICAO_PATTERN = Pattern.compile("^(([0-9A-F]{6})|RANDOM)$");

    public static Collection<Characteristic> getCharacteristics(final Recording recording) {
        return new RecordingSwitch<Collection<Characteristic>>() {

            @Override
            public Collection<Characteristic> visitBaseStationRecording(final Recording recording) {
                return getSbsCharacteristics();
            }

            @Override
            public Collection<Characteristic> visitSiteBaseStationRecording(final Recording recording) {
                return getSbsCharacteristics();
            }
        }.doSwitch(recording);
    }

    public static Collection<Characteristic> getSbsCharacteristics() {
        return newArrayList(
                ALTITUDE,
                CALL_SIGN,
                EMERGENCY,
                GROUND_SPEED,
                ICAO,
                LATITUDE,
                LONGITUDE,
                SPI,
                SQUAWK,
                TRACK,
                VERTICAL_RATE,
                ALERT,
                IS_ON_GROUND);
    }

    public static boolean isSpeedValid(final String content) {
        try {
            final double speed = parseDouble(content);
            return FLOAT_01_PATTERN.matcher(content).matches() && 0 <= speed && speed <= 1446;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isTrackValid(final String content) {
        try {
            final double angle = parseDouble(content);
            return FLOAT_01_PATTERN.matcher(content).matches() && 0 <= angle && angle < 360;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isAltitudeValid(final String content) {
        try {
            double altitude = parseDouble(content);
            return -1000 <= altitude && altitude <= 50175;
        } catch (final NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isNumberValid(final String content) {
        try {
            parseDouble(content);
            return true;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isIntValid(final String content) {
        try {
            parseInt(content);
            return true;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isNSEWVelocityValid(final String content) {
        try {
            final int speed = parseInt(content);
            return -1024 < speed && speed < 1024;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isVerticalRateValid(final String content) {
        try {
            final int speed = parseInt(content);
            return -32640 <= speed && speed <= 32640;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isLongValid(final String content) {
        try {
            parseLong(content);
            return true;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isLatitudeValid(final String content) {
        try {
            final double latitude = parseDouble(content);
            return latitude >= -90 && latitude <= 90 || latitude == 91;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isLongitudeValid(final String content) {
        try {
            final double longitude = parseDouble(content);
            return longitude >= -180 && longitude <= 180 || longitude == 181;
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean isIcaoValid(final String icaoText) {
        return ICAO_PATTERN.matcher(icaoText).matches();
    }

    public static boolean isCallSignValid(final String callsignText) {
        return CALLSIGN_PATTERN.matcher(callsignText).matches();
    }

    public static boolean isSquawkValid(final String squawkText) {
        return SQUAWK_PATTERN.matcher(squawkText).matches();
    }

    public static boolean isFlagValid(final String content) {
        final Pattern pattern = compile("^[0|1]?$");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    public static Predicate<String> getValidationFunction(final Characteristic characteristic) {
        return new CharacteristicSwitch<Predicate<String>>() {

            @Override
            public Predicate<String> visitTimestampNano() {
                return Characteristic::isLongValid;
            }

            @Override
            public Predicate<String> visitEastWestVelocity() {
                return Characteristic::isNSEWVelocityValid;
            }

            @Override
            public Predicate<String> visitNorthSouthVelocity() {
                return Characteristic::isNSEWVelocityValid;
            }

            @Override
            public Predicate<String> visitVerticalRate() {
                return Characteristic::isVerticalRateValid;
            }

            @Override
            public Predicate<String> visitIsOnGround() {
                return Characteristic::isFlagValid;
            }

            @Override
            public Predicate<String> visitAlert() {
                return Characteristic::isFlagValid;
            }

            @Override
            public Predicate<String> visitLatitude() {
                return Characteristic::isLatitudeValid;
            }

            @Override
            public Predicate<String> visitLongitude() {
                return Characteristic::isLongitudeValid;
            }

            @Override
            public Predicate<String> visitSpi() {
                return Characteristic::isFlagValid;
            }

            @Override
            public Predicate<String> visitSquawk() {
                return Characteristic::isSquawkValid;
            }

            @Override
            public Predicate<String> visitTrack() {
                return Characteristic::isTrackValid;
            }

            @Override
            public Predicate<String> visitAltitude() {
                return Characteristic::isAltitudeValid;
            }

            @Override
            public Predicate<String> visitCallSign() {
                return Characteristic::isCallSignValid;
            }

            @Override
            public Predicate<String> visitEmergency() {
                return Characteristic::isFlagValid;
            }

            @Override
            public Predicate<String> visitGroundSpeed() {
                return Characteristic::isSpeedValid;
            }

            @Override
            public Predicate<String> visitIcao() {
                return Characteristic::isIcaoValid;
            }
        }.doSwitch(characteristic);
    }

    public boolean canBeARange() {
        return new CharacteristicSwitch<Boolean>() {
            @Override
            public Boolean visitAltitude() {
                return true;
            }

            @Override
            public Boolean visitLatitude() {
                return true;
            }

            @Override
            public Boolean visitLongitude() {
                return true;
            }

            @Override
            public Boolean visitGroundSpeed() {
                return true;
            }

            @Override
            public Boolean visitCallSign() {
                return false;
            }

            @Override
            public Boolean visitEmergency() {
                return false;
            }

            @Override
            public Boolean visitIcao() {
                return false;
            }

            @Override
            public Boolean visitSpi() {
                return false;
            }

            @Override
            public Boolean visitSquawk() {
                return false;
            }

            @Override
            public Boolean visitTrack() {
                return false;
            }

            @Override
            public Boolean visitTimestampNano() {
                return true;
            }

            @Override
            public Boolean visitEastWestVelocity() {
                return true;
            }

            @Override
            public Boolean visitNorthSouthVelocity() {
                return true;
            }

            @Override
            public Boolean visitVerticalRate() {
                return true;
            }

            @Override
            public Boolean visitIsOnGround() {
                return false;
            }

            @Override
            public Boolean visitAlert() {
                return false;
            }

            @Override
            public Boolean visitDefault() {
                return false;
            }
        }.doSwitch(this);
    }

    public boolean canBeAnOffset() {
        return new CharacteristicSwitch<Boolean>() {
            @Override
            public Boolean visitAltitude() {
                return true;
            }

            @Override
            public Boolean visitGroundSpeed() {
                return true;
            }

            @Override
            public Boolean visitLatitude() {
                return true;
            }

            @Override
            public Boolean visitLongitude() {
                return true;
            }

            @Override
            public Boolean visitTrack() {
                return true;
            }

            @Override
            public Boolean visitTimestampNano() {
                return true;
            }

            @Override
            public Boolean visitEastWestVelocity() {
                return true;
            }

            @Override
            public Boolean visitNorthSouthVelocity() {
                return true;
            }

            @Override
            public Boolean visitVerticalRate() {
                return true;
            }

            @Override
            public Boolean visitIsOnGround() {
                return false;
            }

            @Override
            public Boolean visitAlert() {
                return false;
            }

            @Override
            public Boolean visitCallSign() {
                return false;
            }

            @Override
            public Boolean visitEmergency() {
                return false;
            }

            @Override
            public Boolean visitIcao() {
                return false;
            }

            @Override
            public Boolean visitSpi() {
                return false;
            }

            @Override
            public Boolean visitSquawk() {
                return false;
            }

            @Override
            public Boolean visitDefault() {
                return false;
            }
        }.doSwitch(this);
    }

    public boolean canBeAList() {
        return new CharacteristicSwitch<Boolean>() {
            @Override
            public Boolean visitCallSign() {
                return true;
            }

            @Override
            public Boolean visitIcao() {
                return true;
            }

            @Override
            public Boolean visitSpi() {
                return true;
            }

            @Override
            public Boolean visitSquawk() {
                return true;
            }

            @Override
            public Boolean visitAltitude() {
                return true;
            }

            @Override
            public Boolean visitGroundSpeed() {
                return true;
            }

            @Override
            public Boolean visitLatitude() {
                return true;
            }

            @Override
            public Boolean visitLongitude() {
                return true;
            }

            @Override
            public Boolean visitTrack() {
                return true;
            }

            @Override
            public Boolean visitTimestampNano() {
                return true;
            }

            @Override
            public Boolean visitEastWestVelocity() {
                return true;
            }

            @Override
            public Boolean visitNorthSouthVelocity() {
                return true;
            }

            @Override
            public Boolean visitVerticalRate() {
                return true;
            }

            @Override
            public Boolean visitIsOnGround() {
                return true;
            }

            @Override
            public Boolean visitAlert() {
                return true;
            }

            @Override
            public Boolean visitEmergency() {
                return true;
            }

            @Override
            public Boolean visitDefault() {
                return false;
            }
        }.doSwitch(this);
    }

    public interface CharacteristicSwitch<T> {

        T visitAltitude();

        T visitCallSign();

        T visitEmergency();

        T visitGroundSpeed();

        T visitIcao();

        T visitLatitude();

        T visitLongitude();

        T visitSpi();

        T visitSquawk();

        T visitTrack();

        T visitTimestampNano();

        T visitEastWestVelocity();

        T visitNorthSouthVelocity();

        T visitVerticalRate();

        T visitIsOnGround();

        T visitAlert();

        default T visitDefault() {
            throw new RuntimeException("Unknown characteristic");
        }

        @SuppressWarnings({"SwitchStatementWithTooManyBranches"})
        default T doSwitch(final Characteristic characteristic) {
            switch (characteristic) {
                case ALTITUDE:
                    return visitAltitude();
                case CALL_SIGN:
                    return visitCallSign();
                case EMERGENCY:
                    return visitEmergency();
                case GROUND_SPEED:
                    return visitGroundSpeed();
                case ICAO:
                    return visitIcao();
                case LATITUDE:
                    return visitLatitude();
                case LONGITUDE:
                    return visitLongitude();
                case SPI:
                    return visitSpi();
                case SQUAWK:
                    return visitSquawk();
                case TRACK:
                    return visitTrack();
                case TIMESTAMP_NANO:
                    return visitTimestampNano();
                case EAST_WEST_VELOCITY:
                    return visitEastWestVelocity();
                case NORTH_SOUTH_VELOCITY:
                    return visitNorthSouthVelocity();
                case VERTICAL_RATE:
                    return visitVerticalRate();
                case ALERT:
                    return visitAlert();
                case IS_ON_GROUND:
                    return visitIsOnGround();
                default:
                    return visitDefault();
            }
        }
    }
}