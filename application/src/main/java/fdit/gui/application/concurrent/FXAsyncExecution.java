package fdit.gui.application.concurrent;

import fdit.tools.functional.ThrowableSupplier;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.function.Consumer;

import static javafx.concurrent.Worker.State.*;

public class FXAsyncExecution<T> implements AsyncExecution<T> {

    private T result;
    private ThrowableSupplier<T> onStartHandler;
    private Consumer<T> onSucceededHandler;
    private Consumer<Throwable> onFailedHandler;
    private Runnable onFinishedHandler;

    @Override
    public AsyncExecution<T> onStart(final ThrowableSupplier<T> supplier) {
        onStartHandler = supplier;
        return this;
    }

    @Override
    public AsyncExecution<T> onSucceeded(final Consumer<T> handler) {
        onSucceededHandler = handler;
        return this;
    }

    @Override
    public AsyncExecution<T> onFailed(final Consumer<Throwable> handler) {
        onFailedHandler = handler;
        return this;
    }

    @Override
    public AsyncExecution<T> onFinished(final Runnable handler) {
        onFinishedHandler = handler;
        return this;
    }

    @Override
    public void startInBackground() {
        final Service<T> service = new Service<T>() {
            @Override
            protected Task<T> createTask() {
                return new Task<T>() {
                    @Override
                    protected T call() throws Exception {
                        result = onStartHandler.getThrows();
                        return result;
                    }
                };
            }
        };
        if (onSucceededHandler != null) {
            service.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == SUCCEEDED) {
                    onSucceededHandler.accept(result);
                }
            });
        }
        if (onFailedHandler != null) {
            service.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == FAILED) {
                    onFailedHandler.accept(service.getException());
                }
            });
        }
        if (onFinishedHandler != null) {
            service.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == SUCCEEDED || newValue == FAILED || newValue == CANCELLED) {
                    onFinishedHandler.run();
                }
            });
        }
        service.start();
    }

    @Override
    public void startInUIThread() {
        if (Platform.isFxApplicationThread()) {
            startSync();
        } else {
            Platform.runLater(this::startSync);
        }
    }

    private void startSync() {
        try {
            result = onStartHandler.getThrows();
            if (onSucceededHandler != null) {
                onSucceededHandler.accept(result);
            }
        } catch (final Exception e) {
            if (onFailedHandler != null) {
                onFailedHandler.accept(e);
            }
        } finally {
            if (onFinishedHandler != null) {
                onFinishedHandler.run();
            }
        }
    }

    @Override
    public T getResult() {
        return result;
    }
}