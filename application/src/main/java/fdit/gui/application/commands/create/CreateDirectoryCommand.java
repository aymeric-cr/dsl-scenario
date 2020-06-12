package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.tools.i18n.MessageTranslator;

import java.io.File;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.FditManagerUtils.getFditElementFile;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static org.apache.commons.io.FileUtils.forceMkdir;

public class CreateDirectoryCommand implements FditElementCommand<Directory> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(CreateDirectoryCommand.class);

    private final Directory father;
    private final String name;
    private Directory created;

    public CreateDirectoryCommand(final Directory father, final String name) {
        this.father = father;
        this.name = name;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public Directory getSubject() {
        return created;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createDir.descr", name);
    }

    @Override
    public void execute() throws Exception {
        final File newDir = new File(getFditElementFile(father), name);
        forceMkdir(newDir);
        created = new Directory(name);
        FDIT_MANAGER.addFditElement(created, father);
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(created);
    }

    @Override
    public void redo() throws Exception {
        final File newDir = new File(getFditElementFile(father), name);
        forceMkdir(newDir);
        FDIT_MANAGER.addFditElement(created, father);
    }
}