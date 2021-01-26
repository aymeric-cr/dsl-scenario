package fdit.storage.alteration;

import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.alteration.AlterationSchema;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.action.*;
import fdit.metamodel.alteration.action.Action.ActionTypeSwitch;
import fdit.metamodel.alteration.action.Action.ActionVisitor;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.parameters.ActionParameter.ActionParameterVisitor;
import fdit.metamodel.alteration.parameters.Mode.ModeSwitch;
import fdit.metamodel.alteration.scope.*;
import fdit.metamodel.alteration.scope.Scope.ScopeVisitor;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.recording.Recording.RecordingSwitch;
import fdit.storage.zone.ZoneSaver;
import org.apache.commons.lang.NotImplementedException;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.alteration.AlterationUtils.extractAircraftIDs;
import static fdit.metamodel.element.DirectoryUtils.findRecording;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.storage.alteration.AlterationSpecificationSaver.createZoneChild;
import static fdit.storage.alteration.AlterationSpecificationStorage.*;
import static fdit.tools.jdom.Jdom2Utils.*;
import static fdit.tools.stream.StreamUtils.mapping;
import static fdit.tools.stream.StreamUtils.tryFind;
import static java.lang.String.valueOf;
import static java.util.Optional.empty;

public final class AlterationSpecificationConverter {

    private static final String PARAMETERS = "parameters";
    private static final String ACTION_TYPE_ALTERATION = "ALTERATION";
    private static final String ACTION_TYPE_SATURATION = "SATURATION";
    private static final String ACTION_TYPE_DELETION = "DELETION";
    private static final String ACTION_TYPE_TIMESTAMP = "ALTERATIONTIMESTAMP";
    private static final String ACTION_TYPE_REPLAY = "REPLAY";
    private static final String ACTION_TYPE_TRAJECTORY_MODIFICATION = "TRAJECTORY";
    private static final String ACTION_TYPE_CREATION = "CREATION";
    private static final String RECORD = "record";
    private static final String FIRST_DATE = "firstDate";
    private static final String SCOPE = "scope";
    private static final String ACTION = "action";
    private static final String HEX_IDENT = "hexIdent";
    private static final String ATTRIBUTE_MODE = "mode";
    private static final String MODE_SIMPLE = "simple";
    private static final String MODE_OFFSET = "offset";
    private static final String MODE_NOISE = "noise";
    private static final String MODE_DRIFT = "drift";
    private static final String TARGET = "target";
    private static final String ATTRIBUTE_IDENTIFIER = "identifier";
    private static final String PARAMETER = "parameter";
    private static final String KEY = "key";
    private static final String NUMBER = "number";
    private static final String FREQUENCY = "frequency";
    private static final String TIMESTAMP = "timestamp";
    private static final String INTERROGATOR = "interrogator";
    private static final String VALUE = "value";
    private static final String ACTION_TYPE = "alterationType";
    private static final String SCOPE_TYPE = "type";
    private static final String SCOPE_TYPE_TIME_WINDOW = "timeWindow";
    private static final String SCOPE_TYPE_GEO_TIME_WINDOW = "geoTimeWindow";
    private static final String SCOPE_TYPE_TRIGGER = "trigger";
    private static final String LOWER_BOUND = "lowerBound";
    private static final String UPPER_BOUND = "upperBound";
    private static final String TIME = "time";
    private static final String TRAJECTORY = "trajectory";

    private AlterationSpecificationConverter() {

    }

    public static File convertAlterationToIncident(final AlterationSpecification specification,
                                                   final AlterationSchema alterationSchema,
                                                   final Recording recording,
                                                   final File rootFile) {
        return convertAlterationToIncident(specification, alterationSchema, recording, 0, rootFile);
    }

    public static File convertAlterationToIncident(final AlterationSpecification specification,
                                                   final Recording recording,
                                                   final int testCaseNumber,
                                                   final File rootFile) {
        try {
            final File alterationFile = new File(
                    getFditElementFile(specification.getFather(), rootFile),
                    recording.getName() + '_' + specification.getName() + "_testCase" + testCaseNumber + "_tempIncident.xml");
            try (final OutputStream outputStream = new FileOutputStream(alterationFile)) {
                writeDocument(new Document(createRootParametersElement(
                        specification.getAlterationSchema(),
                        recording)),
                        outputStream);
            }
            return alterationFile;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File convertAlterationToIncident(final AlterationSpecification specification,
                                                   final AlterationSchema alterationSchema,
                                                   final Recording recording,
                                                   final int testCaseNumber,
                                                   final File destination) {
        try {
            final File alterationFile = new File(destination,
                    recording.getName() + '_' + specification.getName() + "_testCase" + testCaseNumber + "_tempIncident.xml");
            try (final OutputStream outputStream = new FileOutputStream(alterationFile)) {
                writeDocument(new Document(createRootParametersElement(
                        alterationSchema,
                        recording)),
                        outputStream);
            }
            return alterationFile;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element createRootParametersElement(final AlterationSchema alterationSchema,
                                                       final Recording recording) {
        final Element scenarioElement = createElement(SCENARIO,
                child(createRecordElement(recording)),
                children(mapping(alterationSchema.getActions(), scenarioAction ->
                        createActionElement(scenarioAction, recording))));
        if (recording instanceof BaseStationRecording) {
            scenarioElement.addContent(createFirstDateElement(recording));
        }
        return scenarioElement;
    }

    private static Content createFirstDateElement(final Recording recording) {
        return createElement(FIRST_DATE, text(valueOf(recording.getFirstDate())));
    }

    private static Content createRecordElement(final Recording recording) {
        final File recordingFile = getFditElementFile(recording, FDIT_MANAGER.getRootFile());
        if (recordingFile.exists()) {
            return createElement(RECORD, text(recordingFile.getAbsolutePath()));
        }
        throw new RuntimeException(recordingFile.getAbsolutePath() + " doesn't exist");
    }

    private static Element createActionElement(final Action action,
                                               final Recording recording) {
        return new ActionVisitor<Element>() {

            @Override
            public Element visitAlteration(final Alteration alteration) {
                final Collection<Element> parametersElements = newArrayList();
                for (final ActionParameter parameter : alteration.getParameters()) {
                    parametersElements.add(createParameterElement(parameter, recording));
                }
                return createActionElement(alteration, recording, parametersElements);
            }

            @Override
            public Element visitTrajectoryModification(final TrajectoryModification trajectoryModification) {
                final Collection<Element> parametersElements = newArrayList(
                        createParameterElement(trajectoryModification.getTrajectory(), recording));
                return createActionElement(trajectoryModification, recording, parametersElements);
            }

            @Override
            public Element visitCreation(final Creation creation) {
                final Collection<Element> parametersElements = newArrayList();
                for (final ActionParameter parameter : creation.getParameters()) {
                    parametersElements.add(createParameterElement(parameter, recording));
                }
                return createActionElement(creation, recording, parametersElements);
            }

            @Override
            public Element visitDeletion(final Deletion deletion) {
                final Element parameterElement = createElement(PARAMETER,
                        attribute(ATTRIBUTE_MODE, MODE_SIMPLE),
                        child(createElement(FREQUENCY, text(valueOf(deletion.getFrequency().getValue())))));
                return createActionElement(deletion, recording, newArrayList(parameterElement));
            }

            @Override
            public Element visitSaturation(final Saturation saturation) {
                final String icao;
                if (saturation.getIcaoParameter() instanceof Value) {
                    icao = ((Value) saturation.getIcaoParameter()).getContent();
                } else {
                    icao = "RANDOM";
                }
                final Collection<Element> parametersElements = newArrayList(createElement(PARAMETER,
                        attribute(ATTRIBUTE_MODE, MODE_SIMPLE),
                        children(
                                createElement(KEY, text(renderCharacteristic(Characteristic.ICAO, recording))),
                                createElement(VALUE, text(icao)),
                                createElement(NUMBER, text(valueOf(saturation.getAircraftNumber().getValue()))))));
                return createActionElement(saturation, recording, parametersElements);
            }

            @Override
            public Element visitDelay(final Delay delay) {
                final Collection<Element> parametersElements = newArrayList();
                for (final ActionParameter parameter : delay.getParameters()) {
                    parametersElements.add(createParameterElement(parameter, recording));
                }
                return createActionElement(delay, recording, parametersElements);
            }

            @Override
            public Element visitReplay(final Replay replay) {
                final Collection<Element> parametersElements = newArrayList();
                for (final ActionParameter parameter : replay.getParameters()) {
                    parametersElements.add(createParameterElement(parameter, recording));
                }
                final Optional<Recording> srcRecording = extractRecordingFromParameters(replay.getParameters());
                if (srcRecording.isPresent()) {
                    return createActionElement(replay, srcRecording.get(), parametersElements);
                }
                throw new RuntimeException("Recording not found.");
            }
        }.accept(action);
    }

    private static Optional<Recording> extractRecordingFromParameters(final Collection<? extends ActionParameter> parameters) {
        for (final ActionParameter parameter : parameters) {
            if (parameter instanceof RecordName) {
                return findRecording(((RecordName) parameter).getName(), FDIT_MANAGER.getRoot());
            }
        }
        return empty();
    }

    private static Element createActionElement(final Action action,
                                               final Recording recording,
                                               final Collection<Element> parametersElements) {
        return createElement(ACTION,
                attribute(ACTION_TYPE, renderActionType(action)),
                child(createScopeElement(action.getScope())),
                child(PARAMETERS,
                        child(createTargetElement(action, recording)),
                        children(parametersElements)));
    }

    private static Element createTargetElement(final Action action, final Recording recording) {
        return createElement(TARGET,
                attribute(ATTRIBUTE_IDENTIFIER, renderICAO(recording)),
                text(getTarget(recording, action)));
    }

    private static String getTarget(final Recording recording,
                                    final Action action) {
        StringBuilder target = new StringBuilder();
        final Collection<Integer> selectedAircraftIDs = extractAircraftIDs(action);
        if (selectedAircraftIDs.size() == recording.getAircrafts().size()) {
            return "ALL";
        }
        for (final Aircraft aircraft : recording.getAircrafts()) {
            final Optional<Integer> found = tryFind(selectedAircraftIDs,
                    selectedAircraft -> aircraft.getAircraftId() == selectedAircraft);
            if (found.isPresent()) {
                target.append(aircraft.getStringICAO());
                target.append(",");
            }
        }
        final int lastIndexOfComma = target.lastIndexOf(",");
        if (lastIndexOfComma != -1) {
            target.deleteCharAt(lastIndexOfComma);
        }
        return target.toString();
    }

    private static String renderICAO(final Recording recording) {
        return new RecordingSwitch<String>() {

            @Override
            public String visitBaseStationRecording(Recording recording) {
                return HEX_IDENT;
            }

            @Override
            public String visitSiteBaseStationRecording(Recording recording) {
                return HEX_IDENT;
            }
        }.doSwitch(recording);
    }

    private static Content createScopeElement(final Scope scope) {
        return new ScopeVisitor<Content>() {
                     @Override
            public Content visitGeoThreshold(final GeoThreshold geoThreshold) {
                throw new NotImplementedException();
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

    private static Element createWaypointElement(final AircraftWayPoint waypoint) {
        return createElement(PARAMETER_WAYPOINT,
                child(ZoneSaver.createVertexElement(waypoint.getCoordinates())),
                child(VALUE_TYPE_ALTITUDE, text(valueOf(waypoint.getAltitude()))),
                child(VALUE_TYPE_TIME, text(valueOf(waypoint.getTime()))));
    }

    private static Element createParameterElement(final ActionParameter parameter,
                                                  final Recording recording) {
        return new ActionParameterVisitor<Element>() {
            @Override
            public Element visitValue(final Value value) {
                return createElement(PARAMETER,
                        attribute(ATTRIBUTE_MODE, renderMode(value.getMode())),
                        child(createElement(KEY, text(renderCharacteristic(value.getCharacteristic(), recording)))),
                        child(createElement(VALUE, text(value.getContent()))));
            }

            @Override
            public Element visitTimestamp(final Timestamp timestamp) {
                final String mode = timestamp.isOffset() ? MODE_OFFSET : MODE_SIMPLE;
                return createElement(PARAMETER,
                        attribute(ATTRIBUTE_MODE, mode),
                        child(createElement(KEY, text(TIMESTAMP))),
                        child(createElement(VALUE, text(valueOf(timestamp.getValue())))));
            }

            @Override
            public Element visitTrajectory(final Trajectory trajectory) {
                final Collection<Element> wayPoints = newArrayList();
                for (final AircraftWayPoint wayPoint : trajectory.getWayPoints()) {
                    wayPoints.add(createWaypointElement(wayPoint));
                }
                return createElement(TRAJECTORY, children(wayPoints));
            }

            @Override
            public Element visitFrequency(Frequency frequency) {
                throw new NotImplementedException();
            }

            @Override
            public Element visitRangeValue(final RangeValue rangeValue) {
                throw new NotImplementedException();
            }

            @Override
            public Element visitListValue(final ListValue listValue) {
                throw new NotImplementedException();
            }

            @Override
            public Element visitAircraftNumber(final AircraftNumber aircraftNumber) {
                throw new NotImplementedException();
            }

            @Override
            public Element visitRecordName(final RecordName recordName) {
                final Optional<Recording> result = findRecording(recordName.getName(), FDIT_MANAGER.getRoot());
                if (result.isPresent()) {
                    final File recordingFile = getFditElementFile(result.get(), FDIT_MANAGER.getRootFile());
                    return createElement(RECORD_PATH, text(recordingFile.getAbsolutePath()));
                }
                throw new RuntimeException("Recording " + recordName.getName() + " not found");
            }
        }.accept(parameter);
    }

    private static String renderMode(final Mode mode) {
        return new ModeSwitch<String>() {

            @Override
            public String visitSimple() {
                return MODE_SIMPLE;
            }

            @Override
            public String visitOffset() {
                return MODE_OFFSET;
            }

            @Override
            public String visitNoise() {
                return MODE_NOISE;
            }

            @Override
            public String visitDrift() {
                return MODE_DRIFT;
            }
        }.doSwitch(mode);
    }

    private static String renderCharacteristic(final Characteristic characteristic, final Recording recording) {
        return new Characteristic.CharacteristicSwitch<String>() {
            @Override
            public String visitAltitude() {
                return VALUE_TYPE_ALTITUDE;
            }

            @Override
            public String visitCallSign() {
                return VALUE_TYPE_CALL_SIGN;
            }

            @Override
            public String visitEmergency() {
                return VALUE_TYPE_EMERGENCY;
            }

            @Override
            public String visitIcao() {
                return renderICAO(recording);
            }

            @Override
            public String visitGroundSpeed() {
                return VALUE_TYPE_GROUND_SPEED;
            }

            @Override
            public String visitLatitude() {
                return VALUE_TYPE_LATITUDE;
            }

            @Override
            public String visitLongitude() {
                return VALUE_TYPE_LONGITUDE;
            }

            @Override
            public String visitSpi() {
                return VALUE_TYPE_SPI;
            }

            @Override
            public String visitSquawk() {
                return renderSquawk(recording);
            }

            @Override
            public String visitTrack() {
                return VALUE_TYPE_TRACK;
            }

            @Override
            public String visitTimestampNano() {
                return VALUE_TYPE_TIMESTAMP_NANO;
            }

            @Override
            public String visitEastWestVelocity() {
                return VALUE_TYPE_EAST_WEST_VELOCITY;
            }

            @Override
            public String visitNorthSouthVelocity() {
                return VALUE_TYPE_NORTH_SOUTH_VELOCITY;
            }

            @Override
            public String visitVerticalRate() {
                return VALUE_TYPE_VERTICAL_RATE;
            }

            @Override
            public String visitIsOnGround() {
                return VALUE_TYPE_IS_ON_GROUND;
            }

            @Override
            public String visitAlert() {
                return VALUE_TYPE_ALERT;
            }

        }.doSwitch(characteristic);
    }

    private static String renderSquawk(final Recording recording) {
        return new RecordingSwitch<String>() {

            @Override
            public String visitBaseStationRecording(Recording recording) {
                return VALUE_TYPE_SQUAWK;
            }

            @Override
            public String visitSiteBaseStationRecording(Recording recording) {
                return VALUE_TYPE_SQUAWK;
            }
        }.doSwitch(recording);
    }

    private static String renderActionType(final Action action) {
        return new ActionTypeSwitch<String>() {

            @Override
            public String visitAlteration() {
                return ACTION_TYPE_ALTERATION;
            }

            @Override
            public String visitDeletion() {
                return ACTION_TYPE_DELETION;
            }

            @Override
            public String visitSaturation() {
                return ACTION_TYPE_SATURATION;
            }

            @Override
            public String visitTimestamp() {
                return ACTION_TYPE_TIMESTAMP;
            }

            @Override
            public String visitReplay() {
                return ACTION_TYPE_REPLAY;
            }

            @Override
            public String visitTrajectoryModification() {
                return ACTION_TYPE_TRAJECTORY_MODIFICATION;
            }

            @Override
            public String visitCreation() {
                return ACTION_TYPE_CREATION;
            }
        }.doSwitch(action.getActionType());
    }
}