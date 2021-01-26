package fdit.gui.application.treeView;

import fdit.gui.application.commands.create.*;
import fdit.gui.application.commands.rename.RenameFditElementCommand;
import fdit.gui.filterEditor.OpenFilterEditorCommand;
import fdit.gui.utils.dialog.BasicFditElementCreationDialog;
import fdit.gui.utils.dialog.FditElementRenameDialog;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.Images.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.delete.DeletionUtils.delete;
import static fdit.gui.utils.FXUtils.createSimpleAsyncExecution;
import static fdit.gui.utils.dialog.DeletionConfirmationUtils.confirmDeletion;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.exists;
import static fdit.tools.stream.StreamUtils.mapping;
import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.WAIT;

public class FditTreeItem extends MenuableTreeItem<FditElement> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditTreeItem.class);
    private final MultipleSelectionModel<TreeItem<FditElement>> selectionModel;

    public FditTreeItem(final FditElement element,
                        final MultipleSelectionModel<TreeItem<FditElement>> selectionModel) {
        super(element);
        this.selectionModel = selectionModel;
    }

    @Override
    public ContextMenu getContextMenu() {
        final Collection<MenuItem> items = newArrayList();
        if (selectionModel.getSelectedItems().size() == 1) {
            if (getValue() instanceof Directory) {
                items.add(createCreationMenu());
            }
            if (!(getValue() instanceof Root)) {
                items.add(createRenameMenu());
            }
        }
        if (!exists(selectionModel.getSelectedItems(), treeItem -> treeItem.getValue() instanceof Root)) {
            items.add(createRemoveMenu());
        }
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(items);
        return contextMenu;
    }

    private MenuItem createCreationMenu() {
        final Menu createMenu = new Menu(TRANSLATOR.getMessage("menu.create"));
        createMenu.setGraphic(new ImageView(CREATE));
        createMenu.getItems().add(createDirectoryMenu());
        createMenu.getItems().add(createExecutionMenu());
        createMenu.getItems().add(createScenarioMenu());
        createMenu.getItems().add(createFilterMenu());
        createMenu.getItems().add(createTriggerMenu());
        return createMenu;
    }

    private MenuItem createDirectoryMenu() {
        final MenuItem createDirectoryMenu = new MenuItem(TRANSLATOR.getMessage("menu.create.directory"));
        createDirectoryMenu.setGraphic(new ImageView(DIRECTORY_ICON));
        createDirectoryMenu.setOnAction(createDirectoryAction());
        return createDirectoryMenu;
    }

    private EventHandler<ActionEvent> createDirectoryAction() {
        return event -> {
            final Directory father = (Directory) getValue();
            final Dialog creationDialog = new BasicFditElementCreationDialog(father,
                    FDIT_MANAGER.getRootFile(),
                    Directory.class);
            creationDialog.setTitle(TRANSLATOR.getMessage("menu.create.directory.dialog.title"));
            creationDialog.setHeaderText(TRANSLATOR.getMessage("menu.create.directory.dialog.header"));
            final Optional<String> name = creationDialog.showAndWait();
            name.ifPresent(s -> FDIT_MANAGER.getCommandExecutor().execute(
                    new CreateDirectoryCommand(father, s)));
        };
    }

    private MenuItem createExecutionMenu() {
        final MenuItem createExecutionMenu = new MenuItem(TRANSLATOR.getMessage("menu.create.execution"));
        createExecutionMenu.setGraphic(new ImageView(START_EXECUTION_ICON));
        createExecutionMenu.setOnAction(createExecutionAction());
        return createExecutionMenu;
    }

    private EventHandler<ActionEvent> createAlterationTriggerAction() {
        return event -> {
            final Directory father = (Directory) getValue();
            final Dialog nameDialog = new BasicFditElementCreationDialog(
                    father,
                    FDIT_MANAGER.getRootFile(),
                    ActionTrigger.class);
            nameDialog.setTitle(TRANSLATOR.getMessage("menu.create.trigger.dialog.title"));
            nameDialog.setHeaderText(TRANSLATOR.getMessage("menu.create.trigger.dialog.header"));
            final Optional<String> newName = nameDialog.showAndWait();
            newName.ifPresent(name -> FDIT_MANAGER.getCommandExecutor().execute(
                    new ActionTriggerCreationCommand(father, name, randomUUID())));
        };
    }


    private EventHandler<ActionEvent> createExecutionAction() {
        return event -> {
            final Directory father = (Directory) getValue();
            final Dialog creationDialog = new BasicFditElementCreationDialog(father, FDIT_MANAGER.getRootFile(),
                    Execution.class);
            creationDialog.setTitle(TRANSLATOR.getMessage("menu.create.execution.dialog.title"));
            creationDialog.setHeaderText(TRANSLATOR.getMessage("menu.create.execution.dialog.header"));
            final Optional<String> name = creationDialog.showAndWait();
            name.ifPresent(name1 -> FDIT_MANAGER.getCommandExecutor().execute(
                    new ExecutionCreationCommand(name1, father)));
        };
    }

    private MenuItem createScenarioMenu() {
        final MenuItem createPatternMenu = new MenuItem(TRANSLATOR.getMessage("menu.create.schema"));
        createPatternMenu.setGraphic(new ImageView(TEXTUAL_SCENARIO_ICON));
        createPatternMenu.setOnAction(createScenarioAction());
        return createPatternMenu;
    }

    private EventHandler<ActionEvent> createScenarioAction() {
        return event -> {
            final Directory father = (Directory) getValue();
            final Dialog nameDialog = new BasicFditElementCreationDialog(father,
                    FDIT_MANAGER.getRootFile(),
                    Schema.class);
            nameDialog.setTitle(TRANSLATOR.getMessage("menu.create.schema.dialog.title"));
            nameDialog.setHeaderText(TRANSLATOR.getMessage("menu.create.schema.dialog.header"));
            final Optional<String> newName = nameDialog.showAndWait();
            newName.ifPresent(name ->
                    FDIT_MANAGER.getCommandExecutor().execute(new SchemaCreationCommand(
                            father, name, "", EMPTY_RECORDING)));
        };
    }

    private MenuItem createFilterMenu() {
        final MenuItem createFilteMenu = new MenuItem(TRANSLATOR.getMessage("menu.create.filter"));
        createFilteMenu.setGraphic(new ImageView(LTLFILTER_ICON));
        createFilteMenu.setOnAction(createFilterAction());
        return createFilteMenu;
    }

    private MenuItem createTriggerMenu() {
        final MenuItem createTriggerMenu = new MenuItem(TRANSLATOR.getMessage("menu.create.trigger"));
        createTriggerMenu.setGraphic(new ImageView(TRIGGER_ICON));
        createTriggerMenu.setOnAction(createAlterationTriggerAction());
        return createTriggerMenu;
    }

    private EventHandler<ActionEvent> createFilterAction() {
        return event -> {
            final Directory father = (Directory) getValue();
            final Dialog nameDialog = new BasicFditElementCreationDialog(
                    father,
                    FDIT_MANAGER.getRootFile(),
                    LTLFilter.class);
            nameDialog.setTitle(TRANSLATOR.getMessage("menu.create.filter.dialog.title"));
            nameDialog.setHeaderText(TRANSLATOR.getMessage("menu.create.filter.dialog.header"));
            final Optional<String> newName = nameDialog.showAndWait();
            newName.ifPresent(name -> {
                final LTLFilterCreationCommand LTLFilterCreationCommand = new LTLFilterCreationCommand(father, name, randomUUID());
                FDIT_MANAGER.getCommandExecutor().executePreCommand(LTLFilterCreationCommand);
                FDIT_MANAGER.getCommandExecutor().execute(new OpenFilterEditorCommand(LTLFilterCreationCommand.getSubject()));
            });
        };
    }

    private MenuItem createRenameMenu() {
        final MenuItem renameMenu = new MenuItem(TRANSLATOR.getMessage("menu.rename"));
        renameMenu.setGraphic(new ImageView(RENAME));
        renameMenu.setOnAction(createRenameAction());
        return renameMenu;
    }

    private EventHandler<ActionEvent> createRenameAction() {
        return (ActionEvent event) -> {
            final FditElement element = getValue();
            final String currentName = element.getName();
            final Dialog renameDialog = new FditElementRenameDialog(element);
            renameDialog.setTitle(TRANSLATOR.getMessage("menu.rename.dialog.title"));
            renameDialog.setHeaderText(TRANSLATOR.getMessage("menu.rename.dialog.header", currentName));
            final Optional<String> newName = renameDialog.showAndWait();
            newName.ifPresent(name -> FDIT_MANAGER.getCommandExecutor().execute(new RenameFditElementCommand(element, name)));
        };
    }

    private MenuItem createRemoveMenu() {
        final MenuItem removeMenu = new MenuItem(TRANSLATOR.getMessage("menu.remove"));
        removeMenu.setGraphic(new ImageView(ERASE));
        removeMenu.setOnAction(createRemoveAction());
        return removeMenu;
    }

    public EventHandler<ActionEvent> createRemoveAction() {
        return (ActionEvent event) -> {
            final Collection<FditElement> toDelete = mapping(selectionModel.getSelectedItems(), TreeItem::getValue);
            final Scene scene = getGraphic().getScene();
            if (!toDelete.isEmpty()) {
                changeCursor(scene, WAIT);
                if (confirmDeletion(toDelete)) {
                    createSimpleAsyncExecution(() -> delete(toDelete))
                            .onFinished(() -> changeCursor(scene, DEFAULT))
                            .startInBackground();
                } else {
                    changeCursor(scene, DEFAULT);
                }
            }
        };
    }

    private void changeCursor(final Scene scene, final Cursor cursor) {
        if (scene != null) {
            scene.setCursor(cursor);
        }
    }

    public ObservableList<TreeItem<FditElement>> getSelectedItems() {
        return selectionModel.getSelectedItems();
    }
}