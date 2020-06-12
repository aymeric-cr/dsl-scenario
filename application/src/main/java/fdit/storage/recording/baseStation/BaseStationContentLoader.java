package fdit.storage.recording.baseStation;

import fdit.storage.recording.RecordingDatabaseFacade;
import fdit.storage.recording.RecordingInDatabaseContentLoader;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;

import java.io.File;

public abstract class BaseStationContentLoader extends RecordingInDatabaseContentLoader {

    public BaseStationContentLoader(final File recordingFile, final RecordingInDatabaseLoadingCallback callback) {
        super(recordingFile, callback);
    }

    @Override
    protected RecordingDatabaseFacade getRecordingDatabaseFacade(final File recordingFile) {
        return new BaseStationRecordingDatabaseFacade(this.recordingFile);
    }

    @Override
    protected String getAircraftStatesTableName() {
        return "SBS_AIRCRAFT_STATES";
    }

    @Override
    protected String getFields() {
        return "altitude, latitude, longitude, track, ground_speed, vertical_rate";
    }
}