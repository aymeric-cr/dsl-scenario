package fdit.metamodel.trigger;

import fdit.metamodel.element.FditElement;
import fdit.tools.functional.ThrowableConsumer;

import java.util.function.Predicate;

import static fdit.tools.predicate.PredicateUtils.and;

public class AlterationTriggerHelper {

    private AlterationTriggerHelper() {
    }

    public static ThrowableConsumer<ActionTrigger> content(final String content) {
        return trigger -> trigger.setContent(content);
    }

    public static ThrowableConsumer<ActionTrigger> description(final String descr) {
        return trigger -> trigger.setDescription(descr);
    }

    public static Predicate<? super FditElement> aTrigger(final String name,
                                                          final String content,
                                                          final String description,
                                                          final Predicate<ActionTrigger>... constraints) {
        return fditElement ->
                fditElement.getName().equals(name)
                        && ((ActionTrigger) fditElement).getDescription().equals(description)
                        && ((ActionTrigger) fditElement).getContent().equals(content)
                        && and(constraints).test((ActionTrigger) fditElement);
    }
}