package fdit.tools.functional;

import java.util.function.Supplier;

import static com.google.common.base.Throwables.throwIfUnchecked;

@FunctionalInterface
public interface ThrowableSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return getThrows();
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    T getThrows() throws Exception;
}
