package fdit.tools.functional;

import java.util.function.BiFunction;

import static com.google.common.base.Throwables.throwIfUnchecked;

@FunctionalInterface
public interface ThrowableBiFunction<T, U, R> extends BiFunction<T, U, R> {

    @Override
    default R apply(final T t, final U u) {
        try {
            return applyThrows(t, u);
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t, U u) throws Exception;
}
