package fdit.metamodel.alteration.scope;

public class GeoThreshold implements Scope {

    private final String thresholdType;
    private final String threshold;
    private final String boundType;

    public GeoThreshold(final String thresholdType, final String threshold, final String boundType) {
        this.thresholdType = thresholdType;
        this.threshold = threshold;
        this.boundType = boundType;
    }

    public String getThresholdType() {
        return thresholdType;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getBoundType() {
        return boundType;
    }
}
