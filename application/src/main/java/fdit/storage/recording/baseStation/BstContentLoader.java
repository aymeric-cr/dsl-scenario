package fdit.storage.recording.baseStation;

import fdit.metamodel.aircraft.InterpolatorLoader;
import fdit.storage.aircraft.BstInterpolatorLoader;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;
import fdit.storage.recording.RecordingMessageProcessor;

import java.io.File;

public class BstContentLoader extends BaseStationContentLoader {

    public BstContentLoader(final File recordingFile, final RecordingInDatabaseLoadingCallback callback) {
        super(recordingFile, callback);
    }

    @Override
    protected InterpolatorLoader getStateInterpolatorContentLoader(final int aircraftId) {
        return new BstInterpolatorLoader(recordingFile, aircraftId);
    }

    @Override
    protected RecordingMessageProcessor getRecordingMessageProcessor() {
        return new BstMessageProcessor();
    }
}