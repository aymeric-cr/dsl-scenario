package fdit.tools.functional;


import java.util.function.Consumer;

import static com.google.common.base.Throwables.throwIfUnchecked;

@FunctionalInterface
public interface ThrowableConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrows(elem);
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T elem) throws Exception;
}
