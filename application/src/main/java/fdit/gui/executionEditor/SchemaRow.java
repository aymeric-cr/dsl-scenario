package fdit.gui.executionEditor;

import fdit.gui.utils.UpdateableComboBox;
import fdit.gui.utils.imageButton.ImageButton;
import fdit.metamodel.schema.Schema;
import javafx.scene.layout.GridPane;

import static fdit.gui.Images.DELETE_CROSS_ICON;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static javafx.geometry.HPos.LEFT;
import static javafx.geometry.HPos.RIGHT;
import static javafx.scene.layout.Priority.ALWAYS;

class SchemaRow extends GridPane {

    private final UpdateableComboBox<Schema> schemaChooser = new UpdateableComboBox<>();
    private final ImageButton deleteButton = new ImageButton();

    SchemaRow() {
        initialize();
        setHgap(10);
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
    }

    private void initialize() {
        initializeTexts();
        deleteButton.setImage(DELETE_CROSS_ICON);
        add(schemaChooser, 0, 0);
        add(deleteButton, 1, 0);
        setHalignment(schemaChooser, RIGHT);
        setHalignment(deleteButton, LEFT);
        setHgrow(schemaChooser, ALWAYS);
        setHgrow(deleteButton, ALWAYS);
    }

    private void initializeTexts() {

    }

    UpdateableComboBox<Schema> getSchemaChooser() {
        return schemaChooser;
    }

    public ImageButton getDeleteButton() {
        return deleteButton;
    }
}