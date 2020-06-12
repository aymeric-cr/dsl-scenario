package fdit.metamodel.aircraft;

import org.apache.commons.math3.analysis.UnivariateFunction;

import java.util.Collection;
import java.util.HashMap;

public class InterpolatorContentLoaderResult {

    private final HashMap<AircraftCriterion, Collection<UnivariateFunction>> result;

    public InterpolatorContentLoaderResult(final HashMap<AircraftCriterion, Collection<UnivariateFunction>> result) {
        this.result = result;
    }

    public HashMap<AircraftCriterion, Collection<UnivariateFunction>> getResult() {
        return result;
    }
}