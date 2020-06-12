package fdit.storage.recording;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.AircraftState;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.tools.date.DateUtils.computeRelativeDate;
import static java.util.Collections.synchronizedMap;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

public abstract class RecordingDatabaseFacade<STATE extends AircraftState> {

    private static final int MAX_PENDING_AIRCRAFT_STATES = 10000;
    protected final Map<Integer, Long> aircraftIdToTableId = synchronizedMap(newHashMap()); // keys : aircraftIds, values : AIRCRAFTS table ids
    private final File recordingFile;
    private final ExecutorService executorService = newSingleThreadExecutor();

    private final Collection<Aircraft> pendingAircrafts = newArrayList();
    private final Multimap<Integer, STATE> pendingAircraftStates = LinkedHashMultimap.create(); // keys : aircraftIDs
    private final Map<AircraftState, Long> pendingStateAbsoluteDates = newHashMap();
    private final Map<Integer, String> aircraftCallsigns = newHashMap();
    private long recordingId;
    protected long firstAbsoluteDate = Long.MAX_VALUE;
    private boolean determiningFirstAbsoluteDate = true;
    private long maxRelativeDate = Long.MIN_VALUE;

    protected RecordingDatabaseFacade(final File recordingFile) {
        this.recordingFile = recordingFile;
    }

    void pushRecording() throws Exception {
        recordingId = FDIT_DATABASE.executeInsertAndGetGeneratedId("insert into RECORDINGS(file_name, file_size)" +
                " values ('" + recordingFile.getName() + "', " + recordingFile.length() + ')');
    }

    void pushCallsign(final int aircraftId, final String callsign) {
        aircraftCallsigns.putIfAbsent(aircraftId, callsign);
    }

    void pushAircraft(final Aircraft aircraft) {
        pendingAircrafts.add(aircraft);
    }

    void pushAircraftState(final int aircraftId, final STATE state, final long stateDate) {
        if (determiningFirstAbsoluteDate) {
            pendingStateAbsoluteDates.put(state, stateDate);
            if (stateDate < firstAbsoluteDate) {
                firstAbsoluteDate = stateDate;
            }
        } else {
            updateStateRelativeDate(state, stateDate);
        }
        pendingAircraftStates.put(aircraftId, state);
        if (pendingAircraftStates.size() > MAX_PENDING_AIRCRAFT_STATES) {
            sendAllPendingToDatabase();
        }
    }

    private void updateAircraftCallsigns() throws Exception {
        final ArrayList<String> queries = newArrayList();
        for (final Map.Entry<Integer, String> entry : aircraftCallsigns.entrySet()) {
            queries.add("update AIRCRAFTS" +
                    " set call_sign='" + entry.getValue() + '\'' +
                    " where fdit_id=" + entry.getKey());
        }
        FDIT_DATABASE.executeUpdateRequests(queries.toArray(new String[0]));
    }

    void commit() throws Exception {
        sendAllPendingToDatabase();
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        FDIT_DATABASE.executeUpdateRequests("update RECORDINGS" +
                " set max_relative_date=" + maxRelativeDate +
                ", first_date=" + getFirstDateTimestamp() +
                " where file_name='" + recordingFile.getName() + '\'' +
                " and file_size=" + recordingFile.length());
        updateAircraftCallsigns();
        aircraftCallsigns.clear();
    }

    private void sendAllPendingToDatabase() {
        if (determiningFirstAbsoluteDate) {
            for (final STATE pendingState : pendingAircraftStates.values()) {
                updateStateRelativeDate(pendingState, pendingStateAbsoluteDates.get(pendingState));
            }
            determiningFirstAbsoluteDate = false;
            pendingStateAbsoluteDates.clear();
        }

        final Iterable<Aircraft> currentPendingAircrafts = newArrayList(pendingAircrafts);
        final Multimap<Integer, STATE> currentPendingAircraftStates = LinkedHashMultimap.create(pendingAircraftStates);
        executorService.submit(() -> {
            try {
                sendPendingAircraftsToDatabase(currentPendingAircrafts);
                sendPendingAircraftStatesToDatabase(currentPendingAircraftStates);
            } catch (final Exception e) {
                throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        });
        pendingAircrafts.clear();
        pendingAircraftStates.clear();
    }

    private void sendPendingAircraftsToDatabase(final Iterable<Aircraft> aircrafts) throws Exception {
        FDIT_DATABASE.executeInPreparedStatement(Statement.RETURN_GENERATED_KEYS,
                "insert into AIRCRAFTS(recording_id, fdit_id, icao, call_sign) values (?,?,?,?)",

                statement -> {
                    statement.setLong(1, recordingId);
                    for (final Aircraft aircraft : aircrafts) {
                        statement.setInt(2, aircraft.getAircraftId());
                        statement.setInt(3, aircraft.getIcao());
                        statement.setString(4, aircraft.getCallSign());
                        statement.addBatch();
                    }
                    statement.executeBatch();

                    final Iterator<Aircraft> aicraftsIterator = aircrafts.iterator();
                    try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        while (generatedKeys.next()) {
                            aircraftIdToTableId.put(aicraftsIterator.next().getAircraftId(), generatedKeys.getLong(1));
                        }
                    }
                });
    }

    protected abstract void sendPendingAircraftStatesToDatabase(final Multimap<Integer, STATE> aircraftStates) throws Exception;

    protected abstract long getFirstDateTimestamp();

    private void updateStateRelativeDate(final STATE state, final Long stateAbsoluteDate) {
        final long relativeDate = stateAbsoluteDate - firstAbsoluteDate;
        if (relativeDate > maxRelativeDate) {
            maxRelativeDate = relativeDate;
        }
        state.setRelativeDate(relativeDate);
    }

    void updateRelativeDate(final long firstMessageDate, final long lastMessageDate) {
        try {
            maxRelativeDate = computeRelativeDate(firstMessageDate, lastMessageDate);
        } catch (final ArithmeticException e) {
            maxRelativeDate = 0;
        }
    }
}