package fdit.gui.utils.dialog;

import fdit.gui.zoneEditor.ZoneType;
import fdit.metamodel.element.Directory;
import fdit.metamodel.zone.Zone;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.storage.nameChecker.FditElementNameChecker.checkNewFditElementNameValidity;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.layout.GridPane.*;
import static javafx.scene.layout.Priority.ALWAYS;

public class ZoneCreationDialog extends FditDialog<Pair<String, ZoneType>> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneCreationDialog.class);

    private final Directory father;
    private final TextInputControl nameTextField = new TextField();
    private final ListView<ZoneType> zoneTypeListView = new ListView<>();
    private final Labeled errorLabel = new Label();
    private final DialogPane dialogPane = createDialogPane();

    public ZoneCreationDialog(final Directory father) {
        this.father = father;
        zoneTypeListView.getItems().addAll(ZoneType.values());
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> updateOkButton());
        zoneTypeListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<ZoneType>) observable -> updateOkButton());
        zoneTypeListView.getSelectionModel().selectFirst();
        zoneTypeListView.setCellFactory(lv -> new ListCell<ZoneType>() {
            @Override
            protected void updateItem(final ZoneType item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(formatListItem(item));
                }
            }
        });
        setResultConverter(buttonClicked -> {
            final ButtonData data = buttonClicked == null ? null : buttonClicked.getButtonData();
            return data == OK_DONE ?
                    new Pair<>(nameTextField.getText(), zoneTypeListView.getSelectionModel().getSelectedItem()) :
                    null;
        });
        nameTextField.requestFocus();
    }

    private static String formatListItem(final ZoneType element) {
        if (element == ZoneType.POLYGON) {
            return TRANSLATOR.getMessage("zoneType.polygon");
        }
        throw new RuntimeException("Unknown zone type");
    }

    private static Node getOkButton(final DialogPane dialogPane) {
        return dialogPane.lookupButton(ButtonType.OK);
    }

    private DialogPane createDialogPane() {
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefHeight(400);
        dialogPane.setMaxHeight(400);
        dialogPane.setPrefWidth(600);
        dialogPane.setMaxWidth(600);
        dialogPane.setContent(createGridPane());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getOkButton(dialogPane).setDisable(true);
        return dialogPane;
    }

    private Node createGridPane() {
        final GridPane grid = new GridPane();
        grid.add(new Label(TRANSLATOR.getMessage("label.name")), 0, 0);
        grid.add(nameTextField, 0, 1);
        grid.add(new Label(TRANSLATOR.getMessage("label.chooseZoneType")), 0, 2);
        grid.add(zoneTypeListView, 0, 3);
        grid.add(errorLabel, 0, 4);
        grid.setHgap(10);
        grid.setVgap(10);
        setHgrow(nameTextField, ALWAYS);
        setHgrow(zoneTypeListView, ALWAYS);
        setFillWidth(nameTextField, true);
        setFillWidth(zoneTypeListView, true);
        setFillHeight(nameTextField, true);
        setFillHeight(zoneTypeListView, true);
        return grid;
    }

    private void updateOkButton() {
        final CheckResult checkNameResult = checkNewFditElementNameValidity(nameTextField.getText(), father,
                FDIT_MANAGER.getRootFile(), Zone.class);
        if (checkNameResult.checkFailed()) {
            getOkButton(dialogPane).setDisable(true);
            errorLabel.setText(checkNameResult.getMessage());
            return;
        }
        if (zoneTypeListView.getSelectionModel().getSelectedItems().isEmpty()) {
            getOkButton(dialogPane).setDisable(true);
            errorLabel.setText(TRANSLATOR.getMessage("error.noZoneTypeSelected"));
            return;
        }
        errorLabel.setText("");
        getOkButton(dialogPane).setDisable(false);
    }
}