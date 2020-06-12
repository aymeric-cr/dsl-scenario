package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.FditTestCase;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.createVertex;
import static fdit.metamodel.FditElementHelper.*;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.containsAllXML;

public class ZoneCreationCommandTest extends FditTestCase {

    private static PolygonEditionModel PolygonEditionModel(final String id) {
        return new PolygonEditionModel(UUID.fromString(id));
    }

    @Test
    public void savePolygon() {
        final PolygonEditionModel polygonEditionModel = PolygonEditionModel("1-1-1-1-1");
        polygonEditionModel.setAltitudeLowerBound("1");
        polygonEditionModel.setAltitudeUpperBound("15");
        polygonEditionModel.setZoneName("MyPolygon");
        polygonEditionModel.addVertices(createVertex(1.0, 0.0),
                createVertex(2.0, 0.0),
                createVertex(3.0, 0.0),
                createVertex(4.0, 0.0));

        polygonEditionModel.save();

        assertThat(getRoot(), aRoot(aPolygon(withZoneName("MyPolygon"),
                withId(defaultIdForTest()),
                withLowerAltitude(1.0),
                withUpperAltitude(15.0),
                withVertices(aVertex(1.0, 0.0),
                        aVertex(2.0, 0.0),
                        aVertex(3.0, 0.0),
                        aVertex(4.0, 0.0)))));
        assertThat(findFile("MyPolygon.xml"),
                aFile("MyPolygon.xml",
                        containsAllXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>1.0</lowerAlt>\n" +
                                "  <upperAlt>15.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>1.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>2.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>3.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>4.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>")));

        getExecutor().undo();
        assertThat(getRoot(), aRoot());
        assertThat(findFile("MyPolygon.xml"), Objects::isNull);

        getExecutor().redo();
        assertThat(getRoot(), aRoot(aPolygon(withZoneName("MyPolygon"),
                withId(defaultIdForTest()),
                withLowerAltitude(1.0),
                withUpperAltitude(15.0),
                withVertices(aVertex(1.0, 0.0),
                        aVertex(2.0, 0.0),
                        aVertex(3.0, 0.0),
                        aVertex(4.0, 0.0)))));
        assertThat(findFile("MyPolygon.xml"),
                aFile("MyPolygon.xml",
                        containsAllXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<zone>\n" +
                                "  <id>00000001-0001-0001-0001-000000000001</id>\n" +
                                "  <lowerAlt>1.0</lowerAlt>\n" +
                                "  <upperAlt>15.0</upperAlt>\n" +
                                "  <polygon>\n" +
                                "    <vertices>\n" +
                                "      <vertex>\n" +
                                "        <lat>1.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>2.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>3.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "      <vertex>\n" +
                                "        <lat>4.0</lat>\n" +
                                "        <lon>0.0</lon>\n" +
                                "      </vertex>\n" +
                                "    </vertices>\n" +
                                "  </polygon>\n" +
                                "</zone>")));
    }
}