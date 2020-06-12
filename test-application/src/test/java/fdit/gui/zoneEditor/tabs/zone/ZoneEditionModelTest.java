package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.FditElementHelper;
import fdit.tools.functional.ThrowableConsumer;
import javafx.beans.property.DoubleProperty;
import org.junit.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionHelper.*;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.*;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZoneEditionModelTest extends FditTestCase {

    private static PolygonEditionModel polygonEditionModel(final UUID uuid) {
        return new PolygonEditionModel(uuid);
    }

    private static PolygonEditionModel polygonEditionModel(final String name,
                                                           final Consumer<? super ZoneEditionModel> altitudes,
                                                           final ThrowableConsumer<PolygonEditionModel> vertices) {
        final PolygonEditionModel polygonEditionModel = new PolygonEditionModel(generateZoneId());
        polygonEditionModel.setZoneName(name);
        altitudes.accept(polygonEditionModel);
        acceptAll(polygonEditionModel, vertices);
        return polygonEditionModel;
    }

    private static Consumer<? super ZoneEditionModel> withAltitudes(final String lowerAltitude,
                                                                    final String upperAltitude) {
        return zoneEditionModel -> {
            zoneEditionModel.setAltitudeLowerBound(lowerAltitude);
            zoneEditionModel.setAltitudeUpperBound(upperAltitude);
        };
    }

    private static ThrowableConsumer<PolygonEditionModel> withVertices(final DoubleProperty[]... vertices) {
        return polygonEditionModel -> polygonEditionModel.addVertices(vertices);
    }

    @Test
    public void validatePolygonEntries() {
        final PolygonEditionModel polygonEditionModel = polygonEditionModel(FditElementHelper.defaultIdForTest());
        assertFalse(polygonEditionModel.validate());

        final DoubleProperty[] p1 = createVertex(0, 0);
        final DoubleProperty[] p2 = createVertex(0, 0);
        final DoubleProperty[] p3 = createVertex(0, 0);
        polygonEditionModel.addVertices(p1, p2, p3);
        assertTrue(polygonEditionModel.validate());
        setVertexLatitude(p1, 90.0);
        setVertexLongitude(p2, 180.0);
        assertTrue(polygonEditionModel.validate());
        setVertexLatitude(p3, 90.1);
        assertFalse(polygonEditionModel.validate());
        setVertexLatitude(p3, -90.0);
        setVertexLongitude(p1, -180.0);
        assertTrue(polygonEditionModel.validate());

        polygonEditionModel.setAltitudeLowerBound("-1");
        assertFalse(polygonEditionModel.validate());
        polygonEditionModel.setAltitudeLowerBound("10");
        assertFalse(polygonEditionModel.validate());
        polygonEditionModel.setAltitudeUpperBound("15");
        assertTrue(polygonEditionModel.validate());
        polygonEditionModel.setAltitudeLowerBound("20");
        assertFalse(polygonEditionModel.validate());
        polygonEditionModel.setAltitudeLowerBound("0");
        assertTrue(polygonEditionModel.validate());

        polygonEditionModel.setZoneName("    ");
        assertFalse(polygonEditionModel.validate());
        polygonEditionModel.setAltitudeLowerBound("10");
        assertFalse(polygonEditionModel.validate());
        polygonEditionModel.setZoneName("My New name");
        assertTrue(polygonEditionModel.validate());

    }

    @Test
    public void testFirstModelPolygonSaving_undoRedo() {
        final PolygonEditionModel polygonEditionModel = polygonEditionModel(FditElementHelper.defaultIdForTest());
        polygonEditionModel.setZoneName("MaZone");
        final DoubleProperty[] p1 = createVertex(1, 6);
        final DoubleProperty[] p2 = createVertex(2, 5);
        final DoubleProperty[] p3 = createVertex(3, 4);
        polygonEditionModel.addVertices(p1, p2, p3);
        polygonEditionModel.setAltitudeLowerBound("0");
        polygonEditionModel.setAltitudeUpperBound("20");
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertFalse(polygonEditionModel.isDeleteButtonEnabled());

        polygonEditionModel.save();
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());
        assertThat(polygonEditionModel, aPolygonEditionModel(
                aName("MaZone"),
                anAltitudeLowerBound(0),
                anAltitudeUpperBound(20.0),
                aVertices(aVertex(1.0, 6.0),
                        aVertex(2.0, 5.0),
                        aVertex(3.0, 4.0))));

        polygonEditionModel.setAltitudeLowerBound("10");
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());

        undo();
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertFalse(polygonEditionModel.isDeleteButtonEnabled());

        polygonEditionModel.addVertex(createVertex(1.0, 30.0));
        assertThat(polygonEditionModel, aPolygonEditionModel(
                aName("MaZone"),
                anAltitudeLowerBound(0),
                anAltitudeUpperBound(0),
                aVertices(aVertex(1.0, 30.0))));

        redo();
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertThat(polygonEditionModel, aPolygonEditionModel(
                aName("MaZone"),
                anAltitudeLowerBound(0),
                anAltitudeUpperBound(20.0),
                aVertices(aVertex(1.0, 6.0),
                        aVertex(2.0, 5.0),
                        aVertex(3.0, 4.0))));
    }

    @Test
    public void polygonLifecycleZoneEdition() {
        final DoubleProperty[] p1 = createVertex(1, 0);
        final DoubleProperty[] p2 = createVertex(2, 0);
        final DoubleProperty[] p3 = createVertex(3, 0);
        final PolygonEditionModel polygonEditionModel = polygonEditionModel("MaZone",
                withAltitudes("10.0", "35.0"),
                withVertices(p1, p2, p3));
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertFalse(polygonEditionModel.isDeleteButtonEnabled());

        polygonEditionModel.save();
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());
        assertFalse(polygonEditionModel.getSaveButtonEnabled());

        polygonEditionModel.setAltitudeLowerBound("45");
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        polygonEditionModel.setAltitudeUpperBound("55");
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());

        polygonEditionModel.save();
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());
        assertFalse(polygonEditionModel.getSaveButtonEnabled());

        polygonEditionModel.addNewVertex();
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());

        polygonEditionModel.save();
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());
        assertFalse(polygonEditionModel.getSaveButtonEnabled());

        undo(); //undo add point p4
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertThat(polygonEditionModel,
                aPolygonEditionModel(
                        aVertices(aVertex(1.0, 0.0),
                                aVertex(2.0, 0.0),
                                aVertex(3.0, 0.0))));
        polygonEditionModel.setAltitudeUpperBound("65");
        assertTrue(polygonEditionModel.getSaveButtonEnabled());
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());

        undo(); // undo set altitude lower / upper bound
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertThat(polygonEditionModel, aZoneEditionModel(anAltitudeLowerBound(10), anAltitudeUpperBound(35)));
        assertTrue(polygonEditionModel.isDeleteButtonEnabled());

        undo(); // undo first save (zone creation)
        assertFalse(polygonEditionModel.isDeleteButtonEnabled());
        assertFalse(polygonEditionModel.getSaveButtonEnabled());
        assertThat(polygonEditionModel, aZoneEditionModel(anAltitudeLowerBound(0), anAltitudeUpperBound(0)));
    }
}