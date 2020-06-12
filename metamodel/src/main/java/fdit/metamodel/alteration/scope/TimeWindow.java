package fdit.metamodel.alteration.scope;

public class TimeWindow implements Scope {

    private final long lowerBoundMillis;
    private final long upperBoundMillis;

    public TimeWindow(final long lowerBoundMillis, final long upperBoundMillis) {
        this.lowerBoundMillis = lowerBoundMillis;
        this.upperBoundMillis = upperBoundMillis;
    }

    public long getLowerBoundMillis() {
        return lowerBoundMillis;
    }

    public long getUpperBoundMillis() {
        return upperBoundMillis;
    }
}