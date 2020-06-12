package fdit.gui.application;

import fdit.gui.application.concurrent.AsyncExecution;
import fdit.gui.application.notifications.LoadingNotifier;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.CommandExecutor;
import fdit.history.FditHistoryListener;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.gui.Images.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.notifications.Notification.successNotification;
import static fdit.gui.application.notifications.Notifier.showNotification;
import static fdit.gui.utils.FXUtils.*;
import static fdit.storage.LocalFditPropertiesFileStorage.*;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;

public final class FditMenuBarController implements Initializable {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditMenuBarController.class);
    @FXML
    public Menu aboutMenu;
    @FXML
    public Menu versionMenu;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu fileMenu;
    @FXML
    private MenuItem openMenuItem;
    @FXML
    private MenuItem importFileItem;
    @FXML
    private MenuItem invalidateCacheItem;
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private Menu settingsMenu;
    @FXML
    private Menu languageMenu;

    private Map<String, RadioMenuItem> languageMenuItems = newHashMap();

    private static void undoAction() {
        startRunnableInBackground(() -> FDIT_MANAGER.getCommandExecutor().undo());
    }

    private static void redoAction() {
        startRunnableInBackground(() -> FDIT_MANAGER.getCommandExecutor().redo());
    }

    private static File chooseImportedFileBrowser(final Window window) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(FDIT_MANAGER.getRootFile());
        fileChooser.setTitle(TRANSLATOR.getMessage("menu.file.import.chooserTitle"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter(TRANSLATOR.getMessage("menu.file.import.all"),
                "*.sbs",
                "*.bst"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter(TRANSLATOR.getMessage("menu.file.import.sbs"),
                "*.sbs",
                "*.bst"));
        return fileChooser.showOpenDialog(window);
    }

    private static File chooseFileBrowserRoot(final Window window) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        String defaultLocation = new File(System.getProperty("user.home")).toString();
        String customLocation = readFromLocalPropertiesStorage(DEFAULT_LOCATION_PROPERTY);
        if (!customLocation.equals("") && new File(customLocation).exists()) {
            defaultLocation = customLocation;
        }
        directoryChooser.setInitialDirectory(new File(defaultLocation));
        directoryChooser.setTitle(TRANSLATOR.getMessage("menu.file.open.chooserTitle"));
        File choosenDir = directoryChooser.showDialog(window);
        if (choosenDir != null) {
            writeToLocalPropertiesStorage(DEFAULT_LOCATION_PROPERTY, choosenDir.getParent());
        }
        return choosenDir;
    }

    private static void setLanguage(final RadioMenuItem languageItem) {
        LANGUAGES_MANAGER.setLocale((Locale) languageItem.getUserData());
        writeToLocalPropertiesStorage(LANG_PROPERTY, languageItem.getUserData().toString());
        languageItem.setSelected(true);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeMenuTexts();
        initializeMenuIcon();

        openMenuItem.setOnAction(event -> chooseFileTreeRoot());
        openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

        importFileItem.setDisable(true);
        importFileItem.setOnAction(event -> chooseImportedFile());
        importFileItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));

        invalidateCacheItem.setOnAction(event -> invalidateCacheAction());

        editMenu.setOnAction(event -> updateEditButtons());

        undoMenuItem.setOnAction(event -> undoAction());
        undoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoMenuItem.setDisable(true);

        redoMenuItem.setOnAction(event -> redoAction());
        redoMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        redoMenuItem.setDisable(true);

        deleteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        deleteMenuItem.setDisable(true);

        versionMenu.getItems().add(new MenuItem(FDIT_MANAGER.getVersion()));

        final ToggleGroup languageItemsGroup = new ToggleGroup();
        for (final Entry<String, Locale> entry : FDIT_MANAGER.getLanguages().entrySet()) {
            final RadioMenuItem languageItem = new RadioMenuItem(entry.getKey());
            if (entry.getKey().equals("English")) {
                languageItem.setGraphic(new ImageView(ENGLISH_FLAG));
            } else {
                languageItem.setGraphic(new ImageView(FRENCH_FLAG));
            }
            languageMenuItems.put(entry.getValue().toString(), languageItem);
            languageItem.setUserData(entry.getValue());
            languageItem.setToggleGroup(languageItemsGroup);
            languageItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    setLanguage(languageItem);
                }
            });
            LANGUAGES_MANAGER.addListener(observable -> Platform.runLater(() -> {
                this.initializeMenuTexts();
                this.initializeMenuIcon();
            }));
            languageMenu.getItems().add(languageItem);
        }

        final String language = readFromLocalPropertiesStorage(LANG_PROPERTY);
        if (!language.isEmpty()) {
            setLanguage(languageMenuItems.get(language));
        } else {
            final RadioMenuItem item = languageMenuItems.get(getDefault().toString());
            if (item != null) {
                setLanguage(item);
            } else {
                setLanguage(languageMenuItems.get(ENGLISH.toString()));
            }
        }

        FDIT_MANAGER.getCommandExecutor().getHistory().addListener(new FditHistoryListener() {
            @Override
            public void commandExecuted(final Command command, final CommandType commandType) {
                updateEditButtons();
            }

            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                updateEditButtons();
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                updateEditButtons();
            }

            @Override
            public void commandFailed(final Command command, final Throwable throwable) {
                updateEditButtons();
            }
        });
        FDIT_MANAGER.addListener(new FditManagerListener() {
            @Override
            public void rootTreeItemChanged() {
                updateEditButtons();
            }

            @Override
            public void elementChanged(final Change<? extends TreeItem<FditElement>> elementSelected) {
                deleteMenuItem.setDisable(elementSelected.getList().stream().anyMatch(fditElementTreeItem ->
                        fditElementTreeItem
                                .getValue() instanceof Root));
            }
        });
        listenProjectTree();
    }

    private void initializeMenuTexts() {
        fileMenu.setText(TRANSLATOR.getMessage("menu.file"));
        openMenuItem.setText(TRANSLATOR.getMessage("menu.file.open"));
        importFileItem.setText(TRANSLATOR.getMessage("menu.file.import"));
        invalidateCacheItem.setText(TRANSLATOR.getMessage("menu.file.invalidateCache"));
        editMenu.setText(TRANSLATOR.getMessage("menu.edit"));
        undoMenuItem.setText(TRANSLATOR.getMessage("menu.edit.undo"));
        redoMenuItem.setText(TRANSLATOR.getMessage("menu.edit.redo"));
        deleteMenuItem.setText(TRANSLATOR.getMessage("menu.edit.delete"));
        settingsMenu.setText(TRANSLATOR.getMessage("menu.settings"));
        aboutMenu.setText(TRANSLATOR.getMessage("menu.about"));
        versionMenu.setText(TRANSLATOR.getMessage("menu.about.version"));
        languageMenu.setText(TRANSLATOR.getMessage("menu.settings.language"));
    }

    private void initializeMenuIcon() {
        openMenuItem.setGraphic(new ImageView(OPEN));
        importFileItem.setGraphic(new ImageView(IMPORT));
        invalidateCacheItem.setGraphic(new ImageView(REMOVE_CACHE));
        undoMenuItem.setGraphic(new ImageView(UNDO));
        redoMenuItem.setGraphic(new ImageView(REDO));
        deleteMenuItem.setGraphic(new ImageView(DELETE_FILE));
        languageMenu.setGraphic(new ImageView(FLAG));
    }

    private void listenProjectTree() {
        FDIT_MANAGER.addListener(new FditManagerListener() {
            @Override
            public void rootChanged() {
                Platform.runLater(() -> importFileItem.setDisable(false));
            }
        });
    }

    private void updateEditButtons() {
        final CommandExecutor commandExecutor = FDIT_MANAGER.getCommandExecutor();
        Platform.runLater(() -> {
            updateUndoButton(commandExecutor);
            updateRedoButton(commandExecutor);
            updateDeleteButton();
        });
    }

    private void updateUndoButton(final CommandExecutor commandExecutor) {
        undoMenuItem.setDisable(!commandExecutor.canUndo());
        final String baseUndoMessage = TRANSLATOR.getMessage("menu.edit.undo");
        final Command undoCommand = commandExecutor.getRealUndoCommand();
        if (undoCommand != null) {
            undoMenuItem.setText(baseUndoMessage + ' ' + undoCommand.getContent());
        } else {
            undoMenuItem.setText(baseUndoMessage);
        }
    }

    private void updateDeleteButton() {
        deleteMenuItem.setOnAction(FDIT_MANAGER.getRootTreeItem().createRemoveAction());
        deleteMenuItem.setDisable(FDIT_MANAGER.getRootTreeItem() == null);
    }

    private void updateRedoButton(final CommandExecutor commandExecutor) {
        redoMenuItem.setDisable(!commandExecutor.canRedo());
        final String baseRedoMessage = TRANSLATOR.getMessage("menu.edit.redo");
        if (commandExecutor.canRedo()) {
            redoMenuItem.setText(baseRedoMessage + ' ' + commandExecutor.getRealRedoCommand().getContent());
        } else {
            redoMenuItem.setText(baseRedoMessage);
        }
    }

    private void chooseFileTreeRoot() {
        final Window window = menuBar.getScene().getWindow();
        final File fileBrowserRoot = chooseFileBrowserRoot(window);
        if (fileBrowserRoot != null) {
            final AsyncExecution<Void> service =
                    createSimpleAsyncExecution(() ->
                            FDIT_MANAGER
                                    .loadRoot(fileBrowserRoot, getRecordingLoadingCallback()))
                            .onFailed(throwable -> createErrorDialog(TRANSLATOR.getMessage("menu.file.open.error"),
                                    throwable).showAndWait())
                            .onFinished(() -> setCursor(Cursor.DEFAULT, menuBar));
            setCursor(Cursor.WAIT, menuBar);
            service.startInBackground();
        }
    }

    private void chooseImportedFile() {
        final Window window = menuBar.getScene().getWindow();
        final File importedFile = chooseImportedFileBrowser(window);
        if (importedFile != null) {
            final AsyncExecution<Void> service =
                    createSimpleAsyncExecution(() ->
                            FDIT_MANAGER
                                    .importFile(importedFile, getRecordingLoadingCallback()))
                            .onFailed(throwable -> createErrorDialog(TRANSLATOR.getMessage("menu.file.import.error"),
                                    throwable).showAndWait())
                            .onFinished(() -> setCursor(Cursor.DEFAULT, menuBar));
            setCursor(Cursor.WAIT, menuBar);
            service.startInBackground();
        }
    }

    private void invalidateCacheAction() {
        final Alert confirmationDialog = createConfirmationDialog(
                TRANSLATOR.getMessage("menu.file.invalidateCache.confirm"));
        confirmationDialog.showAndWait();
        if (confirmationDialog.getResult() == ButtonType.OK) {
            new LoadingNotifier<Void>() {
                @Override
                protected void onSucceeded(final Void result) {
                    showNotification(
                            successNotification(TRANSLATOR.getMessage("menu.file.invalidateCache.success")),
                            menuBar.getScene());
                }

                @Override
                protected void onFailed(final Throwable throwable) {
                    createErrorDialog(TRANSLATOR.getMessage("menu.file.invalidateCache.error")).show();
                }

                @Override
                protected Void run() throws Exception {
                    setLoadingMessage(TRANSLATOR.getMessage("menu.file.invalidateCache.deleteDir"));
                    FDIT_MANAGER.deleteHiddenDirectories();
                    setLoadingMessage(TRANSLATOR.getMessage("menu.file.invalidateCache.reloadRoot"));
                    FDIT_MANAGER.reloadRoot(getRecordingLoadingCallback());
                    setLoadingMessage(TRANSLATOR.getMessage("menu.file.invalidateCache.emptyDB"));
                    FDIT_DATABASE.empty();
                    return null;
                }
            }.startInBackground();
        }
    }

    private RecordingInDatabaseLoadingCallback getRecordingLoadingCallback() {
        return new DefaultRecordingInDatabaseLoading(menuBar.getScene());
    }
}