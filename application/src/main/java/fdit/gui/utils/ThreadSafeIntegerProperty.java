package fdit.gui.utils;

import javafx.beans.property.SimpleIntegerProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeIntegerProperty extends SimpleIntegerProperty {

    public ThreadSafeIntegerProperty() {
    }

    public ThreadSafeIntegerProperty(final int initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}