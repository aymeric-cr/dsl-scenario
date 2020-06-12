package fdit.storage.recording;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.InterpolatorLoader;
import fdit.metamodel.aircraft.StaticProperties;
import fdit.metamodel.recording.RecordingContentLoader;
import fdit.metamodel.recording.RecordingContentLoaderResult;
import javafx.util.Pair;

import java.io.File;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.database.ResultSetUtils.getDoubleOrNull;
import static fdit.storage.recording.RecordingDatabaseRegister.registerRecordingInDatabase;

public abstract class RecordingInDatabaseContentLoader implements RecordingContentLoader {

    protected final File recordingFile;
    private final RecordingInDatabaseLoadingCallback callback;

    protected RecordingInDatabaseContentLoader(final File recordingFile,
                                               final RecordingInDatabaseLoadingCallback callback) {
        this.recordingFile = recordingFile;
        this.callback = callback;
    }

    private static boolean isRecordingRegistered(final File recordingFile) throws Exception {
        return FDIT_DATABASE.executeQuery("select * from RECORDINGS" +
                        " where file_name='" + recordingFile.getName() + '\'' +
                        " and file_size=" + recordingFile.length(),
                ResultSet::first);
    }

    private static Pair<Long, Long> fetchRecordingIdAndMaxRelativeDate(final File recordingFile) throws Exception {
        return FDIT_DATABASE.executeQuery(
                "select id, max_relative_date from RECORDINGS" +
                        " where file_name='" + recordingFile.getName() + '\'' +
                        " and file_size=" + recordingFile.length(),
                resultSet -> {
                    resultSet.first();
                    return new Pair<>(resultSet.getLong("id"), resultSet.getLong("max_relative_date"));
                });
    }

    private static long fetchRecordingFirstDate(final File recordingFile) throws Exception {
        return FDIT_DATABASE.executeQuery(
                "select first_date from RECORDINGS" +
                        " where file_name='" + recordingFile.getName() + '\'' +
                        " and file_size=" + recordingFile.length(),
                resultSet -> {
                    resultSet.first();
                    return resultSet.getLong("first_date");
                });
    }

    @Override
    public RecordingContentLoaderResult loadRecordingContent() throws Exception {
        return loadRecordingContentInDatabase(recordingFile);
    }

    protected RecordingContentLoaderResult loadRecordingContentInDatabase(final File recordingContentFile) throws Exception {
        callback.onDatabaseLoadingStarted();
        if (!isRecordingRegistered(recordingFile)) {
            callback.onDatabaseRegisteringStarted();
            registerRecordingInDatabase(recordingContentFile,
                    getRecordingMessageProcessor(),
                    getRecordingDatabaseFacade(recordingFile));
            callback.onDatabaseRegisteringEnded();
        }
        final Pair<Long, Long> recordingFetchResult = fetchRecordingIdAndMaxRelativeDate(recordingFile);
        final long recordingId = recordingFetchResult.getKey();
        final RecordingContentLoaderResult result = new RecordingContentLoaderResult(
                recordingId,
                fetchAircraftsKnownPositions(recordingId),
                recordingFetchResult.getValue(),
                fetchRecordingFirstDate(recordingFile));
        callback.onDatabaseLoadingEnded();
        return result;
    }

    protected abstract RecordingMessageProcessor getRecordingMessageProcessor();

    protected abstract InterpolatorLoader getStateInterpolatorContentLoader(final int aircraftId);

    protected abstract RecordingDatabaseFacade getRecordingDatabaseFacade(final File recordingFile);

    protected abstract String getAircraftStatesTableName();

    private Collection<Aircraft> fetchAircraftsKnownPositions(final long recordingId) throws Exception {
        final Map<Long, Aircraft> aircrafts = fetchAircrafts(recordingId);
        for (final Entry<Long, Aircraft> aircraftEntry : aircrafts.entrySet()) {
            FDIT_DATABASE.executeQuery(
                    "select relative_date, " + getFields() +
                            " from " + getAircraftStatesTableName() +
                            " where aircraft_table_id=" + aircraftEntry.getKey() +
                            " order by relative_date",
                    resultSet -> {
                        while (resultSet.next()) {
                            final Aircraft aircraft = aircraftEntry.getValue();
                            aircraft.updatePosition(
                                    resultSet.getLong("relative_date"),
                                    getDoubleOrNull(resultSet, "altitude"),
                                    getDoubleOrNull(resultSet, "latitude"),
                                    getDoubleOrNull(resultSet, "longitude"));
                            final StaticProperties staticProperties = aircraft.getStaticProperties();
                            staticProperties.updateGroundSpeeds(getDoubleOrNull(resultSet, "ground_speed"));
                            staticProperties.updateVerticalRates(getDoubleOrNull(resultSet, "vertical_rate"));
                            staticProperties.updateTracks(getDoubleOrNull(resultSet, "track"));
                        }
                    });
        }
        return aircrafts.values();
    }

    protected abstract String getFields();

    private Map<Long, Aircraft> fetchAircrafts(final long recordingId) throws Exception {
        final Map<Long, Aircraft> aircrafts = newHashMap();
        FDIT_DATABASE.executeQuery(
                "select table_id, fdit_id, icao, call_sign from AIRCRAFTS" +
                        " where recording_id=" + recordingId,
                resultSet -> {
                    while (resultSet.next()) {
                        final Aircraft aircraft = new Aircraft(
                                resultSet.getInt("fdit_id"),
                                resultSet.getInt("icao"),
                                getStateInterpolatorContentLoader(resultSet.getInt("fdit_id")));
                        aircraft.setCallSign(resultSet.getString("call_sign"));
                        aircrafts.put(resultSet.getLong("table_id"), aircraft);
                    }
                });
        return aircrafts;
    }
}