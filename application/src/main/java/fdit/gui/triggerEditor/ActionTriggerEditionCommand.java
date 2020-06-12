package fdit.gui.triggerEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;
import org.fxmisc.richtext.model.PlainTextChange;

import java.util.Optional;

import static fdit.gui.application.commands.FditElementCommand.OperationType.TMP_EDITION;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ActionTriggerEditionCommand implements FditElementCommand<ActionTrigger> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ActionTriggerEditionCommand.class);

    private final String initialContent;
    private final ActionTrigger trigger;
    private PlainTextChange contentChange;

    ActionTriggerEditionCommand(final ActionTrigger trigger,
                                final PlainTextChange contentChange) {
        this.trigger = trigger;
        initialContent = trigger.getContent();
        this.contentChange = contentChange;
    }

    PlainTextChange getContentChange() {
        return contentChange;
    }

    boolean mergeTextChangeWith(final PlainTextChange otherTextChange) {
        final Optional<PlainTextChange> merge = contentChange.mergeWith(otherTextChange);
        merge.ifPresent(mergedTextChange -> {
            contentChange = mergedTextChange;
            trigger.setContent(computeNewContent(contentChange));
        });
        return merge.isPresent();
    }

    @Override
    public ActionTrigger getSubject() {
        return trigger;
    }

    @Override
    public OperationType getOperationType() {
        return TMP_EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editTrigger.content", trigger.getName());
    }

    @Override
    public void execute() throws Exception {
        trigger.setContent(computeNewContent(contentChange));
    }

    @Override
    public void undo() throws Exception {
        trigger.setContent(initialContent);
    }

    @Override
    public void redo() throws Exception {
        trigger.setContent(computeNewContent(contentChange));
    }

    private String computeNewContent(final PlainTextChange contentChange) {
        final StringBuilder newContentBuilder = new StringBuilder(initialContent);
        newContentBuilder.replace(contentChange.getPosition(),
                contentChange.getPosition() + contentChange.getRemoved().length(),
                contentChange.getInserted());
        return newContentBuilder.toString();
    }
}
