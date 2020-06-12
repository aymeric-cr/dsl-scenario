package fdit.storage.alteration;

import fdit.metamodel.alteration.AlterationSchema;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.action.Action;
import fdit.metamodel.alteration.parameters.*;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.inject.internal.util.$Maps.newHashMap;
import static fdit.metamodel.alteration.parameters.Characteristic.ALTITUDE;
import static java.lang.Double.parseDouble;
import static java.lang.Math.floor;
import static java.lang.String.valueOf;

public final class AlterationSpecificationConverterUtils {

    private AlterationSpecificationConverterUtils() {
    }

    public static Pair<List<Action>, Map<Integer, List<ActionParameter>>>
    extractMassiveParameters(final AlterationSpecification alterationSpecification) {
        final Map<Integer, List<ActionParameter>> massiveParameters = newHashMap();
        final List<Action> order = newArrayList();
        int depth = 0;
        for (final Action action : alterationSpecification.getAlterationSchema().getActions()) {
            order.add(action);
            for (final ActionParameter actionParameter : action.getParameters()) {

                final List<ActionParameter> mustAppearOnce = newArrayList();

                if (actionParameter instanceof RangeValue) {
                    final boolean isIntegerParameter =
                            ((RangeValue) actionParameter).getCharacteristic() == ALTITUDE;

                    final double rangeMin = parseDouble(((RangeValue) actionParameter).getRangeMinContent());
                    final double rangeMax = parseDouble(((RangeValue) actionParameter).getRangeMaxContent());
                    final boolean isOffSet = ((RangeValue) actionParameter).isOffset();
                    final Characteristic characteristic = ((RangeValue) actionParameter).getCharacteristic();

                    final String rangeMinDecr = rangeValueToString(rangeMin - 1.0, isIntegerParameter);
                    mustAppearOnce.add(new Value(characteristic, rangeMinDecr, isOffSet));
                    final String rangeMinIncr = rangeValueToString(rangeMin + 1.0, isIntegerParameter);
                    mustAppearOnce.add(new Value(characteristic, rangeMinIncr, isOffSet));
                    final String rangeMiddle = rangeValueToString(rangeMin + (rangeMax - rangeMin) / 2.0,
                            isIntegerParameter);
                    mustAppearOnce.add(new Value(characteristic, rangeMiddle, isOffSet));
                    final String rangeMaxDecr = rangeValueToString(rangeMax - 1.0, isIntegerParameter);
                    mustAppearOnce.add(new Value(characteristic, rangeMaxDecr, isOffSet));
                    final String rangeMaxIncr = rangeValueToString(rangeMax + 1.0, isIntegerParameter);
                    mustAppearOnce.add(new Value(characteristic, rangeMaxIncr, isOffSet));
                }

                if (actionParameter instanceof ListValue) {
                    final Characteristic characteristic = ((ListValue) actionParameter).getCharacteristic();
                    final boolean isOffset = ((ListValue) actionParameter).isOffset();

                    for (final String value : ((ListValue) actionParameter).getValues()) {
                        mustAppearOnce.add(new Value(characteristic, value, isOffset));
                    }
                }

                if (!(actionParameter instanceof RangeValue) &&
                        !(actionParameter instanceof ListValue)) {
                    mustAppearOnce.add(actionParameter);
                }
                massiveParameters.put(depth, mustAppearOnce);
                depth++;
            }
        }
        return new Pair<>(order, massiveParameters);
    }

    private static String rangeValueToString(final double value, final boolean isIntegerParameter) {
        if (isIntegerParameter) {
            return valueOf((int) floor(value));
        }
        return valueOf(value);
    }

    public static List<ActionParameter[]> getCrossProduct(final Map<Integer, List<ActionParameter>> lists) {
        final List<ActionParameter[]> results = new ArrayList<>();
        getCrossProduct(results, lists, 0, new ActionParameter[lists.size()]);
        return results;
    }

    private static void getCrossProduct(final List<ActionParameter[]> results,
                                        final Map<Integer, List<ActionParameter>> lists,
                                        final int depth,
                                        final ActionParameter[] current) {
        if (!lists.isEmpty()) {
            for (int i = 0; i < lists.get(depth).size(); i++) {
                current[depth] = lists.get(depth).get(i);
                if (depth < lists.keySet().size() - 1) {
                    getCrossProduct(results, lists, depth + 1, current);
                } else {
                    results.add(Arrays.copyOf(current, current.length));
                }
            }
        }
    }

    public static List<List<Action>> extractScenarioAction(final List<ActionParameter[]> crossProduct,
                                                           final List<Action> order) {
        final AtomicReference<List<List<Action>>> scenarioActionsLists = new AtomicReference<>(newArrayList());
        if (crossProduct.isEmpty()) {
            scenarioActionsLists.get().add(order);
        } else {
            for (final ActionParameter[] parameters : crossProduct) {
                int idx = 0;
                final List<Action> actions = newArrayList();
                for (final Action action : order) {
                    final Action newAction = action.copy();
                    for (int i = idx; i < action.getParameters().size() + idx; i++) {
                        if (parameters[i] instanceof AlterationParameter) {
                            newAction.getParameters().add(parameters[i]);
                        }
                    }
                    idx += action.getParameters().size();
                    actions.add(newAction);
                }
                scenarioActionsLists.get().add(actions);
            }
        }
        return scenarioActionsLists.get();
    }

    public static Collection<AlterationSpecification> extractScenarios(final AlterationSpecification specification,
                                                                       final Iterable<List<Action>> extractedActions) {
        int number = 0;
        final Collection<AlterationSpecification> specifications = newArrayList();
        for (final List<Action> actions : extractedActions) {
            final AlterationSchema alterationSchema = specification.getAlterationSchema();
            final AlterationSchema newAlterationSchema = new AlterationSchema(alterationSchema.getName() +
                    '_' +
                    number,
                    alterationSchema.getDescription(),
                    actions);
            final AlterationSpecification newSpecification =
                    new AlterationSpecification(specification.getName(), newAlterationSchema);
            newSpecification.setFather(specification.getFather());
            specifications.add(newSpecification);
            number++;
        }
        return specifications;
    }

    public static AlterationSpecification mergeAlterationSpecification(final Collection<AlterationSpecification> specifications) {
        final AlterationSchema scenario = new AlterationSchema("", "");
        final AlterationSpecification result = new AlterationSpecification("", scenario);
        boolean firstLoop = true;
        for (final AlterationSpecification specification : specifications) {
            if (firstLoop) {
                result.setName(specification.getName());
                result.setFather(specification.getFather());
                firstLoop = false;
            }
            scenario.getActions().addAll(specification.getAlterationSchema().getActions());
        }
        return result;
    }
}