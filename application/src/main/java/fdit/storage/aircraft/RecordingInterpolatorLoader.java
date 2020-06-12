package fdit.storage.aircraft;

import fdit.metamodel.aircraft.AircraftCriterion;
import fdit.metamodel.aircraft.InterpolatorContentLoaderResult;
import fdit.metamodel.aircraft.InterpolatorLoader;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

import java.io.File;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.database.ResultSetUtils.getDoubleOrNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

abstract class RecordingInterpolatorLoader implements InterpolatorLoader {

    protected final File recordingFile;
    protected final int aircraftId;
    private final UnivariateInterpolator interpolator = new AkimaSplineInterpolator();

    RecordingInterpolatorLoader(final File recording,
                                final int aircraftId) {
        this.recordingFile = recording;
        this.aircraftId = aircraftId;
    }

    private static long fetchAircraftTableId(final long recordingId,
                                             final long aircraftId) throws Exception {
        final long[] aircraftTableId = new long[1];
        FDIT_DATABASE.executeQuery(
                "select table_id from AIRCRAFTS" +
                        " where recording_id=" + recordingId +
                        " and fdit_id=" + aircraftId,
                resultSet -> {
                    while (resultSet.next()) {
                        aircraftTableId[0] = resultSet.getLong("table_id");
                    }
                });
        return aircraftTableId[0];
    }

    private static long fetchRecordingId(final File recordingFile) throws Exception {
        return FDIT_DATABASE.executeQuery(
                "select id from RECORDINGS" +
                        " where file_name='" + recordingFile.getName() + '\'' +
                        " and file_size=" + recordingFile.length(),
                resultSet -> {
                    resultSet.first();
                    return resultSet.getLong("id");
                });
    }

    InterpolatorContentLoaderResult loadFromInDatabase() throws Exception {
        final long recordingId = fetchRecordingId(recordingFile);
        final long aircraftTableId = fetchAircraftTableId(recordingId, aircraftId);
        return new InterpolatorContentLoaderResult(fetchAllAircraftCriteria(aircraftTableId));
    }

    private HashMap<AircraftCriterion, Collection<UnivariateFunction>> fetchAllAircraftCriteria(final long aircraftTableId) throws Exception {
        final HashMap<AircraftCriterion, Collection<UnivariateFunction>> result = newHashMap();
        for (final AircraftCriterion criterion : AircraftCriterion.values()) {
            fetchAircraftCriterion(aircraftTableId, criterion).ifPresent(polynomialSplineFunction ->
                    result.put(criterion, polynomialSplineFunction));
        }
        return result;
    }

    private Optional<Collection<UnivariateFunction>> fetchAircraftCriterion(final long aircraftTableId,
                                                                            final AircraftCriterion criterion) throws Exception {
        final String criterionField = getAircraftCriterionField(criterion);
        if (criterionField.isEmpty()) {
            return empty();
        }
        final ArrayList<UnivariateFunction> univariateFunctions = newArrayList();
        FDIT_DATABASE.executeQuery(
                "select relative_date, min(" + criterionField + ") as " + criterionField +
                        " from " + getAircraftStatesTableName() +
                        " where aircraft_table_id=" + aircraftTableId +
                        " and " + criterionField + " is not null" +
                        " group by " + " relative_date" +
                        " order by " + " relative_date",
                resultSet -> {
                    double previousDate = -1;
                    final List<Double> dates = newArrayList();
                    final List<Double> values = newArrayList();
                    while (resultSet.next()) {
                        Double value;
                        double date;
                        try {
                            value = getDoubleOrNull(resultSet, criterionField);
                        } catch (final Exception ignore) {
                            continue;
                        }
                        date = resultSet.getDouble("relative_date");

                        if (date - previousDate < INTERPOLATION_LIMIT) {
                            values.add(value);
                            dates.add(date);
                        }
                        if (date - previousDate > INTERPOLATION_LIMIT || resultSet.isLast()) {
                            if (dates.size() > 4) {
                                try {
                                    univariateFunctions.add(
                                            interpolator.interpolate(
                                                    convertDoubles(dates),
                                                    convertDoubles(values)));
                                } catch (Exception ignored) {

                                }
                            }
                            values.clear();
                            dates.clear();
                            if (!resultSet.isLast()) {
                                values.add(value);
                                dates.add(date);
                            }
                        }
                        previousDate = date;
                    }
                });
        return of(univariateFunctions);
    }

    private double[] convertDoubles(final List<Double> doubles) {
        double[] result = new double[doubles.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = doubles.get(i);
        }
        return result;
    }
}