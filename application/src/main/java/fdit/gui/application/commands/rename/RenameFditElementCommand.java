package fdit.gui.application.commands.rename;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.FditElement;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class RenameFditElementCommand<T extends FditElement> implements FditElementCommand<T> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(RenameFditElementCommand.class);

    private final T element;
    private final String oldName;
    private final String newName;

    public RenameFditElementCommand(final T element, final String newName) {
        this.element = element;
        oldName = element.getName();
        this.newName = newName;
    }

    @Override
    public T getSubject() {
        return element;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.rename.descr");
    }

    @Override
    public void execute() throws Exception {
        rename(newName);
    }

    @Override
    public void undo() throws Exception {
        rename(oldName);
    }

    @Override
    public void redo() throws Exception {
        execute();
    }

    private void rename(final String newName) throws Exception {
        FDIT_MANAGER.renameFditElement(element, newName);
        save(element, FDIT_MANAGER.getRootFile());
    }
}
