package fdit.gui.zoneEditor.tabs.zone;

import javafx.beans.property.DoubleProperty;

import java.util.function.Predicate;

import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.getVertexLatitude;
import static fdit.gui.zoneEditor.tabs.zone.ZoneEditionUtils.getVertexLongitude;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.tools.predicate.PredicateUtils.and;

final class ZoneEditionHelper {

    private ZoneEditionHelper() {
    }

    static Predicate<? super ZoneEditionModel> aZoneEditionModel(final Predicate<? super ZoneEditionModel>... constraints) {
        return and(constraints);
    }

    static Predicate<? super ZoneEditionModel> aPolygonEditionModel(final Predicate<? super PolygonEditionModel>... constraints) {
        return editionModel -> and(constraints).test((PolygonEditionModel) editionModel);
    }

    static Predicate<? super ZoneEditionModel> aName(final String name) {
        return zoneEditionModel -> name.equals(zoneEditionModel.getZoneName());
    }

    static Predicate<? super ZoneEditionModel> anAltitudeLowerBound(final double altitude) {
        return zoneEditionModel -> altitude == Double.parseDouble(zoneEditionModel.getAltitudeLowerBound());
    }

    static Predicate<? super ZoneEditionModel> anAltitudeUpperBound(final double altitude) {
        return zoneEditionModel -> Double.parseDouble(zoneEditionModel.getAltitudeUpperBound()) == altitude;
    }

    static Predicate<PolygonEditionModel> aVertices(final Predicate<DoubleProperty[]>... vertices) {
        return fditPolygon -> containsOnly(vertices).test(fditPolygon.getVertices());
    }

    static Predicate<DoubleProperty[]> aVertex(final double latitude,
                                               final double longitude) {
        return vertex -> getVertexLatitude(vertex) == latitude && getVertexLongitude(vertex) == longitude;
    }
}