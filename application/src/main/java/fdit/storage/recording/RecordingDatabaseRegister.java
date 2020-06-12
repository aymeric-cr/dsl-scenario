package fdit.storage.recording;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.AircraftState;
import fdit.metamodel.aircraft.BaseStationAircraftState;
import fdit.metamodel.aircraft.StaticProperties;
import fdit.metamodel.coordinates.Coordinates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.metamodel.aircraft.AircraftUtils.MINIMUM_KNOWN_POSITIONS;
import static org.apache.commons.lang.StringUtils.isBlank;

class RecordingDatabaseRegister<MESSAGE, STATE extends AircraftState> {

    private final File recordingFile;
    private final RecordingMessageProcessor<MESSAGE, STATE> messageProcessor;
    private final RecordingDatabaseFacade<STATE> recordingDatabaseFacade;

    private final Collection<Integer> aircraftsInDatabase = newArrayList(); // aircraftIds
    private final Map<Integer, Aircraft> aircraftsNotYetInDatabase = newHashMap(); // keys are aircraftIds
    private final Map<STATE, Long> statesNotYetInDatabaseDates = newHashMap();
    private final Map<Aircraft, Collection<AircraftState>> aircraftStates = newHashMap();

    private long firstMessageDate = Long.MAX_VALUE;
    private long lastMessageDate = Long.MIN_VALUE;

    private RecordingDatabaseRegister(final File recordingFile,
                                      final RecordingMessageProcessor<MESSAGE, STATE> messageProcessor,
                                      final RecordingDatabaseFacade<STATE> recordingDatabaseFacade) {
        this.recordingFile = recordingFile;
        this.messageProcessor = messageProcessor;
        this.recordingDatabaseFacade = recordingDatabaseFacade;
    }

    static <T, STATE extends AircraftState> void registerRecordingInDatabase(final File recordingFile,
                                                                             final RecordingMessageProcessor<T, STATE> messageProcessor,
                                                                             final RecordingDatabaseFacade<STATE> recordingDatabaseFacade) throws Exception {
        new RecordingDatabaseRegister(recordingFile, messageProcessor, recordingDatabaseFacade).doRegisterInDatabase();
    }

    private static boolean canInsertAircraftInDatabase(final Aircraft aircraft) {
        return aircraft.getKnownPositions() >= MINIMUM_KNOWN_POSITIONS;
    }

    private void doRegisterInDatabase() throws Exception {
        recordingDatabaseFacade.pushRecording();
        try (final FileReader fileReader = new FileReader(recordingFile);
             final BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String currentMessage = bufferedReader.readLine();
            while (currentMessage != null) {
                try {
                    handleMessage(currentMessage);
                } catch (final Exception ignored) {

                    // malformed message
                }
                currentMessage = bufferedReader.readLine();
            }
        }
        recordingDatabaseFacade.updateRelativeDate(firstMessageDate, lastMessageDate);
        recordingDatabaseFacade.commit();
    }

    private void handleMessage(final String originalMessage) {
        final Optional<MESSAGE> messageOpt = messageProcessor.messagePreprocessing(originalMessage);
        if (!messageOpt.isPresent()) {
            return;
        }
        final MESSAGE message = messageOpt.get();
        final int aircraftId = messageProcessor.extractAircraftId(message);
        final long messageDate = messageProcessor.extractMessageDate(message);
        updateMessageDate(messageDate);
        final boolean aircraftInDatabase = aircraftsInDatabase.contains(aircraftId);
        if (!aircraftInDatabase && !aircraftsNotYetInDatabase.containsKey(aircraftId)) {
            aircraftsNotYetInDatabase.put(aircraftId, new Aircraft(aircraftId, messageProcessor.extractIcao(message)));
        }
        if (!aircraftInDatabase) {
            final Aircraft aircraft = aircraftsNotYetInDatabase.get(aircraftId);
            if (isBlank(aircraft.getCallSign())) {
                messageProcessor.extractCallSign(message).ifPresent(aircraft::setCallSign);
            }
        }
        messageProcessor.extractCallSign(message).ifPresent(callsign -> recordingDatabaseFacade.pushCallsign(aircraftId, callsign));
        final Optional<STATE> stateOpt = messageProcessor.extractAircraftState(message);
        if (stateOpt.isPresent()) {
            final STATE state = stateOpt.get();
            if (aircraftInDatabase) {
                recordingDatabaseFacade.pushAircraftState(aircraftId, state, messageDate);
            } else {
                final Aircraft aircraft = aircraftsNotYetInDatabase.get(aircraftId);
                updateAircraft(state, aircraft);
                aircraftStates.putIfAbsent(aircraft, newArrayList());
                aircraftStates.get(aircraft).add(state);
                statesNotYetInDatabaseDates.put(state, messageDate);
            }
        }
        if (!aircraftsInDatabase.contains(aircraftId)) {
            final Aircraft aircraft = aircraftsNotYetInDatabase.get(aircraftId);
            if (canInsertAircraftInDatabase(aircraft)) {
                pushAircraftToDatabase(aircraft);
            }
        }
    }

    private void updateAircraft(final STATE state, final Aircraft aircraft) {
        final Optional<Double> altitude = state.getAltitude();
        final Optional<Coordinates> position = state.getPosition();
        if (altitude.isPresent() && position.isPresent()) {
            aircraft.updatePosition(
                    state.getRelativeDate(),
                    altitude.get(),
                    position.get().getLatitude(),
                    position.get().getLongitude());
        }
    }

    private void updateMessageDate(final long dateTime) {
        if (dateTime < firstMessageDate) {
            firstMessageDate = dateTime;
        } else if (dateTime > lastMessageDate) {
            lastMessageDate = dateTime;
        }
    }

    private void pushAircraftToDatabase(final Aircraft aircraft) {
        recordingDatabaseFacade.pushAircraft(aircraft);
        for (final AircraftState state : aircraftStates.get(aircraft)) {
            recordingDatabaseFacade.pushAircraftState(aircraft.getAircraftId(),
                    (STATE) state, statesNotYetInDatabaseDates.remove(state));
        }
        final int aircraftId = aircraft.getAircraftId();
        aircraftsInDatabase.add(aircraftId);
        aircraftsNotYetInDatabase.remove(aircraftId);
    }
}