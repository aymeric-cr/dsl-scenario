package fdit.metamodel.alteration;

import fdit.metamodel.alteration.action.Action;
import fdit.metamodel.alteration.action.Action.ActionType;
import fdit.metamodel.alteration.action.TrajectoryModification;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.scope.TimeWindow;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.zone.Zone;
import fdit.testTools.Saver;
import fdit.tools.functional.ThrowableConsumer;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.parameters.Characteristic.*;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static fdit.tools.predicate.PredicateUtils.and;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;

public final class AlterationSpecificationHelper {

    private AlterationSpecificationHelper() {
    }

    public static Predicate<? super FditElement> anAlterationSpecification() {
        return element -> element instanceof AlterationSpecification;
    }

    public static Predicate<? super FditElement> anAlterationSpecification(final Predicate<AlterationSchema> constraints) {
        return element -> element instanceof AlterationSpecification &&
                constraints.test(((AlterationSpecification) element).getAlterationSchema());
    }

    @SafeVarargs
    public static Predicate<AlterationSchema> withAbstractAlterationSchema(final String name,
                                                                           final String description,
                                                                           final Predicate<AlterationSchema>... actions) {
        return alterationSchema -> {
            if (alterationSchema.getName().equals(name) && alterationSchema.getDescription().equals(description)) {
                return and(actions).test(alterationSchema);
            } else {
                System.err.println("Scenario name '" +
                        alterationSchema.getName() +
                        "' or description '" +
                        alterationSchema.getDescription() +
                        '\'' +
                        " different from name '" +
                        name +
                        "' or description '" +
                        description +
                        '\'');
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<AlterationSchema> anAlterationScenario(final String name,
                                                                   final String description,
                                                                   final Predicate<AlterationSchema>... actions) {
        return scenario -> {
            if (scenario.getName().equals(name) && scenario.getDescription().equals(description)) {
                return and(actions).test(scenario);
            } else {
                System.err.println("Scenario name '" +
                        scenario.getName() +
                        "' or description '" +
                        scenario.getDescription() +
                        '\'' +
                        " different from name '" +
                        name +
                        "' or description '" +
                        description +
                        '\'');
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<AlterationSchema> actions(final Predicate<Action>... contraints) {
        return scenario -> containsOnly(contraints).test(scenario.getActions());
    }

    @SafeVarargs
    public static Predicate<Action> anAction(final Predicate<Action>... constraints) {
        return and(constraints);
    }

    public static Predicate<Action> withType(final ActionType actionType) {
        return action -> {
            if (action.getActionType() == actionType) {
                return true;
            } else {
                System.err.println("Action type: " + action.getActionType() + " different from: " + actionType);
                return false;
            }
        };
    }

    public static Predicate<Action> withActionName(final String name) {
        return action -> {
            if (action.getName().equals(name)) {
                return true;
            } else {
                System.err.println("Scenario action name: " + action.getName() + " different from " + name);
                return false;
            }
        };
    }

    public static Predicate<Action> withActionDescription(final String description) {
        return action -> {
            if (action.getDescription().equals(description)) {
                return true;
            } else {
                System.err.println("Scenario action description: " +
                        action.getDescription() +
                        " different from " +
                        description);
                return false;
            }
        };
    }

    public static Predicate<Action> onTarget(final String target) {
        return action -> {
            final List<String> received_targets = asList(target.split(","));
            final List<String> action_targets = asList(action.getTarget().split(","));
            if (received_targets.stream().anyMatch(t -> !action_targets.contains(t))) {
                System.err.println("Action target: " + action.getTarget() + " different from: " + target);
                return false;
            } else if (action_targets.stream().anyMatch(t -> !received_targets.contains(t))) {
                System.err.println("Action target: " + action.getTarget() + " different from: " + target);
                return false;
            } else {
                return true;
            }

        };
    }

    public static Predicate<Action> onTimeWindow(final long lowerBound, final long upperBound) {
        return action -> {
            final TimeWindow timeWindow = (TimeWindow) action.getScope();
            if (timeWindow.getLowerBoundMillis() == lowerBound && timeWindow.getUpperBoundMillis() == upperBound) {
                return true;
            } else {
                System.err.println("TimeWindow: [" +
                        timeWindow.getLowerBoundMillis() +
                        ';' +
                        timeWindow.getUpperBoundMillis() +
                        ']' +
                        " different from: [" +
                        lowerBound +
                        ';' +
                        upperBound +
                        ']');
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<Action> withParameters(final Predicate<ActionParameter>... parameters) {
        return action -> containsOnly(parameters).test(action.getParameters());
    }

    @SafeVarargs
    public static Predicate<Action> withTrajectory(final Predicate<AircraftWayPoint>... waypoints) {
        return action -> {
            if (action instanceof TrajectoryModification) {
                return containsOnly(waypoints).test(((TrajectoryModification) action).getTrajectory().getWayPoints());
            }
            System.err.println("The scenario action is not an instance of TrajectoryModification");
            return false;
        };
    }

    public static Predicate<ActionParameter> withAircraftNumber(final int aircraftNumber) {
        return parameter -> {
            if (parameter instanceof AircraftNumber) {
                if (((AircraftNumber) parameter).getValue() == aircraftNumber) {
                    return true;
                } else {
                    System.err.println("Aircraft number: " + ((AircraftNumber) parameter).getValue() +
                            " not equals to " + aircraftNumber);
                    return false;
                }
            } else {
                System.err.println("Parameter is not an instance of AircraftNumber");
                return false;
            }
        };
    }

    public static Predicate<ActionParameter> withFrequency(final int frequency) {
        return parameter -> {
            if (parameter instanceof Frequency) {
                if (((Frequency) parameter).getValue() == frequency) {
                    return true;
                } else {
                    System.err.println("Frequency: " + ((Frequency) parameter).getValue() +
                            " not equals to " + frequency);
                    return false;
                }
            } else {
                System.err.println("Parameter is not an instance of Frequency");
                return false;
            }
        };
    }

    public static Predicate<ActionParameter> withTimestampValue(final int offset) {
        return parameter -> {
            if (parameter instanceof Timestamp) {
                if (((Timestamp) parameter).getValue() == offset) {
                    return true;
                } else {
                    System.err.println("Timestamp: " + ((Timestamp) parameter).getValue() +
                            " not equals to " + offset);
                    return false;
                }
            } else {
                System.err.println("Parameter is not an instance of Timestamp");
                return false;
            }
        };
    }

    public static Predicate<AircraftWayPoint> withWaypoint(final double latitude,
                                                           final double longitude,
                                                           final int altitude,
                                                           final long time) {
        return waypoint -> {
            if (waypoint.getCoordinates().getLatitude() == latitude
                    && waypoint.getCoordinates().getLongitude() == longitude
                    && waypoint.getAltitude() == altitude
                    && waypoint.getTime() == time) {
                return true;
            } else {
                System.err.println("Waypoint '" +
                        "(" + waypoint.getCoordinates().getLatitude() + "," + waypoint.getCoordinates().getLongitude() + ")" +
                        "' or " +
                        "altitude '" +
                        waypoint.getAltitude() +
                        "' or time '" +
                        waypoint.getTime() +
                        "' different from: \n'" +
                        latitude +
                        "', '" +
                        longitude +
                        "', '" +
                        altitude +
                        "', '" +
                        time +
                        "'");
                return false;
            }
        };
    }

    public static Predicate<ActionParameter> withValue(final Characteristic characteristic,
                                                       final String content,
                                                       final boolean isOffset) {
        return parameter -> {
            if (parameter instanceof Value) {
                if (((Value) parameter).getCharacteristic() == characteristic &&
                        ((Value) parameter).getContent().equals(content) &&
                        ((Value) parameter).isOffset() == isOffset) {
                    return true;
                } else {
                    System.err.println("Characteristic '" +
                            ((Value) parameter).getCharacteristic() +
                            "' or " +
                            "content '" +
                            ((Value) parameter).getContent() +
                            "' different from: \n'" +
                            characteristic +
                            "', '" +
                            content +
                            "'");
                    return false;
                }
            } else {
                System.err.println("Parameter is not a instance of Value");
                return false;
            }
        };
    }

    public static Predicate<ActionParameter> withValue(final Characteristic characteristic,
                                                       final String content,
                                                       final Mode mode) {
        return parameter -> {
            if (parameter instanceof Value) {
                if (((Value) parameter).getCharacteristic() == characteristic &&
                        ((Value) parameter).getContent().compareTo(content) == 0 &&
                        ((Value) parameter).getMode() == mode) {
                    return true;
                } else {
                    System.err.println("Characteristic '" +
                            ((Value) parameter).getCharacteristic() +
                            "' or " +
                            "content '" +
                            ((Value) parameter).getContent() +
                            "' different from: \n'" +
                            characteristic +
                            "', '" +
                            content +
                            "'");
                    return false;
                }
            } else {
                System.err.println("Parameter is not a instance of Value");
                return false;
            }
        };
    }

    public static Predicate<ActionParameter> withRangeValue(final Characteristic characteristic,
                                                            final String rangeMin,
                                                            final String rangeMax) {
        return parameter -> parameter instanceof RangeValue &&
                ((RangeValue) parameter).getCharacteristic() == characteristic &&
                ((RangeValue) parameter).getRangeMinContent().equals(rangeMin) &&
                ((RangeValue) parameter).getRangeMaxContent().equals(rangeMax) &&
                !((RangeValue) parameter).isOffset();
    }

    public static Predicate<ActionParameter> withListValue(final Characteristic characteristic,
                                                           final List<String> values) {
        return parameter -> parameter instanceof ListValue &&
                ((ListValue) parameter).getCharacteristic() == characteristic &&
                ((ListValue) parameter).getValues().containsAll(values) &&
                ((ListValue) parameter).getValues().size() == values.size() &&
                !((ListValue) parameter).isOffset();
    }

    public static Predicate<ActionParameter> withTimestampValue(final Characteristic characteristic, final String content) {
        return parameter -> ((Value) parameter).isOffset() &&
                ((Value) parameter).getCharacteristic() == characteristic &&
                ((Value) parameter).getContent().equals(content);
    }

    @SafeVarargs
    public static Consumer<AlterationSpecification> alterationScenario(final Consumer<AlterationSchema>... actions) {
        return alterationSpecification -> {
            final AlterationSchema scenario = alterationSpecification.getAlterationSchema();
            acceptAll(scenario, actions);
        };
    }

    @SafeVarargs
    public static Consumer<AlterationSchema> action(final Saver<Action> saver,
                                                    final Consumer<ActionBuilder>... consumers) {
        return scenario -> saver.save(createAction(scenario, consumers));
    }

    @SafeVarargs
    public static Consumer<AlterationSchema> action(final Consumer<ActionBuilder>... consumers) {
        return scenarioBuilder -> createAction(scenarioBuilder, consumers);
    }

    @SafeVarargs
    public static AlterationSpecification createAlterationSpecification(final String name,
                                                                        final Consumer<AlterationSchema>... consumers) {
        final AlterationSchema alterationSchema = new AlterationSchema(name, "");
        acceptAll(alterationSchema, consumers);
        return new AlterationSpecification(name, alterationSchema);
    }

    public static ThrowableConsumer<AlterationSchema> actions(final Action... actions) {
        return alterationScenario -> {
            for (Action action : actions) {
                alterationScenario.addAction(action);
            }
        };
    }

    private static Action createAction(final AlterationSchema scenario,
                                               final Consumer<ActionBuilder>[] consumers) {
        final ActionBuilder builder = new ActionBuilder();
        acceptAll(builder, consumers);
        final Action scenarioAction = builder.build();
        scenario.addAction(scenarioAction);
        return scenarioAction;
    }

    @SafeVarargs
    public static Action createAction(final Consumer<ActionBuilder>... consumers) {
        final ActionBuilder builder = new ActionBuilder();
        acceptAll(builder, consumers);
        return builder.build();
    }

    public static Consumer<ActionBuilder> type(final ActionType actionType) {
        return builder -> builder.withActionType(actionType);
    }

    public static Consumer<ActionBuilder> name(final String name) {
        return builder -> builder.withName(name);
    }

    public static Consumer<ActionBuilder> description(final String description) {
        return builder -> builder.withDescription(description);
    }

    public static Consumer<ActionBuilder> timestamp(final long value) {
        return builder -> builder.withTimestamp(new Timestamp(value));
    }

    public static Consumer<ActionBuilder> timestamp(final long value, final boolean offset) {
        return builder -> builder.withTimestamp(new Timestamp(value, offset));
    }

    public static Consumer<ActionBuilder> recordName(final String value) {
        return builder -> builder.withRecordName(new RecordName(value));
    }

    public static Consumer<ActionBuilder> icaoParameter(final String value) {
        return builder -> builder.withIcaoParameter(new Value(ICAO, value));
    }

    public static Consumer<ActionBuilder> aircraftNumber(final int value) {
        return builder -> builder.withAircraftNumber(new AircraftNumber(value));
    }


    public static Consumer<ActionBuilder> target(final String target) {
        return builder -> builder.withTarget(target);
    }

    public static Consumer<ActionBuilder> parameters(final ActionParameter... parameters) {
        return builder -> builder.addParameters(parameters);
    }

    public static Consumer<ActionBuilder> trajectory(final AircraftWayPoint... waypoints) {
        return builder -> builder.addWaypoints(waypoints);
    }


    public static Consumer<ActionBuilder> timeWindow(final long lowerBound, final long upperBound) {
        return builder -> builder.withScope(new TimeWindow(lowerBound, upperBound));
    }

    public static AircraftWayPoint waypoint(final Coordinates coordinates,
                                            final int altitude,
                                            final long time) {
        return new AircraftWayPoint(coordinates, altitude, time);
    }

    public static ActionParameter value(final Characteristic characteristic,
                                        final String value) {
        return new Value(characteristic, value);
    }

    public static ActionParameter valueWithOffset(final Characteristic characteristic,
                                                  final String value) {
        return new Value(characteristic, value, true);
    }

    public static ActionParameter rangeValue(final Characteristic characteristic,
                                             final String rangeMin,
                                             final String rangeMax) {
        return new RangeValue(characteristic, rangeMin, rangeMax, false);
    }

    public static ActionParameter listValue(final Characteristic characteristic,
                                            final String value1,
                                            final String value2) {
        final List<String> values = newArrayList();
        values.add(value1);
        values.add(value2);
        return new ListValue(characteristic, values, false);
    }

    public static List<String> aList(final String... values) {
        final List<String> list = newArrayList();
        list.addAll(asList(values));
        return list;
    }
}