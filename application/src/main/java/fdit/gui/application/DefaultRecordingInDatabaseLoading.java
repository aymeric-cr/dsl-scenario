package fdit.gui.application;

import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;
import fdit.tools.i18n.MessageTranslator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;

import static fdit.gui.application.notifications.Notifier.showLoadingNotification;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class DefaultRecordingInDatabaseLoading implements RecordingInDatabaseLoadingCallback {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(DefaultRecordingInDatabaseLoading.class);

    private final Scene scene;
    private final StringProperty statusProperty = new ThreadSafeStringProperty(TRANSLATOR.getMessage("recording_loading"));
    private final BooleanProperty loadingFinishedProperty = new ThreadSafeBooleanProperty();

    public DefaultRecordingInDatabaseLoading(final Scene scene) {
        this.scene = scene;
    }

    @Override
    public void onDatabaseLoadingStarted() {
        loadingFinishedProperty.set(false);
        startRunnableInUIThread(() -> showLoadingNotification(statusProperty, loadingFinishedProperty, scene));
    }

    @Override
    public void onDatabaseLoadingEnded() {
        loadingFinishedProperty.set(true);
    }

    @Override
    public void onDatabaseRegisteringStarted() {
        statusProperty.set(TRANSLATOR.getMessage("recording_caching"));
    }

    @Override
    public void onDatabaseRegisteringEnded() {
        statusProperty.set(TRANSLATOR.getMessage("recording_loading"));
    }
}
