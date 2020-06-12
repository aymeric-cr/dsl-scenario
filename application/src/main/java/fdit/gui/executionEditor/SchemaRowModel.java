package fdit.gui.executionEditor;

import fdit.metamodel.execution.Execution;
import fdit.metamodel.schema.Schema;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import java.util.Collection;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.executionEditor.ExecutionEditorModel.ExecutionEditorModelListener;
import static fdit.metamodel.element.DirectoryUtils.gatherAllTextualScenarios;
import static fdit.metamodel.execution.Execution.SCHEMA;
import static javafx.collections.FXCollections.observableArrayList;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class SchemaRowModel {

    private final ObservableList<Schema> selectableSchemas = observableArrayList();
    private final ObjectProperty<Schema> selectedSchema = new SimpleObjectProperty<>(SCHEMA);
    private final Collection<ExecutionEditorModelListener> listeners;
    private final Execution execution;
    private Schema schema;

    SchemaRowModel(final Execution execution,
                   final Schema schema,
                   final Collection<ExecutionEditorModelListener> listeners) {
        this.execution = execution;
        this.schema = schema;
        this.listeners = listeners;
        selectedSchema.setValue(schema);
        selectableSchemas.add(SCHEMA);
        selectableSchemas.addAll(gatherAllTextualScenarios(FDIT_MANAGER.getRoot()));
        initialize();
    }

    private void initialize() {
        createAttached(selectedSchema, observable -> save());
    }

    public void save() {
        if (hasDataChanged()) {
            FDIT_MANAGER.getCommandExecutor().execute(new SchemaEditionCommand(
                    execution,
                    schema,
                    getSelectedSchema()));
        }
        schema = getSelectedSchema();
    }

    private boolean hasDataChanged() {
        return !getSelectedSchema().equals(schema);
    }

    void processDeletion() {
        FDIT_MANAGER.getCommandExecutor().execute(new SchemaDeletionCommand(execution, schema));
    }

    public Schema getSchema() {
        return schema;
    }

    ObservableList<Schema> getSelectableSchemas() {
        return selectableSchemas;
    }

    Schema getSelectedSchema() {
        return selectedSchema.get();
    }

    void setSelectedSchema(Schema selectedSchema) {
        this.selectedSchema.set(selectedSchema);
    }

    ObjectProperty<Schema> selectedSchemaProperty() {
        return selectedSchema;
    }

    public void addListener(final ExecutionEditorModelListener listener) {
        listeners.add(listener);
    }
}