package fdit.tools.optional;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static fdit.tools.stream.StreamUtils.safeContains;
import static java.util.Arrays.asList;
import static java.util.Optional.*;

public final class Optionals {

    private Optionals() {
    }

    public static <T> Optional<T> ensure(final T candidate, final T... available) {
        return safeContains(asList(available), candidate) ? of(candidate) : empty();
    }

    public static <T, C> Optional<C> cast(final Optional<T> element, final Class<C> cast) {
        if (element.isPresent()) {
            final T t = element.get();
            if (cast.isInstance(t)) {
                return of((C) t);
            }
        }
        return empty();
    }

    public static <T> T orNull(final Optional<T> optional) {
        return optional.orElse(null);
    }

    public static <T, U> U orNull(final Optional<T> optional, final Function<T, U> mapper) {
        return orNull(optional.map(mapper));
    }

    public static <T, U> U orNull(@Nullable final T nullable, final Function<T, U> mapper) {
        return orNull(ofNullable(nullable).map(mapper));
    }

    public static Optional<String> ofBlankable(final String value) {
        return StringUtils.isBlank(value) ? empty() : of(value);
    }

    public static <T, U> U visitOptional(final Optional<T> optional,
                                         final Function<T, U> presentCase,
                                         final Supplier<U> absentCase) {
        if (optional.isPresent()) {
            return presentCase.apply(optional.get());
        }
        return absentCase.get();
    }
}
