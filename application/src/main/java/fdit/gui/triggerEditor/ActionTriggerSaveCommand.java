package fdit.gui.triggerEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ActionTriggerSaveCommand implements FditElementCommand<ActionTrigger> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ActionTriggerSaveCommand.class);
    private final ActionTrigger trigger;
    private final String oldContent;

    ActionTriggerSaveCommand(final ActionTrigger trigger,
                             final String oldContent) {
        this.trigger = trigger;
        this.oldContent = oldContent;
    }

    @Override
    public ActionTrigger getSubject() {
        return trigger;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.saveTrigger.content", trigger.getName());
    }

    @Override
    public void execute() throws Exception {
        save(trigger, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void undo() throws Exception {
        final ActionTrigger oldTrigger = new ActionTrigger(
                trigger.getName(),
                trigger.getId(),
                trigger.getDescription(),
                oldContent);
        oldTrigger.setFather(trigger.getFather());
        save(oldTrigger, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void redo() throws Exception {
        execute();
    }

    public String getOldContent() {
        return oldContent;
    }
}