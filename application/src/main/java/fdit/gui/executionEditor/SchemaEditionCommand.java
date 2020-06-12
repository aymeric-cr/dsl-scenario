package fdit.gui.executionEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;

public class SchemaEditionCommand implements FditElementCommand<Execution> {

    private final MessageTranslator TRANSLATOR = MessageTranslator.createMessageTranslator(SchemaEditionCommand.class);

    private final Execution execution;
    private final Schema newSchema;
    private final Schema oldSchema;

    public SchemaEditionCommand(
            final Execution execution,
            final Schema oldSchema,
            final Schema schema) {
        this.execution = execution;
        this.newSchema = schema;
        this.oldSchema = oldSchema;
    }

    @Override
    public Execution getSubject() {
        return execution;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editAction.descr");
    }

    @Override
    public void execute() throws Exception {
        editSchema(newSchema, oldSchema);
    }

    @Override
    public void undo() throws Exception {
        editSchema(newSchema, oldSchema);
    }

    @Override
    public void redo() throws Exception {
        editSchema(newSchema, oldSchema);
    }

    private void editSchema(final Schema newSchema, final Schema oldSchema) {
        execution.removeSchema(oldSchema);
        execution.addSchema(newSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }
}