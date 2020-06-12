package fdit.gui.utils.world;

import fdit.gui.utils.ThreadSafeObjectProperty;
import fdit.gui.zoneEditor.tabs.zone.PolygonEditionModel;
import fdit.gui.zoneEditor.tabs.zone.ZoneEditionModel;
import fdit.leafletmap.LatLong;
import fdit.leafletmap.LeafletMapView;
import fdit.leafletmap.Zone;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.input.MouseButton;

import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.getVertexLatitude;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.getVertexLongitude;

public class FditWorldZone implements Observable {


    private final List<InvalidationListener> rightClickListeners = newArrayList();
    private final ObjectProperty clickedVertex;
    private final HashMap<ZoneEditionModel, Zone> zoneShapes = newHashMap();
    private final LeafletMapView mapView;

    public FditWorldZone(LeafletMapView mapView) {
        this.mapView = mapView;
        clickedVertex = new ThreadSafeObjectProperty(null);
    }

    private static List<LatLong> collectPosition(final Iterable<DoubleProperty[]> vertices) {
        List<LatLong> positions = newArrayList();
        for (final DoubleProperty[] vertex : vertices) {
            getVertexLongitude(vertex);
            positions.add(new LatLong(getVertexLatitude(vertex), getVertexLongitude(vertex)));
        }
        return positions;
    }

    public void setMouseListener() {
        mapView.setOnMouseClicked(event -> {
            if (event.isStillSincePress()) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    clickedVertex.setValue(mapView.getMousePosition());
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    notifyListeners();
                }
            }
        });
    }

    public void removeZone(final ZoneEditionModel zoneEditionModel) {
        Zone currentZone = zoneShapes.get(zoneEditionModel);
        mapView.removeZone(currentZone);
        zoneShapes.remove(zoneEditionModel);
    }

    public void drawPolygon(final PolygonEditionModel polygonEditionModel) {
        List<LatLong> positions = collectPosition(polygonEditionModel.getVertices());
        Zone currentZone = zoneShapes.get(polygonEditionModel);
        if (currentZone != null) {
            currentZone.updatePoints(positions);
            currentZone.updateMap();
        } else {
            currentZone = new Zone(polygonEditionModel.getZoneIdAsString());
            zoneShapes.put(polygonEditionModel, currentZone);
            currentZone.addToMap(mapView);
            currentZone.updatePoints(positions);
            currentZone.updateMap();
        }
    }

    public ObjectProperty getClickedVertex() {
        return clickedVertex;
    }

    private void notifyListeners() {
        for (final InvalidationListener listener : rightClickListeners) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(final InvalidationListener listener) {
        rightClickListeners.add(listener);
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
        rightClickListeners.remove(listener);
    }
}