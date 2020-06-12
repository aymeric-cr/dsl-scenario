package fdit.tools.collection;

import javafx.util.Pair;

import java.util.function.Predicate;

public final class PairUtils {

    private PairUtils() {
    }

    public static <L, R> Predicate<? super Pair<L, R>> isPair(final Predicate<L> key, final Predicate<R> value) {
        return pair -> key.test(pair.getKey()) && value.test(pair.getValue());
    }
}
