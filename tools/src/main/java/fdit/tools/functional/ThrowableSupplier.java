package fdit.tools.functional;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

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

    static void throwIfUnchecked(Throwable throwable) {
        checkNotNull(throwable);
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
    }
}
