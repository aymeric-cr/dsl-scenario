package fdit.gui.schemaEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class SchemaSaveCommand implements FditElementCommand<Schema> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaSaveCommand.class);
    private final Schema schema;
    private final String oldContent;

    public SchemaSaveCommand(final Schema schema,
                             final String oldContent) {
        this.schema = schema;
        this.oldContent = oldContent;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.saveScenario.content", schema.getName());
    }

    @Override
    public Schema getSubject() {
        return schema;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public void execute() {
        save(schema, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void undo() throws Exception {
        final Schema oldSchema = new Schema(
                schema.getName(),
                schema.getDescription(),
                oldContent);
        oldSchema.setFather(schema.getFather());
        oldSchema.setRecording(schema.getRecording());
        save(oldSchema, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void redo() throws Exception {
        execute();
    }

    public String getOldContent() {
        return oldContent;
    }
}
