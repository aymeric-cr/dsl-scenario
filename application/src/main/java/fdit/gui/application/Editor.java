package fdit.gui.application;

import fdit.gui.EditorController;
import javafx.scene.Node;

class Editor {

    private final Node editorPane;
    private final EditorController controller;

    Editor(final Node editorPane, final EditorController controller) {
        this.editorPane = editorPane;
        this.controller = controller;
    }

    Node getEditorPane() {
        return editorPane;
    }

    EditorController getController() {
        return controller;
    }

    void requestFocus() {
        controller.requestFocus();
    }

    void onBackground() {
        controller.onBackground();
    }

    void onClose() {
        controller.onClose();
    }
}
