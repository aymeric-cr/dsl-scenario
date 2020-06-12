package fdit.gui.utils.dialog;

import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.File;

import static fdit.storage.nameChecker.FditElementNameChecker.checkNewFditElementNameValidity;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.layout.GridPane.*;
import static javafx.scene.layout.Priority.ALWAYS;

public class BasicFditElementCreationDialog extends FditDialog<String> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(BasicFditElementCreationDialog.class);

    protected final Directory father;
    protected final Class<? extends FditElement> type;
    protected final TextInputControl nameTextField = new TextField();
    protected final Labeled errorLabel = new Label();
    protected final DialogPane dialogPane = createDialogPane();
    protected final File rootFile;

    public BasicFditElementCreationDialog(final Directory father, final File rootFile, final Class<? extends FditElement> type) {
        this.father = father;
        this.rootFile = rootFile;
        this.type = type;
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> updateOkButton());
        setResultConverter(buttonClicked -> {
            final ButtonData data = buttonClicked == null ? null : buttonClicked.getButtonData();
            return data == OK_DONE ?
                    nameTextField.getText() :
                    null;
        });
        Platform.runLater(nameTextField::requestFocus);
    }

    static Node getOkButton(final DialogPane dialogPane) {
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
        grid.add(new Label(TRANSLATOR.getMessage("label.name")), 0, 0);
        grid.add(nameTextField, 0, 1);
        grid.add(errorLabel, 0, 2);
        grid.setHgap(10);
        grid.setVgap(10);
        setHgrow(nameTextField, ALWAYS);
        setFillWidth(nameTextField, true);
        setFillHeight(nameTextField, true);
        return grid;
    }

    void updateOkButton() {
        final CheckResult checkNameResult = checkNewFditElementNameValidity(nameTextField.getText(), father,
                rootFile, type);
        if (checkNameResult.checkFailed()) {
            getOkButton(dialogPane).setDisable(true);
            errorLabel.setText(checkNameResult.getMessage());
            return;
        }
        errorLabel.setText("");
        getOkButton(dialogPane).setDisable(false);
    }
}
