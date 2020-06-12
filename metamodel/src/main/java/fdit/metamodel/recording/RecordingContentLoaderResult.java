package fdit.metamodel.recording;

import fdit.metamodel.aircraft.Aircraft;

import java.util.Collection;

public class RecordingContentLoaderResult {

    private final long id;
    private final Collection<Aircraft> aircrafts;
    private final long maxRelativeDate;
    private long firstDate = Long.MAX_VALUE;


    public RecordingContentLoaderResult(final long id,
                                        final Collection<Aircraft> aircrafts,
                                        final long maxRelativeDate,
                                        final long firstDate) {
        this.id = id;
        this.aircrafts = aircrafts;
        this.maxRelativeDate = maxRelativeDate;
        this.firstDate = firstDate;
    }

    public RecordingContentLoaderResult(final long id,
                                        final Collection<Aircraft> aircrafts,
                                        final long maxRelativeDate) {
        this.id = id;
        this.aircrafts = aircrafts;
        this.maxRelativeDate = maxRelativeDate;
    }

    long getId() {
        return id;
    }

    Collection<Aircraft> getAircrafts() {
        return aircrafts;
    }

    long getMaxRelativeDate() {
        return maxRelativeDate;
    }

    public long getFirstDate() {
        return firstDate;
    }
}