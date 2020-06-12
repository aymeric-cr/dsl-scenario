package fdit.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class FditAlert extends Alert {

    public FditAlert(final AlertType alertType) {
        super(alertType);
        addStylesheet();
    }

    public FditAlert(final AlertType alertType, final String contentText, final ButtonType... buttons) {
        super(alertType, contentText, buttons);
        addStylesheet();
    }

    private void addStylesheet() {
        getDialogPane().getStylesheets().add(getClass().getResource("/fdit/gui/application/fdit.css").toExternalForm());
    }
}