package fdit.storage.zone;

import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.tools.jdom.Jdom2Utils.*;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.String.valueOf;

public final class ZoneSaver {

    private ZoneSaver() {
    }

    public static void saveZone(final Zone zone, final File rootFile) throws Exception {
        final File zoneFile = getFditElementFile(zone, rootFile);
        try (final OutputStream zoneOutputStream = new FileOutputStream(zoneFile)) {
            writeDocument(new Document(createZoneElement(zone)), zoneOutputStream);
        }
    }

    private static Element createZoneElement(final Zone zone) {
        final Element idElement = createElement(ZoneStorage.IDENT);
        idElement.setText(zone.getId().toString());
        final Element lowerAltitudeElement = createLowerAltitudeElement(zone);
        final Element upperAltitudeElement = createUpperAltitudeElement(zone);
        final Element shapeElement = ((Zone.ZoneVisitor<Element>) ZoneSaver::createPolygonElement).accept(zone);
        return createElement(ZoneStorage.ROOT,
                children(idElement, lowerAltitudeElement, upperAltitudeElement, shapeElement));
    }

    public static Element createUpperAltitudeElement(final Zone zone) {
        final Element upperAltitudeElement = createElement(ZoneStorage.UPPER_ALTITUDE);
        upperAltitudeElement.setText(valueOf(zone.getAltitudeUpperBound()));
        return upperAltitudeElement;
    }

    public static Element createLowerAltitudeElement(final Zone zone) {
        final Element lowerAltitudeElement = createElement(ZoneStorage.LOWER_ALTITUDE);
        lowerAltitudeElement.setText(valueOf(zone.getAltitudeLowerBound()));
        return lowerAltitudeElement;
    }

    private static Element createPolygonElement(final FditPolygon polygon) {
        final Element verticeElement = createVerticesElement(polygon);
        return createElement(ZoneStorage.POLYGON, child(verticeElement));
    }

    public static Element createVerticesElement(final FditPolygon polygon) {
        return createElement(ZoneStorage.VERTICES, children(mapping(polygon.getVertices(),
                ZoneSaver::createVertexElement)));
    }

    public static Element createVertexElement(final Coordinates coordinates) {
        final Element latitudeElement = createElement(ZoneStorage.LATITUDE);
        latitudeElement.setText(valueOf(coordinates.getLatitude()));
        final Element longitudeElement = createElement(ZoneStorage.LONGITUDE);
        longitudeElement.setText(valueOf(coordinates.getLongitude()));
        return createElement(ZoneStorage.VERTEX, children(latitudeElement, longitudeElement));
    }
}