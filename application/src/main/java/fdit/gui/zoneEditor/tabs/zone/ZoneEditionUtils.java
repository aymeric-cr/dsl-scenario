package fdit.gui.zoneEditor.tabs.zone;

import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.Double.valueOf;
import static java.lang.String.valueOf;

public final class ZoneEditionUtils {

    private ZoneEditionUtils() {
    }

    static Zone createPolygonFromModel(final PolygonEditionModel polygonEditionModel) {
        final Zone editedZone = polygonEditionModel.getEditedZone();
        final Double altitudeLower = valueOf(polygonEditionModel.getAltitudeLowerBound());
        final Double altitudeUpper = valueOf(polygonEditionModel.getAltitudeUpperBound());
        final Collection<Coordinates> vertices = convertToCoordinates(polygonEditionModel.getVertices());
        return new FditPolygon(polygonEditionModel.getZoneName(),
                editedZone.getId(), altitudeLower, altitudeUpper, vertices);
    }

    static void restoreSavedPolygonEditionModel(final PolygonEditionModel polygonEditionModel,
                                                final FditElement zone) {
        final FditPolygon polygon = (FditPolygon) zone;
        polygonEditionModel.setAltitudeLowerBound(valueOf(polygon.getAltitudeLowerBound()));
        polygonEditionModel.setAltitudeUpperBound(valueOf(polygon.getAltitudeUpperBound()));
        polygonEditionModel.setZoneName(zone.getName());
        polygonEditionModel.clearVertices();
        polygonEditionModel.addVertices(mapping(polygon.getVertices(),
                vertex -> createVertex(vertex.getLatitude(), vertex.getLongitude())));
    }

    static DoubleProperty[] createVertex(final double latitude, final double longitude) {
        return new DoubleProperty[]{new SimpleDoubleProperty(latitude), new SimpleDoubleProperty(longitude)};
    }

    public static double getVertexLatitude(final DoubleProperty[] vertex) {
        return vertex[0].get();
    }

    public static double getVertexLongitude(final DoubleProperty[] vertex) {
        return vertex[1].get();
    }

    static void setVertexLatitude(final DoubleProperty[] vertex, final double latitude) {
        vertex[0].set(latitude);
    }

    static void setVertexLongitude(final DoubleProperty[] vertex, final double longitude) {
        vertex[1].set(longitude);
    }

    static Coordinates toCoordinates(final DoubleProperty[] vertex) {
        return new Coordinates(getVertexLatitude(vertex), getVertexLongitude(vertex));
    }

    public static UUID generateZoneId() {
        return randomUUID();
    }

    private static Collection<Coordinates> convertToCoordinates(final Iterable<DoubleProperty[]> vertices) {
        final Collection<Coordinates> coordinates = newArrayList();
        for (final DoubleProperty[] vertex : vertices) {
            coordinates.add(toCoordinates(vertex));
        }
        return coordinates;
    }
}