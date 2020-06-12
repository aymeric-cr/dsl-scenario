package fdit.gui.utils.dialog;

import javafx.scene.control.Dialog;

public class FditDialog<R> extends Dialog<R> {

    public FditDialog() {
        getDialogPane().getStylesheets().add(getClass().getResource("/fdit/gui/application/fdit.css").toExternalForm());
    }
}
