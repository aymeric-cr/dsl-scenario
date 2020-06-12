package fdit.gui.utils;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeListProperty extends SimpleListProperty {
    public ThreadSafeListProperty() {
    }

    public ThreadSafeListProperty(final ObservableList initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }
}
