package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ZoneEditionCommand implements FditElementCommand<Zone> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneEditionCommand.class);
    private final Zone originalZone;
    private final Zone afterEditionZone;
    private Zone editedZone;

    ZoneEditionCommand(final Zone originalZone, final Zone afterEditionZone) {
        this.originalZone = originalZone;
        this.afterEditionZone = afterEditionZone;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public Zone getSubject() {
        return editedZone;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editZone.descr", originalZone.getName());
    }

    @Override
    public void execute() throws Exception {
        editZone(originalZone, afterEditionZone);
    }

    @Override
    public void undo() throws Exception {
        editZone(afterEditionZone, originalZone);
    }

    @Override
    public void redo() throws Exception {
        editZone(originalZone, afterEditionZone);
    }

    private void editZone(final Zone oldZone, final Zone newZone) throws Exception {
        editedZone = newZone;
        final Directory father = oldZone.getFather();
        FDIT_MANAGER.removeFditElement(oldZone);
        FDIT_MANAGER.addFditElement(newZone, father);
        save(editedZone, FDIT_MANAGER.getRootFile());
    }
}
