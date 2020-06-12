package fdit.gui.zoneEditor;

import fdit.gui.zoneEditor.tabs.zone.ZoneEditionModel;
import fdit.metamodel.zone.Zone;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;

public class ZoneEditorContext {

    private final ObservableList<ZoneEditionModel> zoneEditionModels = observableArrayList();
    private ZoneEditionModel selectedModel;

    public void addZoneEditionModel(final ZoneEditionModel zoneEditionModel) {
        zoneEditionModels.add(zoneEditionModel);
    }

    public void observeZoneModels(final Consumer<ZoneEditionModel> addedZoneModelConsumer,
                                  final Consumer<ZoneEditionModel> removedZoneModelConsumer) {
        zoneEditionModels.addListener((ListChangeListener<? super ZoneEditionModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (final ZoneEditionModel zoneEditionModel : change.getAddedSubList()) {
                        addedZoneModelConsumer.accept(zoneEditionModel);
                    }
                }
                if (change.wasRemoved()) {
                    for (final ZoneEditionModel zoneEditionModel : change.getRemoved()) {
                        removedZoneModelConsumer.accept(zoneEditionModel);
                    }
                }
            }
        });
    }

    public void removeZoneEditionModel(final ZoneEditionModel zoneEditionModel) {
        zoneEditionModels.remove(zoneEditionModel);
    }

    public Optional<ZoneEditionModel> findZoneEditionModel(final Zone zone) {
        for (final ZoneEditionModel zoneEditionModel : zoneEditionModels) {
            if (zoneEditionModel.getEditedZone() == zone) {
                return of(zoneEditionModel);
            }
        }
        return empty();
    }

    public ZoneEditionModel getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(final ZoneEditionModel selectedModel) {
        this.selectedModel = selectedModel;
    }
}
