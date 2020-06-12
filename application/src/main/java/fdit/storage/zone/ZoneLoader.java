package fdit.storage.zone;

import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.metamodel.zone.ZoneUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.tools.jdom.Jdom2Utils.createFromInputStream;
import static fdit.tools.stream.StreamUtils.tryFind;
import static java.lang.Double.parseDouble;

public final class ZoneLoader {

    private ZoneLoader() {
    }

    public static Zone loadZone(final File zoneFile,
                                final Iterable<Zone> existingZones) throws Exception {
        try (final InputStream zoneInputStream = new FileInputStream(zoneFile)) {
            final Document zoneDocument = createFromInputStream(zoneInputStream);
            final Element zoneElement = zoneDocument.getRootElement();
            return createZone(zoneFile, zoneElement, existingZones);
        }
    }

    private static Zone createZone(final File zoneFile,
                                   final Element zoneElement,
                                   final Iterable<Zone> existingZones) {
        final UUID id = UUID.fromString(zoneElement.getChild(ZoneStorage.IDENT).getText());
        final Optional<Zone> alreadyExistingZone = tryFind(existingZones, ZoneUtils.withZoneId(id));
        if (alreadyExistingZone.isPresent()) {
            return alreadyExistingZone.get();
        }
        final Double lowerAltitude = parseDouble(zoneElement.getChild(ZoneStorage.LOWER_ALTITUDE).getText());
        final Double upperAltitude = parseDouble(zoneElement.getChild(ZoneStorage.UPPER_ALTITUDE).getText());

        final Element polygonElement = zoneElement.getChild(ZoneStorage.POLYGON);
        if (polygonElement != null) {
            final Collection<Coordinates> vertices = createVertices(polygonElement.getChild(ZoneStorage.VERTICES));
            return new FditPolygon(buildFditElementName(zoneFile), id, lowerAltitude, upperAltitude, vertices);
        }
        throw new RuntimeException("Unknown zone type");
    }

    private static Collection<Coordinates> createVertices(final Element verticesElement) {
        final Collection<Coordinates> vertices = newArrayList();
        verticesElement.getChildren().forEach(coordinate -> vertices.add(createCoordinate(coordinate)));
        return vertices;
    }

    private static Coordinates createCoordinate(final Element coordinateElement) {
        final double latitude = parseDouble(coordinateElement.getChild(ZoneStorage.LATITUDE).getText());
        final double longitude = parseDouble(coordinateElement.getChild(ZoneStorage.LONGITUDE).getText());
        return new Coordinates(latitude, longitude);
    }
}