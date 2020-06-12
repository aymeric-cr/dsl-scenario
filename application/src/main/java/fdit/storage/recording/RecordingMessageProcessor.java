package fdit.storage.recording;

import fdit.metamodel.aircraft.AircraftState;

import java.util.Optional;

public interface RecordingMessageProcessor<MESSAGE, STATE extends AircraftState> {

    Optional<MESSAGE> messagePreprocessing(final String message);

    int extractAircraftId(final MESSAGE message);

    int extractIcao(final MESSAGE message);

    Optional<String> extractCallSign(final MESSAGE message);

    Optional<STATE> extractAircraftState(final MESSAGE message);

    long extractMessageDate(final MESSAGE message);
}
