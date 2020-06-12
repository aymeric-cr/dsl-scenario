package fdit.gui.utils.radioButton;

import javafx.scene.control.RadioButton;

public class FditRadioButton<T> extends RadioButton {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }
}
