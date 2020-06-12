package fdit.database;

import fdit.metamodel.aircraft.*;
import fdit.metamodel.aircraft.AircraftCriterion.AircraftCriterionSwitch;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.recording.Recording.RecordingSwitch;
import fdit.tools.functional.ThrowableBiFunction;
import javafx.util.Pair;

import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.database.ResultSetUtils.*;
import static fdit.metamodel.aircraft.AircraftUtils.computeApproximatedPosition;
import static java.util.Optional.of;

public final class AircraftRequests {

    private AircraftRequests() {
    }

    public static Optional<?> fetchAircraftStaticPropertyValue(final Aircraft aircraft,
                                                               final AircraftCriterion property) {
        return new AircraftCriterionSwitch<Optional<?>>() {
            @Override
            public Optional<?> visitKnownPositions() {
                return of(aircraft.getKnownPositions());
            }

            @Override
            public Optional<?> visitMaxAltitude() {
                return of(aircraft.getStaticProperties().getMaxAltitude());
            }

            @Override
            public Optional<?> visitMinAltitude() {
                return of(aircraft.getStaticProperties().getMinAltitude());
            }

            @Override
            public Optional<?> visitMeanAltitude() {
                return of(aircraft.getStaticProperties().getMeanAltitude());
            }

            @Override
            public Optional<?> visitMaxLatitude() {
                return of(aircraft.getStaticProperties().getMinLatitude());
            }

            @Override
            public Optional<?> visitMinLatitude() {
                return of(aircraft.getStaticProperties().getMinLatitude());
            }

            @Override
            public Optional<?> visitMeanLatitude() {
                return of(aircraft.getStaticProperties().getMeanLatitude());
            }

            @Override
            public Optional<?> visitMaxLongitude() {
                return of(aircraft.getStaticProperties().getMinLongitude());
            }

            @Override
            public Optional<?> visitMinLongitude() {
                return of(aircraft.getStaticProperties().getMinLongitude());
            }

            @Override
            public Optional<?> visitMeanLongitude() {
                return of(aircraft.getStaticProperties().getMeanLongitude());
            }

            @Override
            public Optional<?> visitMaxGroundSpeed() {
                return of(aircraft.getStaticProperties().getMinGroundSpeed());
            }

            @Override
            public Optional<?> visitMinGroundSpeed() {
                return of(aircraft.getStaticProperties().getMinGroundSpeed());
            }

            @Override
            public Optional<?> visitMeanGroundSpeed() {
                return of(aircraft.getStaticProperties().getMeanGroundSpeed());
            }

            @Override
            public Optional<?> visitCallSign() {
                return of(aircraft.getCallSign());
            }

            @Override
            public Optional<?> visitIcao() {
                return of(Integer.toHexString(aircraft.getIcao()));
            }

            @Override
            public Optional<?> visitDefault() {
                throw new RuntimeException("accessed dynamic property from static accessor");
            }
        }.doSwitch(property);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static AircraftState fetchAircraftSnapshot(final Aircraft aircraft,
                                                      final Recording recording,
                                                      final long relativeDate) throws Exception {
        final Pair<Coordinates, Double> approximatedPosition = computeApproximatedPosition(aircraft, relativeDate);
        final long aircraftTableId = fetchAircraftTableId(recording.getId(), aircraft.getAircraftId());

        return new RecordingSwitch<AircraftState>() {

            @Override
            public AircraftState visitBaseStationRecording(Recording recording) {
                try {
                    return fetchBaseStationAircraftSnapshot(approximatedPosition, relativeDate, aircraftTableId);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public AircraftState visitSiteBaseStationRecording(Recording recording) {
                try {
                    return fetchBaseStationAircraftSnapshot(approximatedPosition, relativeDate, aircraftTableId);
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }
        }.doSwitch(recording);
    }

    private static AircraftState fetchBaseStationAircraftSnapshot(final Pair<Coordinates, Double> approximatedPosition,
                                                                  final long relativeDate,
                                                                  final long aircraftTableId) throws Exception {
        final String aircraftStatesTableName = "SBS_AIRCRAFT_STATES";
        return new BaseStationAircraftStateBuilder()
                .withPosition(approximatedPosition.getKey())
                .withAltitude(approximatedPosition.getValue())
                .withRelativeDate(relativeDate)
                .withGroundSpeed(getFirstDoubleValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "ground_speed"))
                .withTrack(getFirstDoubleValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "track"))
                .withVerticalRate(getFirstDoubleValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "vertical_rate"))
                .withSquawk(getFirstShortValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "squawk"))
                .withAlert(getFirstStringValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "alert"))
                .withEmergency(getFirstStringValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "emergency"))
                .withSpi(getFirstStringValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "spi"))
                .withIsOnGround(getFirstStringValueBeforeDate(aircraftStatesTableName,
                        aircraftTableId, relativeDate, "is_on_ground"))
                .build();
    }

    private static Double getFirstDoubleValueBeforeDate(final String aircraftStatesTableName,
                                                        final long aircraftTableId,
                                                        final long relativeDate,
                                                        final String column) throws Exception {
        return getFirstValueBeforeDate(aircraftStatesTableName, aircraftTableId, relativeDate, column,
                (ThrowableBiFunction<ResultSet, Integer, Double>)
                        (resultSet, integer) -> getDoubleOrNull(resultSet, 1));
    }

    private static Short getFirstShortValueBeforeDate(final String aircraftStatesTableName,
                                                      final long aircraftTableId,
                                                      final long relativeDate,
                                                      final String column) throws Exception {
        return getFirstValueBeforeDate(aircraftStatesTableName, aircraftTableId, relativeDate, column,
                (ThrowableBiFunction<ResultSet, Integer, Short>)
                        (resultSet, integer) -> getShortOrNull(resultSet, 1));
    }

    private static Integer getFirstIntValueBeforeDate(final String aircraftStatesTableName,
                                                      final long aircraftTableId,
                                                      final long relativeDate,
                                                      final String column) throws Exception {
        return getFirstValueBeforeDate(aircraftStatesTableName, aircraftTableId, relativeDate, column,
                (ThrowableBiFunction<ResultSet, Integer, Integer>)
                        (resultSet, integer) -> getIntOrNull(resultSet, 1));
    }

    private static Boolean getFirstBooleanValueBeforeDate(final String aircraftStatesTableName,
                                                          final long aircraftTableId,
                                                          final long relativeDate,
                                                          final String column) throws Exception {
        return getFirstValueBeforeDate(aircraftStatesTableName, aircraftTableId, relativeDate, column,
                (ThrowableBiFunction<ResultSet, Integer, Boolean>)
                        (resultSet, integer) -> getBooleanOrNull(resultSet, 1));
    }

    private static String getFirstStringValueBeforeDate(final String aircraftStatesTableName,
                                                        final long aircraftTableId,
                                                        final long relativeDate,
                                                        final String column) throws Exception {
        return getFirstValueBeforeDate(aircraftStatesTableName, aircraftTableId, relativeDate, column,
                (ThrowableBiFunction<ResultSet, Integer, String>)
                        (resultSet, integer) -> resultSet.getString(1));
    }

    private static <T> T getFirstValueBeforeDate(final String aircraftStatesTableName,
                                                 final long aircraftTableId,
                                                 final long relativeDate,
                                                 final String column,
                                                 final BiFunction<ResultSet, Integer, T> valueExtractor) throws Exception {
        return FDIT_DATABASE.executeQuery(
                getFirstNotNullValueBeforeDateQuery(aircraftStatesTableName, aircraftTableId, relativeDate, column),

                resultSet -> {
                    if (resultSet.first()) {
                        return valueExtractor.apply(resultSet, 1);
                    }
                    return null;
                });
    }

    private static String getFirstNotNullValueBeforeDateQuery(final String aircraftStatesTableName,
                                                              final long aircraftTableId,
                                                              final long relativeDate,
                                                              final String column) {
        return "select " + column + " from " + aircraftStatesTableName +
                " where aircraft_table_id  = " + aircraftTableId +
                " and " + column + " is not null" +
                " and relative_date <= " + relativeDate +
                " order by relative_date desc" +
                " limit 1";
    }

    private static long fetchAircraftTableId(final long recordingId, final int aircraftId) throws Exception {
        return FDIT_DATABASE.executeQuery(
                "select table_id from AIRCRAFTS where " +
                        "recording_id=" + recordingId + " and " +
                        "fdit_id=" + aircraftId,

                resultSet -> {
                    if (resultSet.first()) {
                        return resultSet.getLong("table_id");
                    }
                    throw new IllegalStateException(
                            "Not found aircraft " + aircraftId + " in database for recording " + recordingId);
                });
    }
}