package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Root;
import fdit.metamodel.zone.Zone;
import org.junit.Test;

import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.*;
import static fdit.metamodel.FditElementHelper.*;
import static fdit.metamodel.element.DirectoryUtils.findZone;
import static fdit.metamodel.element.DirectoryUtils.gatherAllZones;
import static fdit.metamodel.zone.ZoneHelper.*;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.containsAllXML;

public class ZoneEditionCommandTest extends FditTestCase {

    @Test
    public void testEdit_polygon() {
        final Root root = root(
                polygon("MyPolygon",
                        defaultIdForTest(),
                        lowerAltitude(15.0),
                        upperAltitude(35.0),
                        vertices(
                                coordinates(48.0, 2.0),
                                coordinates(50.0, 2.0),
                                coordinates(50.0, 3.0),
                                coordinates(48.0, 3.0))));
        final Zone zone = findZone("MyPolygon", root).get();
        final PolygonEditionModel polygonEditionModel = new PolygonEditionModel(defaultIdForTest());
        polygonEditionModel.setEditedZone(zone);
        restoreSavedPolygonEditionModel(polygonEditionModel, zone);

        assertThat(gatherAllZones(getRoot()), containsOnly(
                aPolygon(withZoneName("MyPolygon"),
                        withLowerAltitude(15.0),
                        withUpperAltitude(35.0),
                        withVertices(aVertex(48.0, 2.0),
                                aVertex(50.0, 2.0),
                                aVertex(50.0, 3.0),
                                aVertex(48.0, 3.0)))));
        assertThat(findFile("MyPolygon.xml"),
                aFile("MyPolygon.xml", containsAllXML(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>15.0</lowerAlt>\n" +
                                "  <upperAlt>35.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>\n")));

        polygonEditionModel.setZoneName("MonPolygon");
        polygonEditionModel.setAltitudeLowerBound("10.0");
        setVertexLatitude(polygonEditionModel.getVertices().get(2), 10.0);
        getExecutor().execute(new ZoneEditionCommand(polygonEditionModel.getEditedZone(),
                createPolygonFromModel(polygonEditionModel)));

        assertThat(gatherAllZones(getRoot()), containsOnly(
                aPolygon(withZoneName("MonPolygon"),
                        withLowerAltitude(10.0),
                        withUpperAltitude(35.0),
                        withVertices(aVertex(48.0, 2.0),
                                aVertex(50.0, 2.0),
                                aVertex(10.0, 3.0),
                                aVertex(48.0, 3.0)))));
        assertThat(findFile("MonPolygon.xml"),
                aFile("MonPolygon.xml", containsAllXML(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>10.0</lowerAlt>\n" +
                                "  <upperAlt>35.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>10.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>")));

        undo();
        assertThat(gatherAllZones(getRoot()), containsOnly(
                aPolygon(withZoneName("MyPolygon"),
                        withLowerAltitude(15.0),
                        withUpperAltitude(35.0),
                        withVertices(aVertex(48.0, 2.0),
                                aVertex(50.0, 2.0),
                                aVertex(50.0, 3.0),
                                aVertex(48.0, 3.0)))));
        assertThat(findFile("MyPolygon.xml"),
                aFile("MyPolygon.xml", containsAllXML(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>15.0</lowerAlt>\n" +
                                "  <upperAlt>35.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>\n")));

        redo();
        assertThat(gatherAllZones(getRoot()), containsOnly(
                aPolygon(withZoneName("MonPolygon"),
                        withLowerAltitude(10.0),
                        withUpperAltitude(35.0),
                        withVertices(aVertex(48.0, 2.0),
                                aVertex(50.0, 2.0),
                                aVertex(10.0, 3.0),
                                aVertex(48.0, 3.0)))));
        assertThat(findFile("MonPolygon.xml"),
                aFile("MonPolygon.xml", containsAllXML(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>10.0</lowerAlt>\n" +
                                "  <upperAlt>35.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>50.0</lat>\n" +
                                "        <lon>2.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>10.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>48.0</lat>\n" +
                                "        <lon>3.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>")));
    }
}