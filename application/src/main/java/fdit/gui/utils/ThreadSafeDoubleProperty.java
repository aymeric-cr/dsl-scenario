package fdit.gui.utils;

import javafx.beans.property.SimpleDoubleProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeDoubleProperty extends SimpleDoubleProperty {

    public ThreadSafeDoubleProperty() {
    }

    public ThreadSafeDoubleProperty(final double initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}
