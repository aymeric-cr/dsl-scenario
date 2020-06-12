package fdit.gui.application.commands.load;

import fdit.history.Command;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;

import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class LoadAlterationTriggerCommand implements Command {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(LoadAlterationTriggerCommand.class);

    private final ActionTrigger trigger;

    public LoadAlterationTriggerCommand(ActionTrigger trigger) {
        this.trigger = trigger;
    }

    public ActionTrigger getTrigger() {
        return trigger;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.loadTrigger.descr", trigger.getName());
    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public void undo() throws Exception {

    }

    @Override
    public void redo() throws Exception {

    }
}