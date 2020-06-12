package fdit.metamodel.filter;

import fdit.metamodel.element.FditElement;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static fdit.tools.predicate.PredicateUtils.and;

public class LTLFilterHelper {

    private LTLFilterHelper() {
    }

    public static Supplier<LTLFilter> ltlFilter(final String name,
                                                final UUID id,
                                                final String description,
                                                final String content,
                                                final Consumer<LTLFilter>... consumers) {
        return () -> {
            final LTLFilter filter = new LTLFilter(name, id, description, content);
            acceptAll(filter, consumers);
            return filter;
        };
    }

    public static Predicate<? super FditElement> anLTLFilter(final Predicate<LTLFilter>... predicates) {
        return fditElement -> and(predicates).test((LTLFilter) fditElement);
    }

    public static Predicate<LTLFilter> withDescription(final String description) {
        return ltlFilter -> {
            if (ltlFilter.getDescription().equals(description)) {
                return true;
            } else {
                System.err.println("Filter description: " + ltlFilter.getDescription() + " different from " + description);
                return false;
            }
        };
    }

    public static Predicate<LTLFilter> withName(final String name) {
        return ltlFilter -> {
            if (ltlFilter.getName().equals(name)) {
                return true;
            } else {
                System.err.println("Filter name: " + ltlFilter.getName() + " different from " + name);
                return false;
            }
        };
    }

    public static Predicate<LTLFilter> withContent(final String content) {
        return ltlFilter -> {
            if (ltlFilter.getContent().equals(content)) {
                return true;
            } else {
                System.err.println("Filter content: " + ltlFilter.getContent() + " different from " + content);
                return false;
            }
        };
    }
}