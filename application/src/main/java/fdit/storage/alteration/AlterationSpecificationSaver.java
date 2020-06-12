package fdit.storage.alteration;

import fdit.metamodel.alteration.AlterationSchema;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.action.Action;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.parameters.ActionParameter.ActionParameterVisitor;
import fdit.metamodel.alteration.scope.*;
import fdit.metamodel.alteration.scope.Scope.ScopeVisitor;
import fdit.metamodel.zone.Zone;
import fdit.storage.zone.ZoneSaver;
import org.apache.commons.lang.NotImplementedException;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.storage.alteration.AlterationSpecificationStorage.*;
import static fdit.storage.zone.ZoneSaver.*;
import static fdit.storage.zone.ZoneStorage.IDENT;
import static fdit.storage.zone.ZoneStorage.POLYGON;
import static fdit.tools.jdom.Jdom2Utils.*;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.String.valueOf;

public final class AlterationSpecificationSaver {

    private AlterationSpecificationSaver() {
    }

    public static void saveAlterationSpecification(final AlterationSpecification alterationSpecification,
                                                   final File rootFile) throws IOException {
        final File alterationFile = getFditElementFile(alterationSpecification, rootFile);
        try (final OutputStream alterationOutputStream = new FileOutputStream(alterationFile)) {
            writeDocument(new Document(createSpecificationElement(alterationSpecification)), alterationOutputStream);
        }
    }

    private static Element createSpecificationElement(final AlterationSpecification alterationSpecification) {
        final Element scenarioElement = createScenarioElement(alterationSpecification.getAlterationSchema());
        return createElement(SPECIFICATION, child(scenarioElement));
    }

    private static Element createScenarioElement(final AlterationSchema alterationSchema) {
        final Collection<Element> actions = mapping(alterationSchema.getActions(),
                AlterationSpecificationSaver::createActionElement);
        return createElement(SCENARIO,
                attribute(SCENARIO_NAME, alterationSchema.getName()),
                child(DESCRIPTION, text(alterationSchema.getDescription())),
                children(actions));
    }

    private static Element createActionElement(final Action action) {
        final Collection<Element> parametersElements = mapping(action.getParameters(),
                AlterationSpecificationSaver::createParameterElement);
        final Element actionElement = createElement(ACTION,
                attribute(ACTION_TYPE, renderActionType(action)),
                child(ACTION_NAME, text(action.getName())),
                child(DESCRIPTION, text(action.getDescription())),
                child(createScopeElement(action.getScope())),
                child(PARAMETERS,
                        child(TARGET, text(action.getTarget())),
                        children(parametersElements)));
        return actionElement;
    }

    private static Element createWayPoint(AircraftWayPoint waypoint) {
        return createElement(PARAMETER_WAYPOINT,
                child(ZoneSaver.createVertexElement(waypoint.getCoordinates())),
                child(VALUE_TYPE_ALTITUDE, text(valueOf(waypoint.getAltitude()))),
                child(VALUE_TYPE_TIME, text(valueOf(waypoint.getTime()))));
    }

    private static Content createScopeElement(final Scope scope) {
        return new ScopeVisitor<Content>() {
            @Override
            public Content visitGeoArea(final GeoArea geoArea) {
                throw new NotImplementedException();
            }

            @Override
            public Content visitGeoThreshold(final GeoThreshold geoThreshold) {
                throw new NotImplementedException();
            }

            @Override
            public Content visitGeoTime(final GeoTime geoTime) {
                throw new NotImplementedException();
            }

            @Override
            public Content visitGeoTimeWindow(final GeoTimeWindow geoTimeWindow) {
                return createElement(SCOPE,
                        attribute(SCOPE_TYPE,
                                SCOPE_TYPE_GEO_TIME_WINDOW),
                        child(createZoneChild(geoTimeWindow.getZone())),
                        child(LOWER_BOUND,
                                text(valueOf(geoTimeWindow.getLowerBoundMillis()))),
                        child(UPPER_BOUND,
                                text(valueOf(geoTimeWindow.getUpperBoundMillis()))));
            }

            @Override
            public Content visitTimeWindow(final TimeWindow timeWindow) {
                return createElement(SCOPE,
                        attribute(SCOPE_TYPE,
                                SCOPE_TYPE_TIME_WINDOW),
                        child(LOWER_BOUND,
                                text(valueOf(timeWindow.getLowerBoundMillis()))),
                        child(UPPER_BOUND,
                                text(valueOf(timeWindow.getUpperBoundMillis()))));

            }

            @Override
            public Content visitTrigger(final Trigger trigger) {
                return createElement(SCOPE,
                        attribute(SCOPE_TYPE,
                                SCOPE_TYPE_TRIGGER),
                        child(TIME, text(valueOf(trigger.getTimeMillis()))));
            }
        }.accept(scope);
    }

    public static Content createZoneChild(final Zone zone) {
        return ((Zone.ZoneVisitor<Element>) polygon -> createElement(POLYGON,
                child(IDENT, text(polygon.getId().toString())),
                child(ZONE_NAME, text(polygon.getName())),
                children(createVerticesElement(polygon),
                        createLowerAltitudeElement(polygon),
                        createUpperAltitudeElement(polygon)))).accept(zone);
    }

    private static Element createParameterElement(final ActionParameter parameter) {
        return new ActionParameterVisitor<Element>() {
            @Override
            public Element visitValue(final Value value) {
                return createElement(VALUE,
                        attribute(VALUE_TYPE, renderCharacteristic(value.getCharacteristic())),
                        attribute(ATTRIBUT_OFFSET, valueOf(value.isOffset())),
                        text(value.getContent()));
            }

            @Override
            public Element visitTimestamp(final Timestamp timestamp) {
                return createElement(PARAMETER_TIMESTAMP,
                        text(valueOf(timestamp.getValue())),
                        attribute(ATTRIBUT_OFFSET, valueOf(timestamp.isOffset())));
            }

            @Override
            public Element visitRangeValue(final RangeValue rangeValue) {
                return createElement(VALUE,
                        attribute(VALUE_TYPE, renderCharacteristic(rangeValue.getCharacteristic())),
                        attribute(ATTRIBUT_OFFSET, valueOf(rangeValue.isOffset())),
                        child(MIN, text(rangeValue.getRangeMinContent())),
                        child(MAX, text(rangeValue.getRangeMaxContent())));
            }

            @Override
            public Element visitListValue(final ListValue listValue) {
                final Element element = createElement(VALUE,
                        attribute(VALUE_TYPE, renderCharacteristic(listValue.getCharacteristic())),
                        attribute(ATTRIBUT_OFFSET, valueOf(listValue.isOffset())));
                for (final String value : listValue.getValues()) {
                    element.getChildren().add(createElement(ITEM, text(value)));
                }
                return element;
            }

            @Override
            public Element visitAircraftNumber(final AircraftNumber aircraftNumber) {
                return createElement(PARAMETER_NUMBER,
                        text(valueOf(aircraftNumber.getValue())));
            }

            @Override
            public Element visitRecordName(final RecordName recordName) {
                return createElement(RECORD_PATH,
                        text(valueOf(recordName.getName())));
            }

            @Override
            public Element visitTrajectory(Trajectory trajectory) {
                final Collection<Element> waypointsElements = mapping(trajectory.getWayPoints(),
                        AlterationSpecificationSaver::createWayPoint);
                return createElement(TRAJECTORY, children(waypointsElements));
            }

            @Override
            public Element visitFrequency(Frequency frequency) {
                return createElement(PARAMETER_FREQUENCY,
                        text(valueOf(frequency.getValue())));
            }
        }.accept(parameter);
    }
}