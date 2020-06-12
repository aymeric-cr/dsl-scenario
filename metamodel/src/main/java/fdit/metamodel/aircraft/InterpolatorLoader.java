package fdit.metamodel.aircraft;

public interface InterpolatorLoader {

    int INTERPOLATION_LIMIT = 900000; // 5min

    InterpolatorContentLoaderResult loadInterpolatorContent() throws Exception;

    String getAircraftCriterionField(final AircraftCriterion criterion);

    String getAircraftStatesTableName();
}