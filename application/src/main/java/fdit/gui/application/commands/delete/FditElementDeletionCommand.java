package fdit.gui.application.commands.delete;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.tools.i18n.MessageTranslator;

import java.io.File;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.FditManagerUtils.getFditElementFile;
import static fdit.gui.application.commands.FditElementCommand.OperationType.DELETION;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static org.apache.commons.io.FileUtils.copyDirectoryToDirectory;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

public class FditElementDeletionCommand implements FditElementCommand<FditElement> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditElementDeletionCommand.class);

    private final FditElement deletedElement;
    private final Directory father;
    private final File tmpDirLocation;
    private final File deletedElementFile;
    private final File originalElementLocation;

    FditElementDeletionCommand(final FditElement deletedElement, final File tmpDirLocation) {
        this.deletedElement = deletedElement;
        this.tmpDirLocation = tmpDirLocation;
        father = deletedElement.getFather();
        deletedElementFile = getFditElementFile(deletedElement);
        originalElementLocation = deletedElementFile.getParentFile();
    }

    @Override
    public OperationType getOperationType() {
        return DELETION;
    }

    @Override
    public FditElement getSubject() {
        return deletedElement;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.delete.descr", deletedElement.getName());
    }

    @Override
    public void execute() throws Exception {
        if (deletedElement instanceof Directory) {
            copyDirectoryToDirectory(deletedElementFile, tmpDirLocation);
        } else {
            copyFileToDirectory(deletedElementFile, tmpDirLocation);
        }
        FDIT_MANAGER.removeFditElement(deletedElement);
    }

    @Override
    public void undo() throws Exception {
        final File fileToRestore = new File(tmpDirLocation, deletedElementFile.getName());
        if (deletedElement instanceof Directory) {
            copyDirectoryToDirectory(fileToRestore, originalElementLocation);
        } else {
            copyFileToDirectory(fileToRestore, originalElementLocation);
        }
        FDIT_MANAGER.addFditElement(deletedElement, father);
    }

    @Override
    public void redo() throws Exception {
        FDIT_MANAGER.removeFditElement(deletedElement);
    }
}