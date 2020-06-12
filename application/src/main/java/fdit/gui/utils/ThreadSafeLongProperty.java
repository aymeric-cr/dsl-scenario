package fdit.gui.utils;

import javafx.beans.property.SimpleLongProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeLongProperty extends SimpleLongProperty {

    public ThreadSafeLongProperty() {
    }

    public ThreadSafeLongProperty(final int initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}