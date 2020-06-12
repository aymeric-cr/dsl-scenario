package fdit.gui.schemaEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;
import org.fxmisc.richtext.model.PlainTextChange;

import java.util.Optional;

import static fdit.gui.application.commands.FditElementCommand.OperationType.TMP_EDITION;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

class SchemaEditionCommand implements FditElementCommand<Schema> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaEditionCommand.class);

    private final String initialContent;
    private final Schema schema;
    private PlainTextChange contentChange;

    SchemaEditionCommand(final Schema schema,
                         final PlainTextChange contentChange) {
        this.schema = schema;
        initialContent = schema.getContent();
        this.contentChange = contentChange;
    }

    PlainTextChange getContentChange() {
        return contentChange;
    }

    boolean mergeTextChangeWith(final PlainTextChange otherTextChange) {
        final Optional<PlainTextChange> merge = contentChange.mergeWith(otherTextChange);
        merge.ifPresent(mergedTextChange -> {
            contentChange = mergedTextChange;
            schema.setContent(computeNewContent(contentChange));
        });
        return merge.isPresent();
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editScenario.content", schema.getName());
    }

    @Override
    public Schema getSubject() {
        return schema;
    }

    @Override
    public OperationType getOperationType() {
        return TMP_EDITION;
    }

    @Override
    public void execute() throws Exception {
        schema.setContent(computeNewContent(contentChange));
    }

    @Override
    public void undo() throws Exception {
        schema.setContent(initialContent);
    }

    @Override
    public void redo() throws Exception {
        schema.setContent(computeNewContent(contentChange));
    }

    private String computeNewContent(final PlainTextChange contentChange) {
        final StringBuilder newContentBuilder = new StringBuilder(initialContent);
        newContentBuilder.replace(contentChange.getPosition(),
                contentChange.getPosition() + contentChange.getRemoved().length(),
                contentChange.getInserted());
        return newContentBuilder.toString();
    }
}