package fdit.tools.functional;

import static com.google.common.base.Throwables.throwIfUnchecked;

@FunctionalInterface
public interface ThrowableRunnable extends Runnable {

    @Override
    default void run() {
        try {
            runThrows();
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    void runThrows() throws Exception;
}
