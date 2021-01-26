package fdit.metamodel.aircraft;

public class TimeInterval {

    public enum IntervalType {
        UNSET,
        TRUE,
        FALSE,
        NULL
    }

    public static final long TIME_INTERVAL_MINIMUM_SIZE = 1000;

    private long start;
    private long end;
    private IntervalType type = IntervalType.UNSET;

    public TimeInterval(final long start, final long end) {
        this.start = start;
        this.end = end;
    }

    public TimeInterval(final long start, final long end, final IntervalType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public TimeInterval(final long start) {
        this.start = start;
        this.end = start + TIME_INTERVAL_MINIMUM_SIZE;
    }

    public TimeInterval(final long start, final IntervalType type) {
        this.start = start;
        this.end = start + TIME_INTERVAL_MINIMUM_SIZE;
        this.type = type;
    }

    public IntervalType getType() {
        return type;
    }

    public void setType(IntervalType type) {
        this.type = type;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    public long getDuration() {
        return end - start;
    }

    public boolean equals(Object timeint) {
        if (!(timeint instanceof TimeInterval)) return false;
        return ((TimeInterval) timeint).getStart() == start && ((TimeInterval) timeint).getEnd() == end;
    }

    public int hashCode() {
        int result = 39;
        return (int) (result * (start + end));
    }
}