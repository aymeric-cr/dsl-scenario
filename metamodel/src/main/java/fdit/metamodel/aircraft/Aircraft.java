package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.*;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.primitives.Doubles.asList;
import static fdit.metamodel.aircraft.AircraftCriterion.isStatusCriterion;
import static java.lang.Integer.toHexString;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.reverse;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class Aircraft {

    public static final int TIME_INTERVAL = 1000;
    private final int aircraftId;
    //TODO: extract ICAO and CALLSIGN to position?
    private final int icao;
    private InterpolatorLoader interpolatorLoader = null;
    private HashMap<AircraftCriterion, Collection<UnivariateFunction>> interpolationFunctionsMap = newHashMap();
    private String callSign = "";
    private final StaticProperties staticProperties = new StaticProperties();
    private long firstKnownAppearance = Long.MAX_VALUE;
    private long lastKnownAppearance = Long.MIN_VALUE;
    private int knownPositions = 0;

    public Aircraft(final int aircraftId,
                    final int icao) {
        this.aircraftId = aircraftId;
        this.icao = icao;
    }

    public Aircraft(final int aircraftId,
                    final int icao,
                    final InterpolatorLoader interpolatorLoader) {
        this(aircraftId, icao);
        this.interpolatorLoader = interpolatorLoader;
    }

    public void load() throws Exception {
        if (interpolatorLoader != null) {
            interpolationFunctionsMap.putAll(interpolatorLoader.loadInterpolatorContent().getResult());
        }
    }

    private void updateDate(long relativeDate) {
        firstKnownAppearance = min(relativeDate, firstKnownAppearance);
        lastKnownAppearance = max(relativeDate, lastKnownAppearance);
    }

    public void updatePosition(final long relativeDate,
                               final Double altitude,
                               final Double latitude,
                               final Double longitude) {
        if (latitude != null && longitude != null && altitude != null) {
            updateDate(relativeDate);
            knownPositions++;
            staticProperties.updateAltitudes(altitude);
            staticProperties.updateCoordinates(new Coordinates(latitude, longitude));
        }
    }

    public Map<AircraftCriterion, Collection<UnivariateFunction>> getInterpolationFunctionsMap() {
        return interpolationFunctionsMap;
    }

    public void setInterpolatorLoader(InterpolatorLoader interpolatorLoader) {
        this.interpolatorLoader = interpolatorLoader;
    }

    public int getKnownPositions() {
        return knownPositions;
    }

    public int getAircraftId() {
        return aircraftId;
    }

    public int getIcao() {
        return icao;
    }

    public String getCallSign() {
        return callSign;
    }

    public void setCallSign(final String callSign) {
        if (callSign != null) {
            this.callSign = callSign;
        }
    }

    public long getTimeOfFirstAppearance() {
        return firstKnownAppearance;
    }

    public long getTimeOfLastAppearance() {
        return lastKnownAppearance;
    }

    public String getStringICAO() {
        return toHexString(icao).toUpperCase();
    }

    public double query(long relativeDate, final AircraftCriterion criterion)
            throws NullLoaderException, OutOfDateException {
        if (interpolatorLoader == null) {
            throw new NullLoaderException();
        }
        if (hasInterpolationForCriterion(criterion)) {
            Optional<UnivariateFunction> optionalFunction = getFunctionAtDate(relativeDate, criterion);
            if (optionalFunction.isPresent()) {
                final UnivariateFunction function = optionalFunction.get();
                if (isStatusCriterion(criterion)) {
                    relativeDate = getPreviousKnot(function, relativeDate);
                }
                return function.value(relativeDate);
            } else {
                throw new OutOfDateException();
            }
        }
        return 0.0;
    }

    public boolean hasInterpolationForCriterion(final AircraftCriterion criterion) {
        return interpolationFunctionsMap.containsKey(criterion) && !interpolationFunctionsMap.get(criterion).isEmpty();
    }


    private Optional<UnivariateFunction> getFunctionAtDate(final double relativeDate,
                                                           final AircraftCriterion criterion) {
        for (final UnivariateFunction function : interpolationFunctionsMap.get(criterion)) {
            if (function instanceof PolynomialSplineFunction) {
                final PolynomialSplineFunction splineFunction = (PolynomialSplineFunction) function;
                if (splineFunction.getKnots()[0] <= relativeDate &&
                        relativeDate <= splineFunction.getKnots()[splineFunction.getN()]) {
                    return of(function);
                }
            }
            if (function instanceof PolynomialFunctionLagrangeForm) {
                final PolynomialFunctionLagrangeForm lagrangeForm = (PolynomialFunctionLagrangeForm) function;
                if (lagrangeForm.getInterpolatingPoints()[0] <= relativeDate &&
                        relativeDate <= lagrangeForm.getInterpolatingPoints()[lagrangeForm.getInterpolatingPoints().length - 1]) {
                    return of(function);
                }
            }
        }
        return empty();
    }

    private long getPreviousKnot(final UnivariateFunction function, final long x) {
        if (function instanceof PolynomialSplineFunction) {
            final PolynomialSplineFunction splineFunction = (PolynomialSplineFunction) function;
            final List<Double> knots = asList(splineFunction.getKnots());
            reverse(knots);
            for (double knot : knots) {
                if (knot <= x) return (long) knot;
            }
        }
        if (function instanceof PolynomialFunctionLagrangeForm) {
            final PolynomialFunctionLagrangeForm lagrangeForm = (PolynomialFunctionLagrangeForm) function;
            final List<Double> points = asList(lagrangeForm.getInterpolatingPoints());
            reverse(points);
            for (final double knot : points) {
                if (knot <= x) return (long) knot;
            }
        }
        return 0;
    }

    public double getFirstCriterionAppearance(final AircraftCriterion criterion) throws NullLoaderException {
        if (interpolatorLoader == null) {
            throw new NullLoaderException();
        }
        if (hasInterpolationForCriterion(criterion)) {
            final UnivariateFunction function = getFirst(interpolationFunctionsMap.get(criterion), null);
            if (function instanceof PolynomialSplineFunction) {
                final PolynomialSplineFunction splineFunction = (PolynomialSplineFunction) function;
                return splineFunction.getKnots()[0];
            }
            if (function instanceof PolynomialFunctionLagrangeForm) {
                final PolynomialFunctionLagrangeForm lagrangeForm = (PolynomialFunctionLagrangeForm) function;
                return lagrangeForm.getInterpolatingPoints()[0];
            }
        }
        return -1;
    }

    public double getLastCriterionAppearance(final AircraftCriterion criterion) throws NullLoaderException {
        if (interpolatorLoader == null) {
            throw new NullLoaderException();
        }
        if (hasInterpolationForCriterion(criterion)) {
            final UnivariateFunction function = getLast(interpolationFunctionsMap.get(criterion));
            if (function instanceof PolynomialSplineFunction) {
                final PolynomialSplineFunction splineFunction = (PolynomialSplineFunction) function;
                return splineFunction.getKnots()[splineFunction.getN()];
            }
            if (function instanceof PolynomialFunctionLagrangeForm) {
                final PolynomialFunctionLagrangeForm lagrangeForm = (PolynomialFunctionLagrangeForm) function;
                return lagrangeForm.getInterpolatingPoints()[lagrangeForm.getInterpolatingPoints().length - 1];
            }
        }
        return -1;
    }

    public StaticProperties getStaticProperties() {
        return staticProperties;
    }

    @Override
    public String toString() {
        if (callSign != null && !callSign.isEmpty()) {
            return callSign.toUpperCase();
        }
        return getStringICAO();
    }
}