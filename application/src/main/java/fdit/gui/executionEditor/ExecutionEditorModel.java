package fdit.gui.executionEditor;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.gui.application.FditManagerListener;
import fdit.gui.schemaEditor.schemaInterpretation.SchemaInterpreter;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.FditHistory;
import fdit.history.FditHistoryListener;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.storage.nameChecker.CheckResult;
import fdit.tools.i18n.MessageTranslator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.executionEditor.ExecutionUtils.*;
import static fdit.gui.utils.FXUtils.startRunnableInBackground;
import static fdit.metamodel.element.DirectoryUtils.gatherAllRecordings;
import static fdit.metamodel.execution.Execution.SCHEMA;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.alteration.AlterationSpecificationConverter.convertAlterationToIncident;
import static fdit.storage.alteration.AlterationSpecificationConverterUtils.mergeAlterationSpecification;
import static fdit.storage.nameChecker.CheckResult.fail;
import static fdit.storage.nameChecker.CheckResult.success;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.find;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;
import static org.codefx.libfx.listener.handle.ListenerHandles.createFor;

public final class ExecutionEditorModel {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ExecutionEditorModel.class);

    private final BooleanProperty enabledGenerationButton = new ThreadSafeBooleanProperty(false);
    private final ObjectProperty<Recording> selectedRecording = new SimpleObjectProperty<>(EMPTY_RECORDING);
    private final ObservableList<Schema> selectableSchemas = observableArrayList();
    private final ObservableList<Recording> selectableRecordings = observableArrayList();
    private final Collection<ExecutionEditorModelListener> listeners = newArrayList();
    private final List<File> incidentFiles = newArrayList();
    private final List<AlterationSpecification> specifications = newArrayList();
    private final Collection<ListenerHandle> listenerHandles = newArrayList();
    private final Execution execution;
    private final FditManagerListener fditManagerListener = createFditManagerListener();
    private final ObservableList<SchemaRowModel> schemaRowModels = observableArrayList();
    private final StringProperty loadingMessage = new ThreadSafeStringProperty("");

    @SuppressWarnings("TypeMayBeWeakened")
    public ExecutionEditorModel(final Execution execution) {
        this.execution = execution;
        selectableSchemas.add(SCHEMA);
        selectableRecordings.addAll(EMPTY_RECORDING);
        selectableRecordings.addAll(gatherAllRecordings(FDIT_MANAGER.getRoot()));
        selectedRecording.setValue(execution.getRecording());
        listenerHandles.add(createAttached(selectedRecording, observable -> save()));
        createFor(FDIT_MANAGER.getCommandExecutor().getHistory(),
                createHistoryListener())
                .onAttach(FditHistory::addListener)
                .onDetach(FditHistory::removeListener)
                .buildAttached();
        FDIT_MANAGER.addListener(fditManagerListener);
        initializeSchemaModels();
        updateGenerationButton();
    }

    private void initializeSchemaModels() {
        schemaRowModels.addAll(mapping(execution.getSchemas(),
                action -> new SchemaRowModel(execution, action, listeners)));
    }

    private void updateGenerationButton() {
        enabledGenerationButton.set(generationCanBeLaunched());
    }

    private CheckResult createIncidentFiles(final Execution execution,
                                            final Collection<AlterationSpecification> specifications,
                                            final File destination) {
        incidentFiles.clear();
        int testCaseNumber = 0;
        for (final AlterationSpecification specification : specifications) {
            loadingMessage.set(TRANSLATOR.getMessage("loading.createIncident", testCaseNumber + 1, specifications.size()));
            incidentFiles.add(convertAlterationToIncident(
                    specification,
                    specification.getAlterationSchema(),
                    extractRecording(execution),
                    testCaseNumber++,
                    destination));
        }
        return success();
    }

    void addListener(final ExecutionEditorModelListener listener) {
        listeners.add(listener);
    }

    ObservableList<Recording> getSelectableRecordings() {
        return selectableRecordings;
    }

    public CheckResult generateIncidentFiles(final File destination) {
        loadingMessage.set(TRANSLATOR.getMessage("loading.alter"));
        CheckResult result = isExecutionReadyForAlteration();
        if (result.checkFailed()) {
            return result;
        }
        result = createSpecificationsAndIncidents(destination);
        if (result.checkFailed()) {
            return result;
        }
        // TODO: save incidentsFiles
        return success();
    }

    private boolean hasDataChanged() {
        try {
            return selectedRecording.get() != execution.getRecording();
        } catch (final Exception ignored) {
            return false;
        }
    }

    public void save() {
        if (hasDataChanged()) {
            try {
                FDIT_MANAGER.getCommandExecutor().execute(new ExecutionEditionCommand(
                        execution,
                        selectedRecording.get()));
            } catch (Exception ignored) {

            }
        }
    }

    private FditHistoryListener createHistoryListener() {
        return new FditHistoryListener() {

            @Override
            public void commandExecuted(final Command command, final CommandType commandType) {
                if (concernedBySchemaCreationCommand(command)) {
                    addNewSchemaRowModel(((AddSchemaCommand) command).getAddedSchema());
                }
                if (concernedBySchemaDeletionCommand(command)) {
                    removeSchemaRowModel(((SchemaDeletionCommand) command).getDeletedSchema());
                }
                refresh();
            }

            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                if (command instanceof ExecutionEditionCommand) {
                    selectedRecording.setValue(((ExecutionEditionCommand) command).getOldRecording());
                }
                if (concernedBySchemaCreationCommand(command)) {
                    final Schema createdSchema = ((AddSchemaCommand) command).getAddedSchema();
                    removeSchemaRowModel(createdSchema);
                }
                if (concernedBySchemaDeletionCommand(command)) {
                    final SchemaDeletionCommand deletionCommand = (SchemaDeletionCommand) command;
                    addNewSchemaRowModel(deletionCommand.getDeletedSchemaIndex(), deletionCommand.getDeletedSchema());
                }
                refresh();
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                if (command instanceof ExecutionEditionCommand) {
                    selectedRecording.setValue(((ExecutionEditionCommand) command).getNewRecording());
                }
                if (concernedBySchemaCreationCommand(command)) {
                    addNewSchemaRowModel(((AddSchemaCommand) command).getAddedSchema());
                }
                if (concernedBySchemaDeletionCommand(command)) {
                    removeSchemaRowModel(((SchemaDeletionCommand) command).getDeletedSchema());
                }
                refresh();
            }

            private boolean concernedBySchemaDeletionCommand(final Command command) {
                return command instanceof SchemaDeletionCommand &&
                        ((SchemaDeletionCommand) command).getSubject() == execution;
            }

            private boolean concernedBySchemaCreationCommand(final Command command) {
                return command instanceof AddSchemaCommand &&
                        ((AddSchemaCommand) command).getSubject() == execution;
            }
        };
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement elementAdded) {
                if (elementAdded instanceof Recording) {
                    selectableRecordings.add((Recording) elementAdded);
                }
            }

            @Override
            public void elementRemoved(final FditElement elementRemoved) {
                if (elementRemoved instanceof Recording) {
                    if (selectedRecording.getValue() == elementRemoved) {
                        selectedRecording.setValue(EMPTY_RECORDING);
                    }
                    selectableRecordings.remove(elementRemoved);
                }
            }

            @Override
            public void elementEdited(final FditElement editedElement) {
                if (editedElement == execution) {
                    refresh();
                }
            }
        };
    }

    private void addNewSchemaRowModel(final Schema schema) {
        addNewSchemaRowModel(schemaRowModels.size(), schema);
    }

    private void addNewSchemaRowModel(final int index, final Schema schema) {
        schemaRowModels.add(index, new SchemaRowModel(execution, schema, listeners));
    }

    private void removeSchemaRowModel(final Schema schema) {
        schemaRowModels.remove(findSchemaRowModel(schema));
    }

    private SchemaRowModel findSchemaRowModel(final Schema schema) {
        return find(schemaRowModels, model -> model.getSchema() == schema);
    }

    public CheckResult isExecutionReadyForAlteration() {
        try {
            final CheckResult checkResult = isConfigurationValid(execution);
            if (checkResult.checkFailed()) {
                return checkResult;
            }
            if (execution.getSchemas().isEmpty()) {
                return fail(TRANSLATOR.getMessage("error.noSchemas"));
            }
            return success();
        } catch (final Exception ex) {
            return fail(TRANSLATOR.getMessage("error.unknown", ex.getMessage()));
        }
    }

    private CheckResult createSpecificationsAndIncidents(final File destination) {
        extractRecording(execution).load();
        final CheckResult result = extractAlterationSpecifications(execution);
        if (result.checkFailed()) {
            return result;
        }
        final CheckResult incidentFilesResult = createIncidentFiles(execution, specifications, destination);
        if (incidentFilesResult.checkFailed()) {
            return incidentFilesResult;
        }
        return success();
    }

    private CheckResult extractAlterationSpecifications(final Execution execution) {
        specifications.clear();
        final SchemaInterpreter interpreter = new SchemaInterpreter(new AttackScenarioFacade());
        interpreter.initialize();
        final Collection<Vector<AlterationSpecification>> data = newArrayList();
        for (final Schema schema : execution.getSchemas()) {
            if (schema == SCHEMA) {
                return fail(TRANSLATOR.getMessage("error.noSelectedscenario"));
            }
            schema.setRecording(execution.getRecording());
            final String errors = interpreter.getSemanticErrors(schema);
            if (!errors.isEmpty()) {
                return fail(TRANSLATOR.getMessage("error.scenarioErrors", schema.getName(), errors));
            }
            data.add(new Vector<>(interpreter.extractSpecifications(schema)));
        }
        final AlterationSpecification[][] uniqueCombinations = allUniqueCombinations(data);
        for (final AlterationSpecification[] uniqueCombination : uniqueCombinations) {
            specifications.add(mergeAlterationSpecification(asList(uniqueCombination)));
        }
        return success();
    }

    private boolean generationCanBeLaunched() {
        if (getSelectedRecording() != EMPTY_RECORDING && !getSchemaRowModels().isEmpty()) {
            for (final SchemaRowModel rowModel : getSchemaRowModels()) {
                if (rowModel.getSelectedSchema() == SCHEMA) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void refresh() {
        updateGenerationButton();
    }

    void close() {
        for (final ListenerHandle listenerHandle : listenerHandles) {
            listenerHandle.detach();
        }
        listenerHandles.clear();
        FDIT_MANAGER.removeListener(fditManagerListener);
    }

    ObjectProperty<Recording> selectedRecordingProperty() {
        return selectedRecording;
    }

    Recording getSelectedRecording() {
        return selectedRecording.get();
    }

    void setSelectedRecording(final Recording selectedRecording) {
        this.selectedRecording.set(selectedRecording);
    }

    void addSchema() {
        startRunnableInBackground(() ->
                FDIT_MANAGER.getCommandExecutor().execute(new AddSchemaCommand(execution)));
    }

    ObservableList<SchemaRowModel> getSchemaRowModels() {
        return schemaRowModels;
    }

    BooleanProperty enabledGenerationButtonProperty() {
        return enabledGenerationButton;
    }

    boolean getEnabledGenerationButton() {
        return enabledGenerationButton.get();
    }

    public StringProperty loadingMessageProperty() {
        return loadingMessage;
    }

    interface ExecutionEditorModelListener {
        void scenarioUpdated();
    }
}