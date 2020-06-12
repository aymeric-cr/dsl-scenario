package fdit.gui.executionEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.metamodel.execution.Execution.SCHEMA;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class AddSchemaCommand implements FditElementCommand<Execution> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(AddSchemaCommand.class);

    private final Execution execution;
    private final Schema addedSchema;

    public AddSchemaCommand(final Execution execution) {
        this(execution, SCHEMA);
    }

    public AddSchemaCommand(final Execution execution, final Schema addedSchema) {
        this.execution = execution;
        this.addedSchema = addedSchema;
    }

    public Schema getAddedSchema() {
        return addedSchema;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createAction.descr");
    }

    @Override
    public void execute() throws Exception {
        execution.addSchema(addedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void undo() throws Exception {
        execution.removeSchema(addedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void redo() throws Exception {
        execution.addSchema(addedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }

    @Override
    public Execution getSubject() {
        return execution;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }
}