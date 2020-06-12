package fdit.gui.utils;

import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

import static fdit.gui.utils.FXUtils.setOnDoubleClick;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;

public class RenamableTab extends Tab {

    private final StringProperty name = new ThreadSafeStringProperty();
    private final Labeled label = new Label();
    private final TextInputControl textField = new TextField();

    public RenamableTab() {
        initialize();
        initializeListener();
    }

    private void initialize() {
        label.textProperty().bindBidirectional(name);
        label.getStyleClass().add("renamableTab-label");
        textField.getStyleClass().add("renamableTab-textField");
        textField.textProperty().bindBidirectional(name);
        setGraphic(label);
    }

    private void initializeListener() {
        setOnDoubleClick(label, mouseEvent -> {
            setGraphic(textField);
        });
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER || event.getCode() == ESCAPE) {
                setGraphic(label);
            }
        });
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                setGraphic(label);
            }
        });
        setOnSelectionChanged(event -> setGraphic(label));
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(final String name) {
        this.name.set(name);
    }
}
