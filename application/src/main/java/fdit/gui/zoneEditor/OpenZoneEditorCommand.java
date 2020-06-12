package fdit.gui.zoneEditor;

import fdit.history.Command;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.zoneEditor.OpenZoneEditorCommand.OpenZoneType.EXISTING_ZONE;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class OpenZoneEditorCommand implements Command {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(OpenZoneEditorCommand.class);
    private final OpenZoneType openZoneType;
    private final Zone zoneToOpen;

    public OpenZoneEditorCommand(final OpenZoneType openZoneType) {
        this.openZoneType = openZoneType;
        zoneToOpen = null;
    }

    public OpenZoneEditorCommand(final Zone zoneToOpen) {
        openZoneType = EXISTING_ZONE;
        this.zoneToOpen = zoneToOpen;
    }

    public OpenZoneType getOpenZoneType() {
        return openZoneType;
    }

    public Zone getZoneToOpen() {
        return zoneToOpen;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createZone.descr");
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

    public enum OpenZoneType {EXISTING_ZONE, ALL_ZONES}
}
