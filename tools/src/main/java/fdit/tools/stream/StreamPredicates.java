package fdit.tools.stream;

import java.util.function.Predicate;
import java.util.stream.Stream;

public final class StreamPredicates {


    private StreamPredicates() {
    }

    public static Predicate<Stream> hasSize(final int size) {
        return stream -> stream.count() == size;
    }
}
