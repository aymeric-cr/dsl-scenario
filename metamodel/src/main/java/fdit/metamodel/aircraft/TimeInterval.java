package fdit.metamodel.aircraft;

public class TimeInterval {

    private static long TIME_INTERVAL_MINIMUM_SIZE = 1000;

    private long start;
    private long end;

    public TimeInterval(final long start, final long end) {
        assert (end - start >= TIME_INTERVAL_MINIMUM_SIZE);
        this.start = start;
        this.end = end;
    }

    public TimeInterval(final long start) {
        this.start = start;
        this.end = start + TIME_INTERVAL_MINIMUM_SIZE;
    }

    public static void setMinimumSize(long size) {
        TIME_INTERVAL_MINIMUM_SIZE = size;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(final long end) {
        assert (end - start >= TIME_INTERVAL_MINIMUM_SIZE);
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        assert (end - start >= TIME_INTERVAL_MINIMUM_SIZE);
        this.start = start;
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