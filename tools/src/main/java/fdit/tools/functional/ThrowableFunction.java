package fdit.tools.functional;

import java.util.function.Function;

import static com.google.common.base.Throwables.throwIfUnchecked;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(final T t) {
        try {
            return applyThrows(t);
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t) throws Exception;
}
