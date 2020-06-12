package fdit.metamodel.aircraft;

import fdit.metamodel.coordinates.Coordinates;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static fdit.tools.predicate.PredicateUtils.and;
import static java.util.Arrays.asList;

public final class AircraftHelper {
    private AircraftHelper() {
    }

    @SafeVarargs
    public static Aircraft aircraft(final int id, final int icao, final Consumer<Aircraft>... consumers) {
        final Aircraft aircraft = new Aircraft(id, icao);
        acceptAll(aircraft, consumers);
        return aircraft;
    }

    public static Aircraft loadedAircraft(final int id,
                                          final int icao,
                                          final AircraftState... states) throws Exception {
        final Aircraft aircraft = aircraft(id, icao);
        asList(states).forEach(aircraftState -> {
            aircraft.updatePosition(
                    aircraftState.getRelativeDate(),
                    aircraftState.getAltitude().get(),
                    aircraftState.getPosition().get().getLatitude(),
                    aircraftState.getPosition().get().getLongitude());
            final StaticProperties staticProperties = aircraft.getStaticProperties();
            if (aircraftState instanceof BaseStationAircraftState) {
                ((BaseStationAircraftState) aircraftState).getVerticalRate().ifPresent(staticProperties::updateVerticalRates);
                ((BaseStationAircraftState) aircraftState).getGroundSpeed().ifPresent(staticProperties::updateGroundSpeeds);
                ((BaseStationAircraftState) aircraftState).getTrack().ifPresent(staticProperties::updateTracks);
            }
        });
        aircraft.setInterpolatorLoader(new InterpolatorTestLoader(asList(states)));
        aircraft.load();
        return aircraft;
    }

    public static AircraftState baseStationState(final Coordinates coordinates,
                                                 final double altitude,
                                                 final long relativeDate) {
        return new BaseStationAircraftStateBuilder()
                .withPosition(coordinates)
                .withAltitude(altitude)
                .withRelativeDate(relativeDate)
                .build();
    }

    public static AircraftState baseStationState(final Coordinates coordinates,
                                                 final double altitude,
                                                 final double track,
                                                 final double groundspeed,
                                                 final double verticalRate,
                                                 final long relativeDate) {
        return new BaseStationAircraftStateBuilder()
                .withPosition(coordinates)
                .withAltitude(altitude)
                .withTrack(altitude)
                .withGroundSpeed(altitude)
                .withVerticalRate(verticalRate)
                .withTrack(track)
                .withGroundSpeed(groundspeed)
                .withRelativeDate(relativeDate)
                .build();
    }

    @SafeVarargs
    public static Predicate<Aircraft> anAircraft(final Predicate<Aircraft>... constraints) {
        return and(constraints);
    }

    @SafeVarargs
    public static Predicate<Aircraft> withFunctions(final AircraftCriterion criterion,
                                                    final Predicate<UnivariateFunction>... functions) {
        return vessel -> containsOnly(functions).test(vessel.getInterpolationFunctionsMap().get(criterion));
    }

    @SafeVarargs
    public static Predicate<UnivariateFunction> aFunction(final Predicate<UnivariateFunction>... functionPredicates) {
        return and(functionPredicates);
    }

    public static Predicate<UnivariateFunction> withKnots(int knots) {
        return univariateFunction -> {
            if (univariateFunction instanceof PolynomialSplineFunction &&
                    ((PolynomialSplineFunction) univariateFunction).getN() == knots - 1) {
                return true;
            } else {
                System.err.println("Bad number of knots");
                return false;
            }
        };
    }

    public static Predicate<UnivariateFunction> from(double startTime) {
        return function -> {
            if (function instanceof PolynomialSplineFunction &&
                    ((PolynomialSplineFunction) function).getKnots()[0] == startTime) {
                return true;
            } else {
                System.err.println("Expected start time: " + startTime + ". Got: " +
                        ((PolynomialSplineFunction) function).getKnots()[0]);
                return false;
            }
        };
    }

    public static Predicate<UnivariateFunction> to(double endTime) {
        return univariateFunction -> {
            if (univariateFunction instanceof PolynomialSplineFunction &&
                    ((PolynomialSplineFunction) univariateFunction).getKnots()[((PolynomialSplineFunction) univariateFunction).getN()] == endTime) {
                return true;
            } else {
                System.err.println("Expected end time: " + endTime + ". Got: " +
                        ((PolynomialSplineFunction) univariateFunction).getKnots()[((PolynomialSplineFunction) univariateFunction).getN()]);
                return false;
            }
        };
    }

    public static Predicate<Aircraft> withoutFunction(final AircraftCriterion criterion) {
        return aircraft -> {
            if (!aircraft.hasInterpolationForCriterion(criterion)) {
                return true;
            } else {
                System.err.println("Function does exist for the criterion " + criterion.toString());
                return false;
            }
        };
    }

    public static Predicate<Aircraft> id(final int id) {
        return aircraft -> {
            if (aircraft.getAircraftId() == id) {
                return true;
            } else {
                System.err.println("Expected Aircraft ID: " + id + ". Got:" + aircraft.getAircraftId());
                return false;
            }
        };
    }

    public static Predicate<Aircraft> icao(final int icao) {
        return aircraft -> {
            if (aircraft.getIcao() == icao) {
                return true;
            } else {
                System.err.println("Expected ICAO: " + icao + ". Got: " + aircraft.getIcao());
                return false;
            }
        };
    }

    public static Predicate<Aircraft> callsign(final String callsign) {
        return aircraft -> aircraft.getCallSign().equals(callsign);
    }

    public static Predicate<Aircraft> firstAppearance(final long firstAppearance) {
        return aircraft -> {
            if (aircraft.getTimeOfFirstAppearance() != firstAppearance) {
                System.err.println("Expected first appearrance: " + firstAppearance + " . Got: " + aircraft.getTimeOfFirstAppearance());
                return false;
            } else {
                return true;
            }
        };
    }

    public static Predicate<Aircraft> lastAppearance(final long lastAppearance) {
        return aircraft -> {
            if (aircraft.getTimeOfLastAppearance() != lastAppearance) {
                System.err.println("Expected last appearrance: " + lastAppearance + ". Got: " + aircraft.getTimeOfLastAppearance());
                return false;
            } else {
                return true;
            }
        };
    }

    public static Predicate<Aircraft> withMinMaxAltitude(final int minAltitude, final int maxAltitude) {
        return aircraft -> {
            if (aircraft.getStaticProperties().getMinAltitude() != minAltitude) {
                System.err.println("Min altitude " + minAltitude + " different from " + aircraft.getStaticProperties().getMinAltitude());
            }
            if (aircraft.getStaticProperties().getMaxAltitude() != maxAltitude) {
                System.err.println("Max altitude " + maxAltitude + " different from " + aircraft.getStaticProperties().getMaxAltitude());
            }
            return aircraft.getStaticProperties().getMinAltitude() == minAltitude && aircraft.getStaticProperties().getMaxAltitude() == maxAltitude;
        };
    }

    public static Predicate<AircraftState> aState(final Predicate<AircraftState>... constraints) {
        return and(constraints);
    }

    public static Predicate<AircraftState> withCoordinates(final double latitude, final double longitude) {
        return state -> {
            final Coordinates coordinates = state.getPosition().get();
            return coordinates.getLatitude() == latitude && coordinates.getLongitude() == longitude;
        };
    }

    public static Predicate<AircraftState> onAltitude(final double altitude) {
        return state -> state.getAltitude().get() == altitude;
    }

    public static Predicate<AircraftState> atDate(final long relativeDate) {
        return state -> state.getRelativeDate() == relativeDate;
    }
}