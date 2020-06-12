package fdit.gui.utils;

import javafx.beans.property.SimpleBooleanProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeBooleanProperty extends SimpleBooleanProperty {

    public ThreadSafeBooleanProperty() {
    }

    public ThreadSafeBooleanProperty(final boolean initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}
