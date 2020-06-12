package fdit.gui.application.notifications;

import fdit.gui.application.concurrent.AsyncExecution;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.notifications.Notifier.showLoadingNotification;
import static fdit.gui.utils.FXUtils.createReturningAsyncExecution;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public abstract class LoadingNotifier<T> implements Observable {

    private static Scene SCENE;
    private final AsyncExecution runnable;
    private final StringProperty loadingMessage = new ThreadSafeStringProperty("");
    private final BooleanProperty finishedProperty = new ThreadSafeBooleanProperty(false);
    private final List<InvalidationListener> invalidationListeners = newArrayList();

    protected LoadingNotifier() {
        runnable = createReturningAsyncExecution(this::run)
                .onFailed(this::onFailed)
                .onFinished(this::executionFinished)
                .onSucceeded(this::onSucceeded);
    }

    public static void setScene(final Scene scene) {
        SCENE = scene;
    }

    public final void startInBackground() {
        startLoading();
        runnable.startInBackground();
    }

    public final void startInUIThread() {
        startLoading();
        runnable.startInUIThread();
    }

    protected final void setLoadingMessage(final String newMessage) {
        this.loadingMessage.setValue(newMessage);
    }

    public StringProperty loadingMessageProperty() {
        return loadingMessage;
    }

    private void executionFinished() {
        this.finishedProperty.setValue(true);
        onFinished();
    }

    private void startLoading() {
        startRunnableInUIThread(() -> showLoadingNotification(loadingMessage, finishedProperty, SCENE));
    }

    protected void onFinished() {

    }

    protected void onSucceeded(final T result) {
    }

    protected void onFailed(final Throwable throwable) {
    }

    protected void notifyListeners() {
        for (final InvalidationListener listener : invalidationListeners) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(final InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }

    protected abstract T run() throws Exception;
}