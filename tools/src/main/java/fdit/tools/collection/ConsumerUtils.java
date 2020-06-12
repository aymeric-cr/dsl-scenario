package fdit.tools.collection;

import java.util.function.Consumer;

public final class ConsumerUtils {

    private ConsumerUtils() {
    }

    public static <T> T acceptAll(final T element, final Consumer<? super T>... actions) {
        for (final Consumer<? super T> action : actions) {
            action.accept(element);
        }
        return element;
    }

    public static <T> Consumer<T> nop() {
        return t -> {
        };
    }
}
