package fdit.storage.zone;

import fdit.gui.application.FditTestCase;
import org.junit.Test;

import java.io.File;

import static fdit.metamodel.FditElementHelper.*;
import static fdit.metamodel.zone.ZoneHelper.*;
import static fdit.storage.zone.ZoneLoader.loadZone;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.containsAllXML;
import static java.util.Collections.EMPTY_LIST;

public class ZoneStorageTest extends FditTestCase {

    @Test
    public void loadPolygon() throws Exception {
        root(polygon("My Zone",
                defaultIdForTest(),
                lowerAltitude(15.0),
                upperAltitude(35.0),
                vertices(
                        coordinates(48.0, 2.0),
                        coordinates(50.0, 2.0),
                        coordinates(48.0, 3.0))));
        final File zoneFile = findFile("My Zone.xml");
        assertThat(zoneFile,
                aFile("My Zone.xml",
                        containsAllXML(
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
                                        "        <lat>48.0</lat>\n" +
                                        "        <lon>3.0</lon>\n" +
                                        "      </vertex>\n" +
                                        "    </vertices>\n" +
                                        "  </polygon>\n" +
                                        "</zone>\n")));

        assertThat(loadZone(zoneFile, EMPTY_LIST),
                aPolygon(withZoneName("My Zone"),
                        withId(defaultIdForTest()),
                        withLowerAltitude(15.0),
                        withUpperAltitude(35.0),
                        withVertices(aVertex(48.0, 2.0),
                                aVertex(50.0, 2.0),
                                aVertex(48.0, 3.0))));
    }
}