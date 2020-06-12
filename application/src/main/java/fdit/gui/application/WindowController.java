package fdit.gui.application;

import fdit.gui.EditorController;
import fdit.gui.FditSplitPaneSkin;
import fdit.gui.application.commands.create.ActionTriggerCreationCommand;
import fdit.gui.application.commands.create.ExecutionCreationCommand;
import fdit.gui.application.commands.create.SchemaCreationCommand;
import fdit.gui.application.commands.load.LoadAlterationTriggerCommand;
import fdit.gui.application.commands.load.LoadExecutionCommand;
import fdit.gui.application.commands.load.LoadScenarioCommand;
import fdit.gui.application.commands.move.MoveFditElementCommand;
import fdit.gui.application.treeView.FditMenuableTreeCell;
import fdit.gui.application.treeView.FditTreeItem;
import fdit.gui.executionEditor.ExecutionEditorController;
import fdit.gui.filterEditor.FilterEditorController;
import fdit.gui.filterEditor.OpenFilterEditorCommand;
import fdit.gui.schemaEditor.SchemaEditorController;
import fdit.gui.triggerEditor.ActionTriggerEditorController;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.gui.zoneEditor.OpenZoneEditorCommand;
import fdit.gui.zoneEditor.ZoneEditorController;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.FditHistoryListener;
import fdit.metamodel.FditElementVisitor;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.delete.DeletionUtils.GLOBAL_TMP_DIR_LOCATION;
import static fdit.gui.application.notifications.Notifier.showLoadingNotification;
import static fdit.gui.application.treeView.DraggableTreeView.initializeDraggableCellFactory;
import static fdit.gui.filterEditor.OpenFilterEditorCommand.OpenFilterType.EXISTING_FILTER;
import static fdit.gui.utils.FXUtils.*;
import static fdit.gui.zoneEditor.OpenZoneEditorCommand.OpenZoneType.EXISTING_ZONE;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.WAIT;
import static org.apache.commons.io.FileUtils.forceDelete;

public class WindowController implements Initializable {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(WindowController.class);
    private final Map<FditElement, Editor> editors = newHashMap();
    @FXML
    private TreeView<FditElement> treeView;
    @FXML
    private StackPane editorStackPane;
    @FXML
    private SplitPane verticalSplitPane;
    private Editor zoneEditor;
    private Editor filterEditor;
    private Editor previusEditor;

    private static void editorInForegroundChanged(final Editor previousEditor, final Editor nextEditor) {
        if (previousEditor != null) {
            previousEditor.onBackground();
        }
        nextEditor.requestFocus();
    }

    private static boolean isDisplayableElement(final FditElement element) {
        return new FditElementVisitor<Boolean>() {
            @Override
            public Boolean visitDirectory(final Directory directory) {
                return true;
            }

            @Override
            public Boolean visitSchema(final Schema schema) {
                return true;
            }

            @Override
            public Boolean visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                return false;
            }

            @Override
            public Boolean visitExecution(final Execution execution) {
                return true;
            }

            @Override
            public Boolean visitRecording(final Recording recording) {
                return true;
            }

            @Override
            public Boolean visitZone(final Zone zone) {
                return true;
            }

            @Override
            public Boolean visitLTLFilter(final LTLFilter ltlFilter) {
                return true;
            }

            @Override
            public Boolean visitAlterationTrigger(ActionTrigger trigger) {
                return true;
            }
        }.accept(element);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        listenProjectTree();
        initializeTreeView();
        verticalSplitPane.setSkin(new FditSplitPaneSkin(verticalSplitPane));
        FDIT_MANAGER.getCommandExecutor().getHistory().addListener(createHistoryListener());
    }

    void onClose() throws IOException {
        forceDelete(GLOBAL_TMP_DIR_LOCATION);
        closeAllEditors();
    }

    private void listenProjectTree() {
        FDIT_MANAGER.addListener(new FditManagerListener() {
            @Override
            public void rootChanged() {
                Platform.runLater(() -> {
                    closeAllEditors();
                    final TreeItem rootTreeItem = toTreeItem(FDIT_MANAGER.getRoot());
                    FDIT_MANAGER.setRootTreeItem((FditTreeItem) rootTreeItem);
                    treeView.setRoot(rootTreeItem);
                    sortTreeItem(rootTreeItem);
                    rootTreeItem.setExpanded(true);
                });
            }

            @Override
            public void elementAdded(final FditElement element) {
                if (!isDisplayableElement(element)) {
                    return;
                }
                final TreeItem newTreeItem = toTreeItem(element);
                final TreeItem<FditElement> fatherTreeItem = findTreeItem(treeView, element.getFather());
                Platform.runLater(() -> {
                    fatherTreeItem.getChildren().add(newTreeItem);
                    sortTreeItem(fatherTreeItem);
                    fatherTreeItem.setExpanded(true);
                });
            }

            @Override
            public void elementRemoved(final FditElement element) {
                if (!isDisplayableElement(element)) {
                    return;
                }
                final TreeItem<FditElement> itemToRemove = findTreeItem(treeView, element);
                if (itemToRemove != null) {
                    Platform.runLater(() -> itemToRemove.getParent().getChildren().remove(itemToRemove));
                }
                closeImpactedEditors(element);
            }

            @Override
            public void elementRenamed(final FditElement element) {
                if (isDisplayableElement(element)) {
                    if (!(element instanceof Root)) {
                        final TreeItem<FditElement> fatherTreeItem = findTreeItem(treeView, element.getFather());
                        Platform.runLater(() -> sortTreeItem(fatherTreeItem));
                    }
                }
            }

            @Override
            public void elementMoved(final FditElement element, final Directory oldFather) {
                if (!isDisplayableElement(element)) {
                    return;
                }
                final TreeItem<FditElement> itemToMove = findTreeItem(treeView, element);
                final TreeItem<FditElement> newFatherTreeItem = findTreeItem(treeView, element.getFather());
                final TreeItem<FditElement> oldFatherTreeItem = findTreeItem(treeView, oldFather);
                Platform.runLater(() -> {
                    oldFatherTreeItem.getChildren().remove(itemToMove);
                    newFatherTreeItem.getChildren().add(itemToMove);
                    newFatherTreeItem.setExpanded(true);
                    sortTreeItem(newFatherTreeItem);
                });
            }
        });
    }

    private void initializeTreeView() {
        final MultipleSelectionModel<TreeItem<FditElement>> selectionModel = treeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        treeView.setCellFactory(initializeDraggableCellFactory(() -> new FditMenuableTreeCell(),
                (elementToMove, newFather) -> {
                    if (elementToMove.getFather() == newFather) {
                        return;
                    }
                    FDIT_MANAGER.getCommandExecutor()
                            .execute(new MoveFditElementCommand(elementToMove,
                                    newFather));
                }));

        setOnDoubleClick(treeView, event -> {
            if (selectionModel.getSelectedItems().size() != 1) {
                return;
            }
            final TreeItem<FditElement> selectedItem = selectionModel.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            final FditElement element = selectedItem.getValue();
            if (element instanceof Schema) {
                loadTextualScenarioEditor((Schema) element);
                return;
            }
            if (element instanceof Zone) {
                FDIT_MANAGER.getCommandExecutor().execute(
                        new OpenZoneEditorCommand((Zone) element));
                return;
            }
            if (element instanceof Execution) {
                loadExecutionEditor((Execution) element);
                return;
            }
            if (element instanceof ActionTrigger) {
                loadAlterationTriggerEditor((ActionTrigger) element);
            }
            if (element instanceof LTLFilter) {
                FDIT_MANAGER.getCommandExecutor().execute(
                        new OpenFilterEditorCommand((LTLFilter) element));
            }
        });
    }

    private FditHistoryListener createHistoryListener() {
        return new FditHistoryListener() {
            @Override
            public void commandExecuted(final Command command, final CommandType commandType) {
                if (command instanceof SchemaCreationCommand) {
                    Platform.runLater(() -> openScenarioEditor(((SchemaCreationCommand) command).getSubject()));
                }
                if (command instanceof ExecutionCreationCommand) {
                    Platform.runLater(() -> openExecutionEditor(((ExecutionCreationCommand) command).getSubject()));
                }
                if (command instanceof ActionTriggerCreationCommand) {
                    Platform.runLater(() -> openAlterationTriggerEditor(((ActionTriggerCreationCommand) command)
                            .getSubject()));
                }
                if (command instanceof LoadScenarioCommand) {
                    Platform.runLater(() -> openScenarioEditor(((LoadScenarioCommand) command).gescenario()));
                }
                if (command instanceof LoadExecutionCommand) {
                    Platform.runLater(() -> openExecutionEditor(((LoadExecutionCommand) command).getExecution()));
                }
                if (command instanceof LoadAlterationTriggerCommand) {
                    Platform.runLater(() -> openAlterationTriggerEditor(((LoadAlterationTriggerCommand) command)
                            .getTrigger()));
                }
                if (command instanceof OpenZoneEditorCommand) {
                    Platform.runLater(() -> openZoneEditor((OpenZoneEditorCommand) command));
                }
                if (command instanceof OpenFilterEditorCommand) {
                    Platform.runLater(() -> openFilterEditor((OpenFilterEditorCommand) command));
                }
            }

            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                if (command instanceof SchemaCreationCommand) {
                    Platform.runLater(() -> closeEditor(((SchemaCreationCommand) command).getSubject()));
                }
                if (command instanceof ActionTriggerCreationCommand) {
                    Platform.runLater(() -> closeEditor(((ActionTriggerCreationCommand) command).getSubject()));
                }
                if (command instanceof ExecutionCreationCommand) {
                    Platform.runLater(() -> closeEditor(((ExecutionCreationCommand) command).getSubject()));
                }
                if (command instanceof LoadScenarioCommand) {
                    Platform.runLater(() -> closeEditor(((LoadScenarioCommand) command).gescenario()));
                }
                if (command instanceof LoadExecutionCommand) {
                    Platform.runLater(() -> closeEditor(((LoadExecutionCommand) command).getExecution()));
                }
                if (command instanceof LoadAlterationTriggerCommand) {
                    Platform.runLater(() -> closeEditor(((LoadAlterationTriggerCommand) command).getTrigger()));
                }
                if (command instanceof OpenZoneEditorCommand) {
                    Platform.runLater(() -> closeZoneEditor());
                }
                if (command instanceof OpenFilterEditorCommand) {
                    Platform.runLater(() -> closeFilterEditor());
                }
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                if (command instanceof SchemaCreationCommand) {
                    Platform.runLater(() -> openScenarioEditor(((SchemaCreationCommand) command).getSubject()));
                }
                if (command instanceof ExecutionCreationCommand) {
                    Platform.runLater(() -> openExecutionEditor(((ExecutionCreationCommand) command).getSubject()));
                }
                if (command instanceof LoadScenarioCommand) {
                    Platform.runLater(() -> openScenarioEditor(((LoadScenarioCommand) command).gescenario()));
                }
                if (command instanceof LoadExecutionCommand) {
                    Platform.runLater(() -> openExecutionEditor(((LoadExecutionCommand) command).getExecution()));
                }
                if (command instanceof LoadAlterationTriggerCommand) {
                    Platform.runLater(() -> openAlterationTriggerEditor(((LoadAlterationTriggerCommand) command)
                            .getTrigger()));
                }
                if (command instanceof OpenZoneEditorCommand) {
                    Platform.runLater(() -> openZoneEditor((OpenZoneEditorCommand) command));
                }
                if (command instanceof OpenFilterEditorCommand) {
                    Platform.runLater(() -> openFilterEditor((OpenFilterEditorCommand) command));
                }
            }
        };
    }

    private void loadTextualScenarioEditor(final Schema schema) {
        setCursor(WAIT, editorStackPane);
        startRunnableInBackground(() ->
                FDIT_MANAGER.getCommandExecutor()
                        .execute(new LoadScenarioCommand(schema)));
    }

    private void loadExecutionEditor(final Execution execution) {
        setCursor(WAIT, editorStackPane);
        startRunnableInBackground(() ->
                FDIT_MANAGER.getCommandExecutor()
                        .execute(new LoadExecutionCommand(execution)));
    }

    private void loadAlterationTriggerEditor(final ActionTrigger trigger) {
        setCursor(WAIT, editorStackPane);
        startRunnableInBackground(() ->
                FDIT_MANAGER.getCommandExecutor()
                        .execute(new LoadAlterationTriggerCommand(trigger)));
    }

    private void openScenarioEditor(final Schema schema) {
        previusEditor = null;
        if (putEditorToFrontIfExist(schema)) {
            return;
        }
        openNewEditor(schema, "/fdit/gui/schemaEditor/schemaEditor.fxml",
                new SchemaEditorController(schema));
    }

    private void openExecutionEditor(final Execution execution) {
        previusEditor = null;
        if (putEditorToFrontIfExist(execution)) {
            return;
        }
        openNewEditor(execution,
                "/fdit/gui/executionEditor/executionEditor.fxml",
                new ExecutionEditorController(execution));
    }

    private void openAlterationTriggerEditor(final ActionTrigger trigger) {
        previusEditor = null;
        if (putEditorToFrontIfExist(trigger)) {
            return;
        }
        openNewEditor(trigger,
                "/fdit/gui/triggerEditor/alterationTriggerEditor.fxml",
                new ActionTriggerEditorController(trigger));
    }

    private void openNewEditor(final FditElement fditElement,
                               final String editorFxmlPath,
                               final EditorController controller) {
        previusEditor = null;
        synchronized (editors) {
            setCursor(WAIT, editorStackPane);
            showLoadingNotification(
                    new ThreadSafeStringProperty(TRANSLATOR.getMessage("editor.open.loading")),
                    controller.loadingFinishedProperty(),
                    editorStackPane.getScene());
            try {
                final Node editorPane = loadFxml(getClass().getResource(editorFxmlPath), controller);
                final Editor editor = new Editor(editorPane, controller);
                final Editor previousEditorInForeground = getEditorInForeground();
                editors.put(fditElement, editor);
                editorStackPane.getChildren().add
                        (editorPane);
                editorInForegroundChanged(previousEditorInForeground, editor);
            } catch (final Exception e) {
                createErrorDialog(TRANSLATOR.getMessage("editor.open.error"), e).showAndWait();
            } finally {
                setCursor(DEFAULT, editorStackPane);
            }
        }
    }

    private boolean putEditorToFrontIfExist(final FditElement fditElement) {
        if (editors.containsKey(fditElement)) {
            final Editor editor = editors.get(fditElement);
            putToFront(editor);
            return true;
        }
        return false;
    }

    private void closeAllEditors() {
        for (final Editor editor : editors.values()) {
            editor.onClose();
        }
        if (zoneEditor != null) {
            zoneEditor.onClose();
        }
        if (filterEditor != null) {
            filterEditor.onClose();
        }
        editorStackPane.getChildren().clear();
        editors.clear();
        zoneEditor = null;
        filterEditor = null;
    }

    private void closeImpactedEditors(final FditElement removedElement) {
        new FditElementVisitor<Void>() {
            @Override
            public Void visitDirectory(final Directory directory) {
                return null;
            }

            @Override
            public Void visitSchema(final Schema schema) {
                Platform.runLater(() -> closeEditor(removedElement));
                return null;
            }

            @Override
            public Void visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                return null;
            }

            @Override
            public Void visitExecution(final Execution execution) {
                Platform.runLater(() -> closeEditor(removedElement));
                return null;
            }

            @Override
            public Void visitRecording(Recording recording) {
                return null;
            }

            @Override
            public Void visitZone(final Zone zone) {
                return null;
            }


            @Override
            public Void visitLTLFilter(final LTLFilter ltlFilter) {
                return null;
            }

            @Override
            public Void visitAlterationTrigger(ActionTrigger trigger) {
                return null;
            }
        }.accept(removedElement);
    }

    private void closeEditor(final FditElement fditElement) {
        synchronized (editors) {
            final Editor closedEditor = editors.remove(fditElement);
            if (closedEditor != null) {
                int openedEditors = editorStackPane.getChildren().size();
                final Node editorPaneInForeground = editorStackPane.getChildren().get(openedEditors - 1);
                closedEditor.onClose();
                editorStackPane.getChildren().remove(closedEditor.getEditorPane());
                openedEditors--;
                if (openedEditors > 0 && editorPaneInForeground == closedEditor.getEditorPane()) {
                    final Node nextEditorPane = editorStackPane.getChildren().get(openedEditors - 1);
                    final Editor nextEditor = findEditorWithPane(nextEditorPane);
                    if (nextEditor != null) {
                        nextEditor.requestFocus();
                    }
                }
            }
        }
    }

    private void openZoneEditor(final OpenZoneEditorCommand openZoneEditorCommand) {
        setCursor(WAIT, editorStackPane);
        if (zoneEditor != null) {
            if (zoneEditor == previusEditor) {
                ((ZoneEditorController) zoneEditor.getController()).editZone(openZoneEditorCommand
                        .getZoneToOpen());
            } else {
                putToFront(zoneEditor);
            }
            setCursor(DEFAULT, editorStackPane);
        } else {
            try {
                final ZoneEditorController zoneEditorController = new ZoneEditorController();
                final Node zoneEditorPane = loadFxml(
                        getClass().getResource("/fdit/gui/zoneEditor/zoneEditor.fxml"),
                        zoneEditorController);
                zoneEditor = new Editor(zoneEditorPane, zoneEditorController);
                final Editor previousEditorInForeGround = getEditorInForeground();
                editorStackPane.getChildren().add(zoneEditorPane);
                editorInForegroundChanged(previousEditorInForeGround, zoneEditor);
            } catch (final Exception e) {
                createErrorDialog(TRANSLATOR.getMessage("editor.open.error"), e).showAndWait();
            } finally {
                setCursor(DEFAULT, editorStackPane);
                if (openZoneEditorCommand.getOpenZoneType() == EXISTING_ZONE) {
                    if (zoneEditor != null) {
                        ((ZoneEditorController) zoneEditor.getController()).editZone(openZoneEditorCommand
                                .getZoneToOpen());
                    }
                }
            }
        }
        previusEditor = zoneEditor;
    }

    private void openFilterEditor(final OpenFilterEditorCommand openFilterEditorCommand) {
        setCursor(Cursor.WAIT, editorStackPane);
        if (filterEditor != null) {
            if (filterEditor == previusEditor) {
                ((FilterEditorController) filterEditor.getController()).editFilter(openFilterEditorCommand
                        .getFilterToOpen());
            } else {
                putToFront(filterEditor);
            }
            setCursor(DEFAULT, editorStackPane);
        } else {
            try {
                final FilterEditorController filterEditorController = new FilterEditorController();
                final Node filterEditorPane =
                        loadFxml(
                                getClass().getResource("/fdit/gui/filterEditor/filterEditor.fxml"),
                                filterEditorController);
                filterEditor = new Editor(filterEditorPane, filterEditorController);
                final Editor previousEditorInForeGround = getEditorInForeground();
                editorStackPane.getChildren().add(filterEditorPane);
                editorInForegroundChanged(previousEditorInForeGround, filterEditor);
            } catch (final Exception e) {
                createErrorDialog(TRANSLATOR.getMessage("editor.open.error"), e).showAndWait();
            } finally {
                setCursor(DEFAULT, editorStackPane);
                if (openFilterEditorCommand.getFilterType() == EXISTING_FILTER) {
                    if (filterEditor != null) {
                        ((FilterEditorController) filterEditor.getController()).editFilter(openFilterEditorCommand
                                .getFilterToOpen());
                    }
                }
            }
        }
        previusEditor = filterEditor;
    }

    private void closeZoneEditor() {
        editorStackPane.getChildren().remove(zoneEditor.getEditorPane());
    }

    private void closeFilterEditor() {
        editorStackPane.getChildren().remove(filterEditor.getEditorPane());
    }

    private TreeItem toTreeItem(final FditElement element) {
        if (!isDisplayableElement(element)) {
            return null;
        }
        final TreeItem treeItem = new FditTreeItem(element, treeView.getSelectionModel());
        if (element instanceof Directory) {
            final Directory directoryElement = (Directory) element;
            for (final FditElement child : directoryElement.getChildren()) {
                final TreeItem childTreeItem = toTreeItem(child);
                if (childTreeItem != null) {
                    treeItem.getChildren().add(childTreeItem);
                }
            }
        }
        return treeItem;
    }

    private void putToFront(final Editor editor) {
        final Node editorPane = editor.getEditorPane();
        final Editor previousEditorInForeground = getEditorInForeground();
        if (!editorStackPane.getChildren().contains(editorPane)) {
            editorStackPane.getChildren().add(editorPane);
        }
        editorPane.toFront();
        editorInForegroundChanged(previousEditorInForeground, editor);
        setCursor(DEFAULT, editorStackPane);
    }

    private Editor getEditorInForeground() {
        final int editorStackPaneSize = editorStackPane.getChildren().size();
        if (editorStackPaneSize == 0) {
            return null;
        }
        final Node editorPane = editorStackPane.getChildren().get(editorStackPaneSize - 1);
        return findEditorWithPane(editorPane);
    }

    private Editor findEditorWithPane(final Node editorPane) {
        if (zoneEditor != null && editorPane == zoneEditor.getEditorPane()) {
            return zoneEditor;
        }
        for (final Entry<FditElement, Editor> entry : editors.entrySet()) {
            final Editor editor = entry.getValue();
            if (editor.getEditorPane() == editorPane) {
                return editor;
            }
        }
        return null;
    }
}