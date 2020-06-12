package fdit.metamodel.recording;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.element.LazyFditElement;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

public class Recording extends LazyFditElement {

    public static final Recording EMPTY_RECORDING = new Recording("", null);

    private final RecordingContentLoader loader;
    private final Collection<Aircraft> aircrafts = newArrayList();
    private long id;
    private long maxRelativeDate = -1;
    private long firstDate = Long.MAX_VALUE;

    public Recording(final String name, final RecordingContentLoader loader) {
        super(name);
        this.loader = loader;
    }

    public long getId() {
        return id;
    }

    public Collection<Aircraft> getAircrafts() {
        if (!isLoaded()) {
            throw new IllegalStateException("Recording " + getName() + " is not loaded");
        }
        return aircrafts;
    }

    public long getMaxRelativeDate() {
        if (!isLoaded()) {
            throw new IllegalStateException("Recording " + getName() + " is not loaded");
        }
        return maxRelativeDate;
    }

    public long getFirstDate() {
        if (!isLoaded()) {
            throw new IllegalStateException("Recording " + getName() + " is not loaded");
        }
        return firstDate;
    }

    @Override
    protected void loadContent() throws Exception {
        final RecordingContentLoaderResult loadingResult = loader.loadRecordingContent();
        id = loadingResult.getId();
        aircrafts.clear();
        aircrafts.addAll(loadingResult.getAircrafts());
        for (final Aircraft aircraft : aircrafts) {
            aircraft.load();
        }
        maxRelativeDate = loadingResult.getMaxRelativeDate();
        firstDate = loadingResult.getFirstDate();
    }

    @Override
    protected void unloadContent() {
        aircrafts.clear();
    }

    public interface RecordingSwitch<T> {

        T visitBaseStationRecording(final Recording recording);

        T visitSiteBaseStationRecording(final Recording recording);

        default T visitDefault() {
            throw new IllegalArgumentException("Unknown recording type");
        }

        default T doSwitch(final Recording recording) {
            if (recording instanceof SiteBaseStationRecording) {
                return visitSiteBaseStationRecording(recording);
            }
            if (recording instanceof BaseStationRecording) {
                return visitBaseStationRecording(recording);
            }
            return visitDefault();
        }
    }
}