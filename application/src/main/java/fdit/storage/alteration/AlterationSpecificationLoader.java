package fdit.storage.alteration;

import fdit.metamodel.alteration.AlterationSchema;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.action.*;
import fdit.metamodel.alteration.action.Action.ActionType;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.scope.Scope;
import fdit.metamodel.alteration.scope.TimeWindow;
import fdit.metamodel.alteration.scope.Trigger;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.zone.Zone;
import fdit.storage.LoadingResult;
import fdit.storage.zone.ZoneStorage;
import fdit.tools.i18n.MessageTranslator;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.*;
import static fdit.metamodel.alteration.action.Action.ActionTypeSwitch;
import static fdit.metamodel.alteration.parameters.Characteristic.LATITUDE;
import static fdit.metamodel.alteration.parameters.Characteristic.LONGITUDE;
import static fdit.metamodel.alteration.parameters.Characteristic.*;
import static fdit.metamodel.zone.ZoneUtils.withZoneId;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.storage.LoadingResult.*;
import static fdit.storage.alteration.AlterationSpecificationStorage.*;
import static fdit.storage.zone.ZoneStorage.*;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.jdom.Jdom2Utils.createFromInputStream;
import static fdit.tools.stream.StreamUtils.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public final class AlterationSpecificationLoader {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(AlterationSpecificationLoader.class);

    private AlterationSpecificationLoader() {
    }

    public static LoadingResult<AlterationSpecification> loadAlterationSpecification(final File alterationFile,
                                                                                     final Iterable<Zone> loadedZones) throws Exception {
        try (final InputStream alterationInputStream = new FileInputStream(alterationFile)) {
            final Document alterationDocument = createFromInputStream(alterationInputStream);
            final Element specificationElement = alterationDocument.getRootElement();
            LoadingResult<AlterationSchema> currentResult = succeeded(null);
            final LoadingResult<AlterationSchema> scenario =
                    loadAlterationScenario(specificationElement.getChild(SCENARIO), loadedZones);
            if (scenario.checkFailed()) {
                return chain(scenario, null);
            }
            currentResult = currentResult.combineResult(scenario);
            return chain(currentResult,
                    new AlterationSpecification(buildFditElementName(alterationFile), scenario.getElementLoaded()));
        }
    }

    private static LoadingResult<AlterationSchema> loadAlterationScenario(final Element scenarioElement,
                                                                          final Iterable<Zone> loadedZones) {
        final String description = scenarioElement.getChildText(DESCRIPTION);
        final Collection<Action> actions = newArrayList();
        LoadingResult<Action> currentResult = succeeded(null);
        for (final Element actionElement : scenarioElement.getChildren(ACTION)) {
            final LoadingResult<Action> action = loadScenarioAction(actionElement, loadedZones);
            if (action.checkFailed()) {
                return chain(action, null);
            }
            currentResult = currentResult.combineResult(action);
            actions.add(action.getElementLoaded());
        }
        return chain(currentResult,
                new AlterationSchema(scenarioElement.getAttributeValue(SCENARIO_NAME),
                        description,
                        actions));
    }

    private static LoadingResult<Action> loadScenarioAction(final Element actionElement,
                                                            final Iterable<Zone> loadedZones) {
        final ActionType actionType = retrieveActionType(actionElement.getAttributeValue(ACTION_TYPE));
        final String name = actionElement.getChildText(ACTION_NAME);
        final String description = actionElement.getChildText(DESCRIPTION);
        final LoadingResult<Scope> scope = loadScope(actionElement.getChild(SCOPE), loadedZones);
        final Element parametersElement = actionElement.getChild(PARAMETERS);
        final String target = parametersElement.getChildText(TARGET);
        parametersElement.removeChild(TARGET);
        return chain(scope, new ActionTypeSwitch<Action>() {

            @Override
            public Action visitAlteration() {
                return new Alteration(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadAlterationParameters(parametersElement));
            }

            @Override
            public Action visitDeletion() {
                return new Deletion(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadFrequency(parametersElement));
            }

            @Override
            public Action visitSaturation() {
                return new Saturation(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadAircraftNumber(parametersElement),
                        loadIcaoParameter(parametersElement));
            }

            @Override
            public Action visitTimestamp() {
                return new Delay(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadTimestamp(parametersElement));
            }

            @Override
            public Action visitReplay() {
                return new Replay(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadRecordName(parametersElement),
                        loadAlterationParameters(parametersElement));
            }

            @Override
            public Action visitTrajectoryModification() {
                return new TrajectoryModification(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadTrajectory(parametersElement));
            }

            @Override
            public Action visitCreation() {
                return new Creation(
                        name,
                        description,
                        target,
                        scope.getElementLoaded(),
                        loadTrajectory(parametersElement),
                        loadAlterationParameters(parametersElement));
            }
        }.doSwitch(actionType));
    }

    private static Trajectory loadTrajectory(final Element parametersElement) {
        final Element trajectory = parametersElement.getChild(TRAJECTORY);
        final Collection<AircraftWayPoint> waypoints = newArrayList();
        trajectory.getChildren().forEach(child -> waypoints.add(loadWaypoint(child)));
        return new Trajectory(waypoints);
    }

    private static RecordName loadRecordName(final Element parametersElement) {
        return new RecordName(parametersElement.getChildText(PARAMETER_RECORD_NAME));
    }

    private static Timestamp loadTimestamp(final Element parametersElement) {
        final Element timestampElement = parametersElement.getChild(PARAMETER_TIMESTAMP);
        return new Timestamp(
                parseLong(timestampElement.getText()),
                parseBoolean(timestampElement.getAttributeValue(ATTRIBUT_OFFSET)));
    }

    private static ActionParameter loadIcaoParameter(final Element parametersElement) {
        return loadActionParameter(parametersElement.getChild(VALUE));
    }

    private static AircraftNumber loadAircraftNumber(final Element parametersElement) {
        return new AircraftNumber(parseInt(parametersElement.getChildText(PARAMETER_NUMBER)));
    }

    private static Frequency loadFrequency(final Element parametersElement) {
        return new Frequency(parseInt(parametersElement.getChildText(PARAMETER_FREQUENCY)));
    }

    private static Collection<AlterationParameter> loadAlterationParameters(final Element parametersElement) {
        return filter(mapping(parametersElement.getChildren(), AlterationSpecificationLoader::loadActionParameter), AlterationParameter.class);
    }

    private static AircraftWayPoint loadWaypoint(Element child) {
        return new AircraftWayPoint(
                loadCoordinates(child.getChild(ZoneStorage.VERTEX)),
                parseInt(child.getChildText(VALUE_TYPE_ALTITUDE)),
                parseLong(child.getChildText(VALUE_TYPE_TIME))
        );
    }

    private static Coordinates loadCoordinates(Element coordinate) {
        return new Coordinates(
                Double.parseDouble(coordinate.getChildText(ZoneStorage.LATITUDE)),
                Double.parseDouble(coordinate.getChildText(ZoneStorage.LONGITUDE))
        );
    }

    private static ActionParameter loadActionParameter(final Element parameterElement) {
        final String value = parameterElement.getText();
        switch (parameterElement.getName()) {
            case PARAMETER_NUMBER:
                return new AircraftNumber(parseInt(value));
            case PARAMETER_TIMESTAMP:
                return new Timestamp(
                        parseLong(value),
                        parseBoolean(parameterElement.getAttributeValue(ATTRIBUT_OFFSET)));
            case VALUE:
                if (parameterElement.getChildren().isEmpty()) {
                    return new Value(
                            retrieveCharacteristic(parameterElement),
                            value,
                            retrieveOffset(parameterElement));
                }
                if (isRangeValueElement(parameterElement)) {
                    return loadRangeValue(parameterElement);
                }
                if (isListValueElement(parameterElement)) {
                    return loadListValue(parameterElement);
                }
                break;
            default:
                break;
        }
        throw new IllegalArgumentException("Unknown parameter type");
    }

    private static boolean isRangeValueElement(final Element element) {
        return filter(element.getChildren(), child ->
                child.getName().equals(MIN) || child.getName().equals(MAX)).size() == 2;
    }

    private static boolean isCustomElement(final Element element) {
        return filter(element.getChildren(), child ->
                child.getName().equals(KEY) || child.getName().equals(VALUE)).size() == 2;
    }

    private static boolean isListValueElement(final Element element) {
        return !filter(element.getChildren(), child ->
                child.getName().equals(ITEM)).isEmpty();
    }

    private static ActionParameter loadListValue(final Element element) {
        final List<String> values = newArrayList();

        for (final Element item : filter(element.getChildren(), child -> child.getName().equals(ITEM))) {
            values.add(item.getText());
        }
        return new ListValue(retrieveCharacteristic(element),
                values,
                retrieveOffset(element));
    }

    private static ActionParameter loadRangeValue(final Element element) {
        final String rangeMin = element.getChild(MIN).getText();
        final String rangeMax = element.getChild(MAX).getText();
        return new RangeValue(retrieveCharacteristic(element),
                rangeMin,
                rangeMax,
                retrieveOffset(element));
    }

    private static LoadingResult<Scope> loadScope(final Element scopeElement, final Iterable<Zone> loadedZones) throws IllegalArgumentException {
        switch (scopeElement.getAttributeValue(VALUE_TYPE)) {
            case SCOPE_TYPE_GEO_AREA:
            case SCOPE_TYPE_GEO_TIME:
            case SCOPE_TYPE_GEO_THRESHOLD:
                throw new IllegalArgumentException("Not yet implemented");
            case SCOPE_TYPE_TIME_WINDOW:
                final long lowerBound = parseLong(scopeElement.getChildText(LOWER_BOUND));
                final long upperBound = parseLong(scopeElement.getChildText(UPPER_BOUND));
                return succeeded(new TimeWindow(lowerBound, upperBound));
            case SCOPE_TYPE_TRIGGER:
                final long time = parseLong(scopeElement.getChildText(TIME));
                return succeeded(new Trigger(time));
            default:
                throw new IllegalArgumentException("Unknown scope type");
        }
    }

    private static Zone findZone(final Element zoneElement, final Iterable<Zone> loadedZones) {
        final UUID zoneId = UUID.fromString(zoneElement.getChildText(IDENT));
        return tryFind(loadedZones, withZoneId(zoneId)).orElse(null);
    }

    private static ActionType retrieveActionType(final String type) {
        switch (type) {
            case ACTION_TYPE_ALTERATION:
                return ALTERATION;
            case ACTION_TYPE_CREATION:
                return CREATION;
            case ACTION_TYPE_DELETION:
                return DELETION;
            case ACTION_TYPE_SATURATION:
                return SATURATION;
            case ACTION_TYPE_TIMESTAMP:
                return TIMESTAMP;
            case ACTION_TYPE_TRAJECTORY_MODIFICATION:
                return TRAJECTORY_MODIFICATION;
            default:
                throw new IllegalArgumentException("Unknown action type");
        }
    }

    @SuppressWarnings("SwitchStatementWithTooManyBranches")
    private static Characteristic retrieveCharacteristic(final Element valueElement) {
        switch (valueElement.getAttributeValue(VALUE_TYPE)) {
            case VALUE_TYPE_ALERT:
                return ALERT;
            case VALUE_TYPE_ALTITUDE:
                return ALTITUDE;
            case VALUE_TYPE_CALL_SIGN:
                return CALL_SIGN;
            case VALUE_TYPE_EMERGENCY:
                return EMERGENCY;
            case VALUE_TYPE_GROUND_SPEED:
                return GROUND_SPEED;
            case VALUE_TYPE_ICAO:
                return ICAO;
            case VALUE_TYPE_IS_ON_GROUND:
                return IS_ON_GROUND;
            case VALUE_TYPE_LATITUDE:
                return LATITUDE;
            case VALUE_TYPE_LONGITUDE:
                return LONGITUDE;
            case VALUE_TYPE_SPI:
                return SPI;
            case VALUE_TYPE_SQUAWK:
                return SQUAWK;
            case VALUE_TYPE_TRACK:
                return TRACK;
            case VALUE_TYPE_VERTICAL_RATE:
                return VERTICAL_RATE;
            case VALUE_TYPE_TIMESTAMP_NANO:
                return TIMESTAMP_NANO;
            case VALUE_TYPE_EAST_WEST_VELOCITY:
                return EAST_WEST_VELOCITY;
            case VALUE_TYPE_NORTH_SOUTH_VELOCITY:
                return NORTH_SOUTH_VELOCITY;
            default:
                throw new IllegalArgumentException("Unknown characteristic");
        }
    }

    private static boolean retrieveOffset(final Element valueElement) {
        switch (valueElement.getAttributeValue(ATTRIBUT_OFFSET)) {
            case OFFSET_VALUE_TRUE:
                return true;
            case OFFSET_VALUE_FALSE:
                return false;
            default:
                throw new IllegalArgumentException("Unknown offset value");
        }
    }
}