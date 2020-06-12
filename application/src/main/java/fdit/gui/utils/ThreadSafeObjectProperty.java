package fdit.gui.utils;

import javafx.beans.property.SimpleObjectProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeObjectProperty<T> extends SimpleObjectProperty<T> {

    public ThreadSafeObjectProperty(final T value) {
        super(value);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}