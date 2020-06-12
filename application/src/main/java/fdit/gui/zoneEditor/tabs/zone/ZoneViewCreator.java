package fdit.gui.zoneEditor.tabs.zone;

import fdit.metamodel.zone.Zone;

public interface ZoneViewCreator {

    void showCreateZoneEditionView();

    void showEditZoneEditionView(Zone zone);
}