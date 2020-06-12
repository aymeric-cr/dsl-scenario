package fdit.metamodel.aircraft;

import javafx.util.Pair;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class InterpolatorTestLoader implements InterpolatorLoader {

    private final List<AircraftState> states = newArrayList();
    private final HashMap<AircraftCriterion, Collection<UnivariateFunction>> result = newHashMap();

    InterpolatorTestLoader(final Collection<AircraftState> states) {
        this.states.addAll(states);
        this.states.sort(comparingLong(AircraftState::getRelativeDate));
    }

    @Override
    public InterpolatorContentLoaderResult loadInterpolatorContent() {
        buildFunctions();
        return new InterpolatorContentLoaderResult(result);
    }

    @Override
    public String getAircraftCriterionField(AircraftCriterion criterion) {
        return null;
    }

    @Override
    public String getAircraftStatesTableName() {
        return null;
    }

    private void buildFunctions() {
        Pair<ArrayList<Double>, ArrayList<Double>> latitudePair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> longitudePair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> altitudePair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> groundSpeedPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> trackPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> verticalRatePair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> squawkPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> alertPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> emergencyPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> spiPair = new Pair<>(newArrayList(), newArrayList());
        Pair<ArrayList<Double>, ArrayList<Double>> isOnGroundPair = new Pair<>(newArrayList(), newArrayList());

        for (final AircraftState state : states) {
            if (state instanceof BaseStationAircraftState) {
                final BaseStationAircraftState bstState = (BaseStationAircraftState) state;
                int lastIndex = latitudePair.getKey().size() - 1;
                if (lastIndex == -1 || latitudePair.getKey().get(lastIndex) < bstState.getRelativeDate()) {
                    bstState.getPosition().ifPresent(coordinates -> {
                        latitudePair.getKey().add((double) bstState.getRelativeDate());
                        latitudePair.getValue().add(coordinates.getLatitude());
                    });
                }
                lastIndex = longitudePair.getKey().size() - 1;
                if (lastIndex == -1 || longitudePair.getKey().get(lastIndex) < bstState.getRelativeDate()) {
                    bstState.getPosition().ifPresent(coordinates -> {
                        longitudePair.getKey().add((double) bstState.getRelativeDate());
                        longitudePair.getValue().add(coordinates.getLongitude());
                    });
                }
                addOptionalState(altitudePair, bstState.getRelativeDate(), bstState.getAltitude());
                addOptionalState(groundSpeedPair, bstState.getRelativeDate(), bstState.getGroundSpeed());
                addOptionalState(trackPair, bstState.getRelativeDate(), bstState.getTrack());
                addOptionalState(verticalRatePair, bstState.getRelativeDate(), bstState.getVerticalRate());
                addOptionalState(squawkPair, bstState.getRelativeDate(), bstState.getSquawk());
                addOptionalState(alertPair, bstState.getRelativeDate(), bstState.getAlert());
                addOptionalState(emergencyPair, bstState.getRelativeDate(), bstState.getEmergency());
                addOptionalState(spiPair, bstState.getRelativeDate(), bstState.getSpi());
                addOptionalState(isOnGroundPair, bstState.getRelativeDate(), bstState.getIsOnGround());
            }
        }
        final AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator();
        createSplineFunction(latitudePair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.LATITUDE, singleton(polynomialSplineFunction)));
        createSplineFunction(longitudePair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.LONGITUDE, singleton(polynomialSplineFunction)));
        createSplineFunction(altitudePair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.ALTITUDE, singleton(polynomialSplineFunction)));
        createSplineFunction(groundSpeedPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.GROUNDSPEED, singleton(polynomialSplineFunction)));
        createSplineFunction(trackPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.TRACK, singleton(polynomialSplineFunction)));
        createSplineFunction(verticalRatePair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.VERTICAL_RATE, singleton(polynomialSplineFunction)));
        createSplineFunction(squawkPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.SQUAWK, singleton(polynomialSplineFunction)));
        createSplineFunction(alertPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.ALERT, singleton(polynomialSplineFunction)));
        createSplineFunction(emergencyPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.EMERGENCY, singleton(polynomialSplineFunction)));
        createSplineFunction(spiPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.SPI, singleton(polynomialSplineFunction)));
        createSplineFunction(isOnGroundPair, interpolator).ifPresent(polynomialSplineFunction ->
                result.put(AircraftCriterion.IS_ON_GROUND, singleton(polynomialSplineFunction)));
    }

    private Optional<PolynomialSplineFunction> createSplineFunction(final Pair<ArrayList<Double>, ArrayList<Double>> pair,
                                                                    final AkimaSplineInterpolator interpolator) {
        if (!pair.getValue().isEmpty()) {
            return of(interpolator.interpolate(
                    convertDoubles(pair.getKey()),
                    convertDoubles(pair.getValue())));
        }
        return empty();
    }

    private void addOptionalState(final Pair<ArrayList<Double>, ArrayList<Double>> pair,
                                  final long relativeDate,
                                  final Optional<?> field) {
        field.ifPresent(value -> {
            int lastIndex = pair.getKey().size() - 1;
            if (lastIndex == -1 || pair.getKey().get(lastIndex) < relativeDate) {
                pair.getKey().add((double) relativeDate);
                if (value instanceof Double) {
                    pair.getValue().add((Double) value);
                } else if (value instanceof String) {
                    pair.getValue().add(Double.parseDouble((String) value));
                }
            }
        });
    }

    private double[] convertDoubles(final List<Double> doubles) {
        double[] result = new double[doubles.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = doubles.get(i);
        }
        return result;
    }
}