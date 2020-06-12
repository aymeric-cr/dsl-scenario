package fdit.gui.utils.dialog;

import fdit.metamodel.element.FditElement;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.storage.nameChecker.FditElementNameChecker.checkRenameElementValidity;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.layout.GridPane.*;
import static javafx.scene.layout.Priority.ALWAYS;

public class FditElementRenameDialog extends FditDialog<String> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditElementRenameDialog.class);

    private final FditElement element;
    private final TextInputControl nameTextField = new TextField();
    private final Labeled errorLabel = new Label();
    private final DialogPane dialogPane = createDialogPane();

    public FditElementRenameDialog(final FditElement element) {
        this.element = element;
        nameTextField.setText(element.getName());
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> updateOkButton());
        setResultConverter(buttonClicked -> {
            final ButtonData data = buttonClicked == null ? null : buttonClicked.getButtonData();
            return data == OK_DONE ?
                    nameTextField.getText() :
                    null;
        });
        nameTextField.requestFocus();
        nameTextField.end();
        nameTextField.selectAll();
    }

    private static Node getOkButton(final DialogPane dialogPane) {
        return dialogPane.lookupButton(ButtonType.OK);
    }

    private DialogPane createDialogPane() {
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefHeight(150);
        dialogPane.setMaxHeight(150);
        dialogPane.setPrefWidth(300);
        dialogPane.setMaxWidth(300);
        dialogPane.setContent(createGridPane());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getOkButton(dialogPane).setDisable(true);
        return dialogPane;
    }

    private Node createGridPane() {
        final GridPane grid = new GridPane();
        grid.add(new Label(TRANSLATOR.getMessage("label.newName")), 0, 0);
        grid.add(nameTextField, 0, 1);
        grid.add(errorLabel, 0, 2);
        grid.setHgap(10);
        grid.setVgap(10);
        setHgrow(nameTextField, ALWAYS);
        setFillWidth(nameTextField, true);
        setFillHeight(nameTextField, true);
        return grid;
    }

    private void updateOkButton() {
        final CheckResult checkNameResult = checkRenameElementValidity(element,
                FDIT_MANAGER.getRootFile(),
                nameTextField.getText());
        if (checkNameResult.checkFailed()) {
            getOkButton(dialogPane).setDisable(true);
            errorLabel.setText(checkNameResult.getMessage());
            return;
        }
        errorLabel.setText("");
        getOkButton(dialogPane).setDisable(false);
    }
}
