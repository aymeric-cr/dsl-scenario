package fdit.gui;

import fdit.gui.utils.ThreadSafeBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public interface EditorController extends Initializable {

    @Override
    default void initialize(final URL location, final ResourceBundle resources) {
        initialize();
    }


    void initialize();

    default void requestFocus() {

    }

    /**
     * Called when another editor has been opened, this editor is put in background
     */
    default void onBackground() {
    }

    /**
     * Called when this editor is closed
     */
    void onClose();

    default BooleanProperty loadingFinishedProperty() {
        return new ThreadSafeBooleanProperty(true);
    }
}
