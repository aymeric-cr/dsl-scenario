package fdit.metamodel.alteration;

import fdit.metamodel.alteration.action.Action;
import fdit.metamodel.alteration.parameters.Characteristic;
import fdit.metamodel.alteration.parameters.Value;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;
import static fdit.tools.stream.StreamUtils.mapping;
import static fdit.tools.stream.StreamUtils.tryFind;

public final class AlterationUtils {

    private AlterationUtils() {
    }

    public static Optional<Value> getValue(final Action action, final Characteristic type) {
        return tryFind(action.getParameters(), Value.class, value -> value.getCharacteristic() == type);
    }

    public static Collection<Integer> extractAircraftIDs(final Action action) {
        if (action.getTarget().isEmpty()) {
            return newArrayList();
        }
        return mapping(on(',').split(action.getTarget()), Integer::parseInt);
    }
}