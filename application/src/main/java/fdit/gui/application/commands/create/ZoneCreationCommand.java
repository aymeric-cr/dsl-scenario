package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.element.Directory;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;

import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ZoneCreationCommand implements FditElementCommand<Zone> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneCreationCommand.class);

    private final Directory father;
    private final String name;
    private final UUID zoneId;
    private Zone createdZone;

    public ZoneCreationCommand(final Directory father,
                               final String name,
                               final UUID zoneId) {
        this.father = father;
        this.name = name;
        this.zoneId = zoneId;
    }

    private static Zone createDefaultPolygon(final String name,
                                             final UUID zoneId) {
        final Double altitudeLower = 0.0;
        final Double altitudeUpper = 0.0;
        final Collection<Coordinates> vertices = newArrayList();
        return new FditPolygon(name, zoneId, altitudeLower, altitudeUpper, vertices);
    }

    public Zone getCreatedZone() {
        return createdZone;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public Zone getSubject() {
        return createdZone;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createZone.descr");
    }

    @Override
    public void execute() throws Exception {
        createdZone = createDefaultPolygon(name, zoneId);
        FDIT_MANAGER.addFditElement(createdZone, father);
        save(createdZone, FDIT_MANAGER.getRootFile());
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(createdZone);
    }

    @Override
    public void redo() throws Exception {
        FDIT_MANAGER.addFditElement(createdZone, father);
        save(createdZone, FDIT_MANAGER.getRootFile());
    }
}