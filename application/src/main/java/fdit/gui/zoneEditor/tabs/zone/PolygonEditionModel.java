package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.commands.create.ZoneCreationCommand;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.zone.FditPolygon;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Iterables.getLast;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.utils.FXUtils.startRunnableInBackground;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.*;
import static fdit.metamodel.coordinates.CoordinatesUtils.isValidLatitude;
import static fdit.metamodel.coordinates.CoordinatesUtils.isValidLongitude;
import static fdit.tools.collection.CollectionUtils.sameSequence;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class PolygonEditionModel extends ZoneEditionModel {

    private final ObservableList<DoubleProperty[]> vertices = observableArrayList();

    public PolygonEditionModel(final UUID zoneId) {
        super(zoneId);
        listenerHandles.add(createAttached(vertices,
                (ListChangeListener<DoubleProperty[]>) change -> updateOkButton()));
        updateButtons();
    }

    @Override
    protected void save() {
        if (validate()) {
            startRunnableInBackground(() -> {
                if (editedZone == null) {
                    FDIT_MANAGER.getCommandExecutor().executePreCommand(
                            new ZoneCreationCommand(FDIT_MANAGER.getRoot(), zoneName.get(), zoneId));
                }
                FDIT_MANAGER.getCommandExecutor().execute(
                        new ZoneEditionCommand(editedZone, createPolygonFromModel(this)));
            });
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean areVerticesDifferents(final Collection<Coordinates> vertices) {
        if (this.vertices.size() != vertices.size()) {
            return true;
        }
        if (vertices.isEmpty()) {
            return false;
        }
        return !sameSequence(vertices, mapping(this.vertices, ZoneEditionUtils::toCoordinates));
    }

    public boolean areValidVertices() {
        for (final DoubleProperty[] vertex : vertices) {
            if (!isValidLatitude(getVertexLatitude(vertex)) || !isValidLongitude(getVertexLongitude(vertex))) {
                return false;
            }
        }
        return true;
    }

    public ObservableList<DoubleProperty[]> getVertices() {
        return vertices;
    }

    void removeVertex(final DoubleProperty[] vertex) {
        vertices.remove(vertex);
    }

    void clearVertices() {
        vertices.clear();
    }

    void addVertices(final Iterable<DoubleProperty[]> coordinates) {
        coordinates.forEach(this::addVertex);
    }

    void addVertices(final DoubleProperty[]... coordinates) {
        addVertices(asList(coordinates));
    }

    public void addVertex(final DoubleProperty[] coordinates) {
        vertices.add(coordinates);
        listenerHandles.add(createAttached(coordinates[0], (observable, oldValue, newValue) -> {
            updateButtons();
            fireZoneUpdated();
        }));
        listenerHandles.add(createAttached(coordinates[1], (observable, oldValue, newValue) -> {
            updateButtons();
            fireZoneUpdated();
        }));
    }

    DoubleProperty[] addNewVertex() {
        final DoubleProperty[] newVertex;
        if (vertices.isEmpty()) {
            newVertex = createVertex(0, 0);
        } else {
            final DoubleProperty[] last = getLast(vertices);
            newVertex = createVertex(last[0].get(), last[1].get());
        }
        addVertex(newVertex);
        return newVertex;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean hasDataChanged() {
        if (super.hasDataChanged()) {
            return true;
        }
        final FditPolygon polygon = (FditPolygon) editedZone;
        if (polygon.getAltitudeLowerBound() != parseDouble(getAltitudeLowerBound())) {
            return true;
        }
        if (polygon.getAltitudeUpperBound() != parseDouble(getAltitudeUpperBound())) {
            return true;
        }
        if (areVerticesDifferents(polygon.getVertices())) {
            return true;
        }
        return false;
    }

    @Override
    protected void restoreModel() {
        if (editedZone != null) {
            restoreSavedPolygonEditionModel(this, editedZone);
        }
    }

    @Override
    public boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (vertices.size() < 3) {
            errorMessage.setValue(TRANSLATOR.getMessage("error.vertices.incorrectNumber"));
            return false;
        }
        if (!areValidVertices()) {
            errorMessage.setValue(TRANSLATOR.getMessage("error.invalidVertex"));
            return false;
        }
        errorMessage.setValue("");
        return true;
    }
}
