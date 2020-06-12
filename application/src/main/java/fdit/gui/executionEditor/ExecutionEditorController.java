package fdit.gui.executionEditor;

import fdit.gui.EditorController;
import fdit.gui.application.notifications.LoadingNotifier;
import fdit.gui.utils.UpdateableComboBox;
import fdit.gui.utils.binding.BindingHandle;
import fdit.gui.utils.imageButton.ImageButton;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.recording.Recording;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.Images.*;
import static fdit.gui.application.notifications.Notification.errorNotification;
import static fdit.gui.application.notifications.Notification.successNotification;
import static fdit.gui.application.notifications.Notifier.showNotification;
import static fdit.gui.utils.FXUtils.setOnPrimaryButtonMouseClicked;
import static fdit.gui.utils.FXUtils.startRunnableInUIThread;
import static fdit.gui.utils.binding.BindingHandles.createAttachBinding;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static javafx.beans.binding.Bindings.bindContentBidirectional;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class ExecutionEditorController implements EditorController {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ExecutionEditorController.class);
    private final String execName;
    private final ExecutionEditorModel model;
    private final Collection<BindingHandle> bindingHandles = newArrayList();
    private final Collection<ListenerHandle> listenerHandles = newArrayList();
    private HashMap<SchemaRowModel, SchemaRowController> schemasRows = newHashMap();
    @FXML
    private UpdateableComboBox<Recording> recordingChooser;
    @FXML
    private VBox schemasVBox;
    @FXML
    private ImageButton addSchemaButton;
    @FXML
    private GridPane executionsPane;
    @FXML
    private ImageButton generateButton;
    @FXML
    private Label preparationLabel;


    public ExecutionEditorController(final Execution execution) {
        execName = execution.getName();
        this.model = new ExecutionEditorModel(execution);
        LANGUAGES_MANAGER.addListener(observable -> initializeTexts());
    }

    @Override
    public void initialize() {
        initializeButtons();
        initializeTexts();
        initializeSchemasRowModelsListener();
        updateStartExecutionButtonEnabling();
        updateGenerateButton();
        initializeExecutionListeners();
        initializeRecordingChooser();
    }

    private void initializeSchemas(final Iterable<SchemaRowModel> schemaRowModels) {
        for (final SchemaRowModel schemaRowModel : schemaRowModels) {
            addSchema(schemaRowModel);
        }
    }

    private void initializeSchemasRowModelsListener() {
        final ObservableList<SchemaRowModel> schemaRowModels = model.getSchemaRowModels();
        listenerHandles.add(createAttached(schemaRowModels, (ListChangeListener<? super SchemaRowModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final SchemaRowModel addedSchemaRowModel = change.getList().get(i);
                        final int index = i;
                        startRunnableInUIThread(() -> addSchema(index, addedSchemaRowModel));
                    }
                }
                if (change.wasRemoved()) {
                    for (final SchemaRowModel removedSchemaRowModel : change.getRemoved()) {
                        startRunnableInUIThread(() -> removeSchema(removedSchemaRowModel));
                    }
                }
            }
        }));
        initializeSchemas(schemaRowModels);
    }

    private void removeSchema(final SchemaRowModel removedSchemaRowModel) {
        schemasVBox.getChildren().remove(schemasRows.remove(removedSchemaRowModel).getView());
    }

    private void addSchema(final SchemaRowModel schemaRowModel) {
        addSchema(schemasVBox.getChildren().size(), schemaRowModel);
    }

    private void addSchema(final int index, final SchemaRowModel addedSchemaRowModel) {
        final SchemaRow schemaRow = new SchemaRow();
        schemasVBox.getChildren().add(index, schemaRow);
        final SchemaRowController schemaRowController = new SchemaRowController(schemaRow, addedSchemaRowModel);
        schemasRows.put(addedSchemaRowModel, schemaRowController);
    }

    private void initializeButtons() {

        addSchemaButton.setImage(ADD_SCHEMA_ICON);
        setOnPrimaryButtonMouseClicked(addSchemaButton, event -> model.addSchema());
    }

    private void initializeTexts() {
        preparationLabel.setText(TRANSLATOR.getMessage("label.preparation", execName));
        generateButton.setToolTip(TRANSLATOR.getMessage("tooltip.execute.generate"));
        addSchemaButton.setToolTip(TRANSLATOR.getMessage("tooltip.execution.addSchema"));
    }

    @Override
    public void onClose() {
        model.close();
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }
        listenerHandles.clear();
        for (final BindingHandle bindingHandle : bindingHandles) {
            bindingHandle.unbind();
        }
        bindingHandles.clear();
        clearInnerListeners();
        model.close();
    }

    public void clearInnerListeners() {
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }
        listenerHandles.clear();
    }

    private void initializeRecordingChooser() {
        final Callback<ListView<Recording>, ListCell<Recording>> cellFactory =
                new Callback<ListView<Recording>, ListCell<Recording>>() {
                    @Override
                    public ListCell<Recording> call(final ListView<Recording> param) {
                        return new ListCell<Recording>() {
                            protected void updateItem(final Recording recording, final boolean empty) {
                                super.updateItem(recording, empty);
                                final String text;
                                if (recording == null || empty) {
                                    text = null;
                                } else if (recording == EMPTY_RECORDING) {
                                    text = TRANSLATOR.getMessage("selectionRecording.noRecording");
                                } else {
                                    text = recording.getName();
                                }
                                Platform.runLater(() -> {
                                    setGraphic(new ImageView(getRecordingIcon(recording)));
                                    setText(text);
                                });
                            }
                        };
                    }
                };
        recordingChooser.setCellFactory(cellFactory);
        recordingChooser.setButtonCell(cellFactory.call(null));
        recordingChooser.getSelectionModel().select(model.getSelectedRecording());
    }

    private void initializeExecutionListeners() {
        setOnPrimaryButtonMouseClicked(generateButton, mouseEvent -> executeAlterationAction());
        listenerHandles.add(createAttached(model.enabledGenerationButtonProperty(),
                (observable, oldValue, newValue) -> updateStartExecutionButtonEnabling()));
        initializeRecordingListener();
    }

    private void executeAlterationAction() {

        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(TRANSLATOR.getMessage("generate.destination"));
        final File destination = directoryChooser.showDialog(executionsPane.getScene().getWindow());

        if (destination != null) {
            new LoadingNotifier<CheckResult>() {

                private final BindingHandle bindingHandle =
                        createAttachBinding(loadingMessageProperty(), model.loadingMessageProperty());

                @Override
                protected CheckResult run() {
                    bindingHandles.add(bindingHandle);
                    return model.generateIncidentFiles(destination);
                }

                @Override
                protected void onSucceeded(final CheckResult result) {
                    if (result.checkFailed()) {
                        showNotification(
                                errorNotification(result.getMessage()),
                                executionsPane.getScene());
                    } else {
                        showNotification(
                                successNotification(TRANSLATOR.getMessage("generate.success")),
                                executionsPane.getScene());
                    }
                }

                @Override
                protected void onFailed(final Throwable throwable) {
                    showNotification(
                            errorNotification(TRANSLATOR.getMessage("error.unknown", throwable.getLocalizedMessage())),
                            executionsPane.getScene());
                }

                @Override
                protected void onFinished() {
                    bindingHandle.unbind();
                    bindingHandles.remove(bindingHandle);
                }
            }.startInBackground();
        }
    }

    private void initializeRecordingListener() {
        bindContentBidirectional(recordingChooser.getItems(), model.getSelectableRecordings());
        listenerHandles.add(createAttached(recordingChooser.getSelectionModel().selectedItemProperty(),
                (observable, oldValue, newValue) -> {
                    if (newValue != model.getSelectedRecording()) {
                        model.setSelectedRecording(newValue);
                    }
                }));
        model.addListener(() -> recordingChooser.refresh());
        listenerHandles.add(createAttached(model.selectedRecordingProperty(), (observable, oldValue, newValue) ->
                startRunnableInUIThread(() -> recordingChooser.getSelectionModel().select(newValue))));
    }

    private void updateGenerateButton() {
        generateButton.setDisable(false);
        if (model.getEnabledGenerationButton()) {
            enableGenerateButton();
        } else {
            disableGenerateButton();
        }
    }

    private void enableGenerateButton() {
        generateButton.setImage(EXECUTE_GREEN);
    }

    private void disableGenerateButton() {
        generateButton.setImage(EXECUTE_RED);
    }


    private void updateStartExecutionButtonEnabling() {
        if (model.getEnabledGenerationButton()) {
            enableGenerateButton();
        } else {
            disableGenerateButton();
        }
    }
}