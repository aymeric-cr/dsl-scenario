package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;

import java.util.UUID;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ActionTriggerCreationCommand implements FditElementCommand<ActionTrigger> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ActionTriggerCreationCommand.class);

    private final Directory father;
    private final String name;
    private final UUID id;
    private final String content;
    private ActionTrigger createdTrigger;

    public ActionTriggerCreationCommand(final Directory father,
                                        final String name,
                                        final UUID id) {
        this.father = father;
        this.name = name;
        this.id = id;
        content = "";
    }

    @Override
    public ActionTrigger getSubject() {
        return createdTrigger;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createTrigger.descr");
    }

    @Override
    public void execute() throws Exception {
        createdTrigger = createDefaultAlterationTrigger(name, content, id);
        addAndSaveAlterationTrigger();
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(createdTrigger);
    }

    @Override
    public void redo() throws Exception {
        addAndSaveAlterationTrigger();
    }

    private void addAndSaveAlterationTrigger() {
        FDIT_MANAGER.addFditElement(createdTrigger, father);
        save(createdTrigger, FDIT_MANAGER.getRootFile());
    }

    private ActionTrigger createDefaultAlterationTrigger(final String name,
                                                         final String content,
                                                         final UUID id) {
        return new ActionTrigger(name, id, "", content);
    }
}