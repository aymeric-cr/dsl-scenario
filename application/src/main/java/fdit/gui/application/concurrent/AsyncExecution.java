package fdit.gui.application.concurrent;

import fdit.tools.functional.ThrowableSupplier;

import java.util.function.Consumer;

public interface AsyncExecution<T> {

    AsyncExecution<T> onStart(final ThrowableSupplier<T> supplier);

    AsyncExecution<T> onSucceeded(final Consumer<T> handler);

    AsyncExecution<T> onFailed(final Consumer<Throwable> handler);

    AsyncExecution<T> onFinished(final Runnable handler);

    void startInBackground();

    void startInUIThread();

    T getResult();
}
