package fdit.gui.utils;

import javafx.beans.property.SimpleStringProperty;

import static fdit.gui.utils.FXUtils.startRunnableInUIThread;

public class ThreadSafeStringProperty extends SimpleStringProperty {

    public ThreadSafeStringProperty() {
    }

    public ThreadSafeStringProperty(final String initialValue) {
        super(initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        startRunnableInUIThread(() -> super.fireValueChangedEvent());
    }

}
