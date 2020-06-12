package fdit.gui.application.concurrent;

import fdit.tools.functional.ThrowableSupplier;

import java.util.function.Consumer;

public class TestSyncExecution<T> implements AsyncExecution<T> {

    private T result;
    private ThrowableSupplier<T> supplier;

    @Override
    public AsyncExecution<T> onStart(final ThrowableSupplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    @Override
    public AsyncExecution<T> onSucceeded(final Consumer<T> handler) {
        handler.accept(result);
        return this;
    }

    @Override
    public AsyncExecution<T> onFailed(final Consumer<Throwable> handler) {
        throw new AssertionError("");
    }

    @Override
    public AsyncExecution<T> onFinished(final Runnable handler) {
        handler.run();
        return this;
    }

    @Override
    public void startInBackground() {
        start();
    }

    @Override
    public void startInUIThread() {
        start();
    }

    private void start() {
        result = supplier.get();
    }

    @Override
    public T getResult() {
        return result;
    }
}
