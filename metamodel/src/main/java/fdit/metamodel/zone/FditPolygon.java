package fdit.metamodel.zone;

import fdit.metamodel.coordinates.Coordinates;
import javafx.scene.shape.Polygon;

import java.util.Collection;
import java.util.UUID;

import static java.util.Collections.unmodifiableCollection;

public final class FditPolygon extends Zone {

    private final Collection<Coordinates> vertices;

    public FditPolygon(final String name,
                       final UUID id,
                       final Double altitudeLowerBound,
                       final Double altitudeUpperBound,
                       final Collection<Coordinates> vertices) {
        super(name, id, altitudeLowerBound, altitudeUpperBound);
        this.vertices = vertices;
        zoneShape = new Polygon();
        createShapeFromZone();
    }

    public Collection<Coordinates> getVertices() {
        return unmodifiableCollection(vertices);
    }

    public void setVertices(final Collection<Coordinates> vertices) {
        this.vertices.clear();
        this.vertices.addAll(vertices);
        createShapeFromZone();
    }

    public void addVertice(Coordinates vertice) {
        vertices.add(vertice);
        createShapeFromZone();
    }

    @Override
    protected void createShapeFromZone() {
        for (final Coordinates coordinates : vertices) {
            ((Polygon) zoneShape).getPoints().addAll(coordinates.getLongitude(), coordinates.getLatitude());
        }
    }
}
