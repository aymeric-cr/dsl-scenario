package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.zoneEditor.ZoneEditorContext;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.utils.FXUtils.loadFxml;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.createVertex;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.generateZoneId;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.String.valueOf;

public class PolygonTabCreator implements ZoneViewCreator {

    private final TabPane tabPane;
    private final Map<PolygonEditionModel, Tab> tabModelsMapping = newHashMap();
    private final ZoneEditorContext contextZone;

    public PolygonTabCreator(final TabPane tabPane, final ZoneEditorContext contextZone) {
        this.tabPane = tabPane;
        this.contextZone = contextZone;
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            for (final Entry<PolygonEditionModel, Tab> entry : tabModelsMapping.entrySet()) {
                if (entry.getValue().equals(newValue)) {
                    contextZone.setSelectedModel(entry.getKey());
                }
            }
        });
    }

    private static PolygonEditionModel createPolygonEditionModel(final FditPolygon polygon) {
        final PolygonEditionModel polygonEditionModel = new PolygonEditionModel(polygon.getId());
        polygonEditionModel.setZoneName(polygon.getName());
        polygonEditionModel.setAltitudeLowerBound(valueOf(polygon.getAltitudeLowerBound()));
        polygonEditionModel.setAltitudeUpperBound(valueOf(polygon.getAltitudeUpperBound()));
        polygonEditionModel.addVertices(mapping(polygon.getVertices(), vertex ->
                createVertex(vertex.getLatitude(), vertex.getLongitude())));
        polygonEditionModel.setEditedZone(polygon);
        return polygonEditionModel;
    }

    @Override
    public void showCreateZoneEditionView() {
        final PolygonEditionModel zoneEditionModel = new PolygonEditionModel(generateZoneId());
        createPolygonEditionTab(zoneEditionModel);
        contextZone.addZoneEditionModel(zoneEditionModel);
    }

    @Override
    public void showEditZoneEditionView(final Zone zone) {
        final Optional<ZoneEditionModel> zoneEditionModelOptional = contextZone.findZoneEditionModel(zone);
        if (zoneEditionModelOptional.isPresent()) {
            tabPane.getSelectionModel().select(tabModelsMapping.get(zoneEditionModelOptional.get()));
            return;
        }
        final PolygonEditionModel zoneEditionModel = createPolygonEditionModel((FditPolygon) zone);
        createPolygonEditionTab(zoneEditionModel);
        contextZone.addZoneEditionModel(zoneEditionModel);
    }

    private void createPolygonEditionTab(final PolygonEditionModel polygonEditionModel) {
        try {
            final Tab zoneEditionTab = new Tab();
            final PolygonEditionTabController controller =
                    new PolygonEditionTabController(zoneEditionTab, polygonEditionModel);
            final Node contentPane = loadFxml(getClass().getResource("zoneCreationTab.fxml"), controller);
            zoneEditionTab.setOnCloseRequest(event -> {
                contextZone.removeZoneEditionModel(polygonEditionModel);
                tabModelsMapping.remove(polygonEditionModel);
                controller.onClose();
            });
            tabPane.getTabs().add(zoneEditionTab);
            tabPane.getSelectionModel().select(zoneEditionTab);
            zoneEditionTab.setContent(contentPane);
            tabModelsMapping.put(polygonEditionModel, zoneEditionTab);
            contextZone.setSelectedModel(polygonEditionModel);
        } catch (final IOException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}