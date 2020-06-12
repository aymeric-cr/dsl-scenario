package fdit.gui.executionEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static com.google.common.collect.Iterables.indexOf;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class SchemaDeletionCommand implements FditElementCommand<Execution> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaDeletionCommand.class);

    private final Execution execution;
    private final Schema deletedSchema;
    private final int deletedSchemaIndex;

    public SchemaDeletionCommand(final Execution execution,
                                 final Schema deletedSchema) {
        this.execution = execution;
        this.deletedSchema = deletedSchema;
        deletedSchemaIndex = indexOf(execution.getSchemas(), schema -> schema == deletedSchema);
    }

    int getDeletedSchemaIndex() {
        return deletedSchemaIndex;
    }

    Schema getDeletedSchema() {
        return deletedSchema;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public Execution getSubject() {
        return execution;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.deleteSchema.descr");
    }

    @Override
    public void execute() throws Exception {
        execution.removeSchema(deletedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void undo() throws Exception {
        execution.addSchema(deletedSchemaIndex, deletedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void redo() throws Exception {
        execution.removeSchema(deletedSchema);
        save(execution, FDIT_MANAGER.getRootFile());
    }
}