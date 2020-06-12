package fdit.gui.zoneEditor.world;

import fdit.gui.utils.ThreadSafeDoubleProperty;
import fdit.gui.utils.world.FditWorldZone;
import fdit.gui.zoneEditor.ZoneEditorContext;
import fdit.gui.zoneEditor.tabs.zone.PolygonEditionModel;
import fdit.gui.zoneEditor.tabs.zone.ZoneEditionModel;
import fdit.gui.zoneEditor.tabs.zone.ZoneEditionModel.ZoneEditionModelListener;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import fdit.leafletmap.LatLong;
import fdit.leafletmap.LeafletMapView;
import fdit.leafletmap.MapConfig;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Maps.newHashMap;

public class FditWorldZoneController implements Initializable {

    private final ZoneEditorContext contextZone;
    private final Map<ZoneEditionModel, ZoneEditionModelListener> zoneEditionModelListeners = newHashMap();
    @FXML
    private StackPane worldStackPane;
    private FditWorldZone world;
    private LeafletMapView mapView;
    private CompletableFuture<Worker.State> completeFutureMap;

    public FditWorldZoneController(final ZoneEditorContext conextZone) {
        contextZone = conextZone;
    }

    public FditWorldZone getWorld() {
        return world;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        mapView = new LeafletMapView();
        completeFutureMap = mapView.displayMap(new MapConfig());
        world = new FditWorldZone(mapView);
        world.getClickedVertex().addListener((observable, oldValue, newValue) -> {
            final LatLong latlongValues = (LatLong) newValue;
            final DoubleProperty[] doubleProperties = new DoubleProperty[2];
            doubleProperties[0] = new ThreadSafeDoubleProperty(latlongValues.getLatitude());
            doubleProperties[1] = new ThreadSafeDoubleProperty(latlongValues.getLongitude());
            addClickedVertexToSelectedModel(doubleProperties);
        });
        world.addListener(observable -> {
            final ZoneEditionModel model = contextZone.getSelectedModel();
            if (model instanceof PolygonEditionModel) {
                if (!((PolygonEditionModel) model).getVertices().isEmpty()) {
                    ((PolygonEditionModel) model).getVertices().remove(((PolygonEditionModel) model).getVertices().size() - 1);
                }
            }
        });
        contextZone.observeZoneModels(this::addZoneEditionModelListeners,
                this::removeZoneEditionModelListeners);
        Platform.runLater(() -> {
            completeFutureMap.whenComplete((workerState, ignore) -> {
                worldStackPane.getChildren().add(mapView);
                mapView.setEventMousePosition();
                world.setMouseListener();
            });
        });
    }

    private void addClickedVertexToSelectedModel(final DoubleProperty[] vertex) {
        final ZoneEditionModel editionModel = contextZone.getSelectedModel();
        if (editionModel != null) {
            if (editionModel instanceof PolygonEditionModel) {
                ((PolygonEditionModel) editionModel).addVertex(vertex);
            }
        }
    }

    private void addZoneEditionModelListeners(final ZoneEditionModel zoneEditionModel) {
        final ZoneEditionModelListener zoneEditionModelListener = () -> refreshZoneOnMap(zoneEditionModel);
        zoneEditionModel.addListener(zoneEditionModelListener);
        zoneEditionModelListeners.put(zoneEditionModel, zoneEditionModelListener);

        if (zoneEditionModel instanceof PolygonEditionModel) {
            completeFutureMap.whenComplete((workerState, ignore) -> {
                world.drawPolygon((PolygonEditionModel) zoneEditionModel);
                ((PolygonEditionModel) zoneEditionModel).getVertices().addListener((ListChangeListener<? super DoubleProperty[]>) observable -> {
                    try {
                        world.drawPolygon((PolygonEditionModel) zoneEditionModel);
                    } catch (IllegalStateException ignore1) {
                    }
                });
            });
        }
    }

    private void removeZoneEditionModelListeners(final ZoneEditionModel zoneEditionModel) {
        final ZoneEditionModelListener zoneEditionModelListener = zoneEditionModelListeners.remove(zoneEditionModel);
        zoneEditionModel.removeListener(zoneEditionModelListener);
        world.removeZone(zoneEditionModel);
    }

    private void refreshZoneOnMap(final ZoneEditionModel zoneEditionModel) {
        if (zoneEditionModel instanceof PolygonEditionModel) {
            if (((PolygonEditionModel) zoneEditionModel).areValidVertices()) {
                completeFutureMap.whenComplete((workerState, ignore) -> {
                    world.drawPolygon((PolygonEditionModel) zoneEditionModel);
                });
            }
        }
    }
}