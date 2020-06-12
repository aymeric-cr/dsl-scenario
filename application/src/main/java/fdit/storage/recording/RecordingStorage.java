package fdit.storage.recording;

import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.storage.recording.baseStation.BstContentLoader;
import fdit.storage.recording.baseStation.SbsContentLoader;

import java.io.File;
import java.util.Optional;

import static fdit.storage.FditElementExtensions.getElementTypeFrom;
import static fdit.storage.FditStorageUtils.buildFditElementName;

public final class RecordingStorage {

    private RecordingStorage() {
    }

    public static Recording loadRecording(final File recordingFile, final RecordingInDatabaseLoadingCallback callback) {
        final Optional<Class<? extends FditElement>> type = getElementTypeFrom(recordingFile);
        if (type.isPresent()) {
            if (type.get().equals(SiteBaseStationRecording.class)) {
                return new SiteBaseStationRecording(buildFditElementName(recordingFile),
                        new SbsContentLoader(recordingFile, callback));
            }
            if (type.get().equals(BaseStationRecording.class)) {
                return new BaseStationRecording(buildFditElementName(recordingFile),
                        new BstContentLoader(recordingFile, callback));
            }
            throw new RuntimeException("Unknown recording type");
        }
        throw new RuntimeException("Cannot determine Recording type");
    }
}