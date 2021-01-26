package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.ide.AttackScenarioFacade;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Constant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.ListConstant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Memory;
import fdit.gui.schemaEditor.schemaInterpretation.memory.RangeConstant;
import fdit.gui.utils.ThreadSafeBooleanProperty;
import fdit.ltlcondition.ide.LTLConditionFacade;
import fdit.metamodel.alteration.parameters.Characteristic;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.tools.i18n.MessageTranslator;
import fdit.triggcondition.ide.TriggeringConditionFacade;
import javafx.beans.property.BooleanProperty;
import javafx.util.Pair;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.ecore.EObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.schemaEditor.schemaInterpretation.AttackScenarioInterpretationUtils.getCharacteristicByString;
import static fdit.metamodel.alteration.parameters.Characteristic.*;
import static fdit.metamodel.coordinates.CoordinatesUtils.isValidLatitude;
import static fdit.metamodel.coordinates.CoordinatesUtils.isValidLongitude;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.alteration.AlterationSpecificationStorage.PARAMETER_FREQUENCY;
import static fdit.storage.alteration.AlterationSpecificationStorage.PARAMETER_NUMBER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.filter;
import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;

public class SchemaSwitchSemanticAnalyse extends AttackScenarioSwitch<Object> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaSwitchSemanticAnalyse.class);
    private final AttackScenarioFacade attackScenarioDslFacade;
    private final LTLConditionFacade filterFacade = LTLConditionFacade.get();
    private final TriggeringConditionFacade triggerFacade = TriggeringConditionFacade.get();
    private Schema schema;
    private final Memory memory;
    private final BooleanProperty convertible = new ThreadSafeBooleanProperty(true);


    SchemaSwitchSemanticAnalyse(final AttackScenarioFacade attackScenarioDslFacade)
            throws NoSuchAlgorithmException {
        this.attackScenarioDslFacade = attackScenarioDslFacade;
        filterFacade.initialize(FDIT_MANAGER.getRoot());
        memory = new Memory();
    }

    public String processAnalysis(final Schema schema) {
        memory.clear();
        this.schema = schema;
        try {
            attackScenarioDslFacade.parse(schema.getContent());
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        if (attackScenarioDslFacade.getAST().isEmpty()) {
            return "";
        }
        return (String) doSwitch(attackScenarioDslFacade.getAST().get(0));
    }

    @Override
    public Object caseASTScenario(final ASTScenario object) {
        final StringBuilder stringBuilder = new StringBuilder();
        convertible.setValue(true);
        object.getDeclarations().forEach(astDeclaration -> stringBuilder.append(doSwitch(astDeclaration)));
        object.getInstructions().forEach(astInstruction -> stringBuilder.append(doSwitch(astInstruction)));
        return stringBuilder.toString();
    }

    @Override
    public Object caseASTParameters(final ASTParameters object) {
        final StringBuilder errors = new StringBuilder();
        for (final ASTParameter parameter : object.getItems()) {
            if (isDuplicateParameter(object, parameter.getName().getLiteral())) {
                errors.append(TRANSLATOR.getMessage("error.duplicateParameter", parameter.getName().getLiteral()));
                break;
            }
        }
        object.getItems().forEach(astParameter -> errors.append(doSwitch(astParameter)));
        return errors;
    }

    @Override
    public Object caseASTCreationParameters(final ASTCreationParameters object) {
        final StringBuilder errors = new StringBuilder();
        for (final ASTCreationParameter parameter : object.getItems()) {
            if (isDuplicateParameter(object, parameter.getName().getLiteral())) {
                errors.append(TRANSLATOR.getMessage("error.duplicateParameter", parameter.getName().getLiteral()));
                break;
            }
        }
        object.getItems().forEach(astParameter -> errors.append(doSwitch(astParameter)));
        return errors;
    }

    @Override
    public Object caseASTCreationParameter(final ASTCreationParameter object) {
        try {
            final StringBuilder errors = new StringBuilder();
            final String characteristicName = object.getName().getLiteral().toUpperCase();
            final Characteristic characteristic = getCharacteristicByString(characteristicName);
            final ASTValue value = object.getValue();
            final Predicate<String> predicate = getValidationFunction(characteristic);
            if (isAnOffset(value)) {
                return TRANSLATOR.getMessage("error.offset") + '\n';
            }
            if (value instanceof ASTConstantValue) {
                final boolean canBeList = characteristic.canBeAList();
                final boolean canBeRange = characteristic.canBeARange();
                if (canBeList) {
                    errors.append(computeListError(value, predicate, characteristicName, canBeRange));
                }
                if (canBeRange) {
                    errors.append(computeRangeError(value, predicate, characteristicName, canBeList));
                }
            } else {
                errors.append(computeValueError(value, predicate, characteristicName));
            }
            return errors.toString();
        } catch (final RuntimeException ex) {
            return TRANSLATOR.getMessage("error.unknownCharacteristic", object.getName()) + '\n';
        }
    }

    @Override
    public Object caseASTSaturationParameters(final ASTSaturationParameters object) {
        final StringBuilder errors = new StringBuilder();
        if (object.getItems().size() != 2) {
            errors.append(TRANSLATOR.getMessage("error.saturationParameter"));
        }
        for (final ASTSaturationParameter parameter : object.getItems()) {
            if (isDuplicateParameter(object, parameter.getName().getLiteral())) {
                errors.append(TRANSLATOR.getMessage("error.duplicateParameter", parameter.getName().getLiteral()));
                break;
            }
            object.getItems().forEach(astParameter -> errors.append(doSwitch(astParameter)));
        }
        return errors;
    }

    private boolean isDuplicateParameter(final EObject object, final String name) {
        if (object instanceof ASTParameters) {
            return filter(((ASTParameters) object).getItems(), toCompare ->
                    name.compareToIgnoreCase(toCompare.getName().getLiteral()) == 0).size() > 1;
        } else if (object instanceof ASTSaturationParameters) {
            return filter(((ASTSaturationParameters) object).getItems(), toCompare ->
                    name.compareToIgnoreCase(toCompare.getName().getLiteral()) == 0).size() > 1;
        }
        return false;
    }

    @Override
    public Object caseASTReplay(final ASTReplay object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getParameters() != null) {
            errors.append(doSwitch(object.getParameters()));
        }
        return errors;
    }

    @Override
    public Object caseASTPlaneFrom(final ASTPlaneFrom object) {
        final StringBuilder errors = new StringBuilder();
        if (object.getFilters() != null) {
            errors.append(doSwitch(object.getFilters()));
        }
        errors.append(caseSourceRec(object.getRecording()));
        return errors;
    }

    @Override
    public Object caseASTAllPlaneFrom(final ASTAllPlaneFrom object) {
        final StringBuilder errors = new StringBuilder();
        if (object.getFilters() != null) {
            errors.append(doSwitch(object.getFilters()));
        }
        errors.append(caseSourceRec(object.getRecording()));
        return errors;
    }

    private String caseSourceRec(final ASTValue recording) {
        final StringBuilder errors = new StringBuilder();
        if (recording instanceof ASTStringValue &&
                !findRecording(((ASTStringValue) recording).getContent(), FDIT_MANAGER.getRoot()).isPresent()) {
            errors.append(TRANSLATOR.getMessage("error.unknownRecording", ((ASTStringValue) recording).getContent())).append("\n");
        } else if (recording instanceof ASTIntegerValue || recording instanceof ASTDoubleValue) {
            errors.append(TRANSLATOR.getMessage("error.rectype"));
        } else if (recording instanceof ASTConstantValue) {
            convertible.setValue(false);
            final Constant constant = memory.getConstant(((ASTConstantValue) recording).getContent());
            if (constant instanceof ListConstant) {
                final ListConstant<?> cst = (ListConstant<?>) constant;
                for (final Object val : cst.getValues()) {
                    if (!(val instanceof String)) {
                        errors.append(TRANSLATOR.getMessage("error.rectype")).append('\n');
                    } else if (!findRecording((String) val, FDIT_MANAGER.getRoot()).isPresent())
                        errors.append(TRANSLATOR.getMessage("error.unknownRecording", val)).append("\n");
                }
            }
        }
        return errors.toString();
    }

    @Override
    public Object caseASTFilters(final ASTFilters filters) {
        final StringBuilder errors = new StringBuilder();
        for (final ASTValue filter : filters.getFilters()) {
            if (filter instanceof ASTStringValue) {
                errors.append(computeFilterError(((ASTStringValue) filter).getContent()));
            } else if (filter instanceof ASTConstantValue) {
                convertible.setValue(false);
                final Constant constant = memory.getConstant(((ASTConstantValue) filter).getContent());
                errors.append(computeConstantFilterErrors(constant));
            } else {
                errors.append(computeFilterTriggerTypeError(filter));
            }
        }
        return errors.toString();
    }

    @Override
    public Object caseASTTrigger(final ASTTrigger object) {
        final StringBuilder errors = new StringBuilder();
        final ASTValue trigger = object.getTriggername();
        if (trigger instanceof ASTStringValue) {
            errors.append(computeTriggerError(((ASTStringValue) trigger).getContent()));
        } else if (trigger instanceof ASTConstantValue) {
            convertible.setValue(false);
            final Constant constant = memory.getConstant(((ASTConstantValue) trigger).getContent());
            errors.append(computeConstantTriggerErrors(constant));
        } else {
            errors.append(computeFilterTriggerTypeError(trigger));
        }
        return errors.toString();
    }

    @Override
    public Object caseASTSaturationParameter(final ASTSaturationParameter object) {
        if (object.getName().getLiteral().compareToIgnoreCase(PARAMETER_NUMBER) == 0) {
            return computeIntError(object.getValue(), PARAMETER_NUMBER);
        } else {
            try {
                if (isAnOffset(object.getValue())) {
                    return TRANSLATOR.getMessage("error.offset") + '\n';
                }
                final Characteristic characteristic = getCharacteristicByString(object.getName().getLiteral());
                switch (characteristic) {
                    case ICAO:
                        return computeIcaoError(object.getValue());
                    case SQUAWK:
                        return computeSquawkError(object.getValue());
                    case CALL_SIGN:
                        return computeCallsignError(object.getValue());
                    case ALTITUDE:
                        return computeAltitudeError(object.getValue());
                    case LATITUDE:
                        return computeLatitudeError(object.getValue());
                    case LONGITUDE:
                        return computeLongitudeError(object.getValue());
                    case GROUND_SPEED:
                        return computeGroundspeedError(object.getValue());
                    case TRACK:
                        return computeTrackError(object.getValue());
                    case EMERGENCY:
                        return computeEmergencyError(object.getValue());
                    case SPI:
                        return computeSpiError(object.getValue());
                    default:
                        return "";
                }
            } catch (final RuntimeException ex) {
                return TRANSLATOR.getMessage("error.unknownCharacteristic", object.getName()) + '\n';
            }
        }
    }

    @Override
    public Object caseASTHideParameter(final ASTHideParameter object) {
        if (isAnOffset(object.getValue())) {
            return TRANSLATOR.getMessage("error.offset") + '\n';
        }
        return computeIntError(object.getValue(), PARAMETER_FREQUENCY);
    }

    @Override
    public Object caseASTParameter(final ASTParameter object) {
        try {
            final StringBuilder errors = new StringBuilder();
            final String characteristicName = object.getName().getLiteral().toUpperCase();
            final Characteristic characteristic = getCharacteristicByString(characteristicName);
            final ASTValue value = object.getValue();
            final Predicate<String> predicate = getValidationFunction(characteristic);
            if (isAnOffset(value)) {
                return TRANSLATOR.getMessage("error.offset") + '\n';
            }
            if (value instanceof ASTConstantValue) {
                final boolean canBeList = characteristic.canBeAList();
                final boolean canBeRange = characteristic.canBeARange();
                if (canBeList) {
                    errors.append(computeListError(value, predicate, characteristicName, canBeRange));
                }
                if (canBeRange) {
                    errors.append(computeRangeError(value, predicate, characteristicName, canBeList));
                }
            } else {
                errors.append(computeValueError(value, predicate, characteristicName));
            }
            return errors.toString();
        } catch (final RuntimeException ex) {
            return TRANSLATOR.getMessage("error.unknownCharacteristic", object.getName()) + '\n';
        }
    }

    private boolean isAnOffset(ASTValue value) {
        return value instanceof ASTLeftShift || value instanceof ASTRightShift;
    }

    @Override
    public Object caseASTTimeScope(final ASTTimeScope object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTWayPoints(final ASTWayPoints object) {
        final StringBuilder errors = new StringBuilder();
        for (final ASTWayPoint wayPoint : object.getWaypoints()) {
            errors.append(doSwitch(wayPoint));
        }
        return errors.toString();
    }

    @Override
    public Object caseASTWayPoint(final ASTWayPoint object) {
        return doSwitch(object.getTime()) +
                computeListError(object.getLatitude(), getValidationFunction(LATITUDE), "latitude", LATITUDE.canBeARange()) +
                computeListError(object.getLongitude(), getValidationFunction(LONGITUDE), "longitude", LONGITUDE.canBeARange()) +
                computeListError(object.getAltitude(), getValidationFunction(ALTITUDE), "altitude", ALTITUDE.canBeARange()) +
                computeRangeError(object.getLatitude(), getValidationFunction(LATITUDE), "latitude", LATITUDE.canBeAList()) +
                computeRangeError(object.getLongitude(), getValidationFunction(LONGITUDE), "longitude", LONGITUDE.canBeAList()) +
                computeRangeError(object.getAltitude(), getValidationFunction(ALTITUDE), "altitude", ALTITUDE.canBeAList());
    }

    @Override
    public Object caseASTWindow(final ASTWindow object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getStart()));
        if (object.getEnd() != null) {
            errors.append(doSwitch(object.getEnd()));
        }
        if (object.getEnd() != null && object.getStart() != null) {
            final Object start = doSwitch(object.getStart());
            final Object end = doSwitch(object.getEnd());
            if (start instanceof Integer && end instanceof Integer && ((int) end) <= ((int) start)) {
                errors.append(TRANSLATOR.getMessage("error.invalidTimeWindow", start, end)).append('\n');
            }
        }
        return errors;
    }

    @Override
    public Object caseASTAtFor(final ASTAtFor object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTime()));
        errors.append(doSwitch(object.getFor()));
        return errors;
    }

    @Override
    public Object caseASTAt(final ASTAt object) {
        return valueOf(doSwitch(object.getTime()));
    }

    private String computeTimeError(long time) {
        final StringBuilder errors = new StringBuilder();
        if (time < 0) {
            errors.append(TRANSLATOR.getMessage("error.negativetime", time)).append('\n');
        }
        if (schema.getRecording() != EMPTY_RECORDING) {
            final long maxTime = schema.getRecording().getMaxRelativeDate();
            if (time * 1000 > maxTime) {
                errors.append(TRANSLATOR.getMessage("error.timetoolong", time, maxTime / 1000)).append('\n');
            }
        }
        return errors.toString();
    }

    @Override
    public Object caseASTTime(final ASTTime object) {
        final ASTValue time = object.getRealTime();
        if (time instanceof ASTIntegerValue) {
            return computeTimeError(((ASTIntegerValue) time).getContent());
        }
        if (isAnOffset(time)) {
            return TRANSLATOR.getMessage("error.offset") + '\n';
        }
        if (time instanceof ASTRecordingValue) {
            final ASTRecordingValue recordingValue = (ASTRecordingValue) time;
            if (recordingValue.getRatio() < 0 || recordingValue.getRatio() > 1) {
                return TRANSLATOR.getMessage("error.invalidRatioTimeValue", recordingValue.getRatio());
            } else {
                return "";
            }
        }
        if (time instanceof ASTConstantValue) {
            final StringBuilder errors = new StringBuilder();
            convertible.setValue(false);
            final Constant constant = memory.getConstant(((ASTConstantValue) time).getContent());
            if (constant instanceof ListConstant) {
                final ListConstant<?> values = (ListConstant<?>) constant;
                for (final Object value : values.getValues()) {
                    if (!(value instanceof Integer
                            || value instanceof ASTIntegerValue
                            || value instanceof ASTRecordingValue)) {
                        errors.append(TRANSLATOR.getMessage("error.invalidConstantTimeValue", value)).append('\n');
                    } else {
                        int res = 0;
                        if (value instanceof ASTIntegerValue) {
                            res = ((ASTIntegerValue) value).getContent();
                        } else if (value instanceof ASTRecordingValue) {
                            Object val = doSwitch((ASTRecordingValue) value);
                            if (val instanceof String) {
                                errors.append((String) val);
                            } else {
                                res = (int) val;
                            }
                        } else {
                            res = (int) value;
                        }
                        errors.append(computeTimeError(res));
                    }
                }
            } else if (constant instanceof RangeConstant) {
                final RangeConstant<?> rangeConstant = (RangeConstant<?>) constant;
                if (!(rangeConstant.getStart() instanceof Integer)) {
                    errors.append(TRANSLATOR.getMessage("error.invalidConstantTimeValue", valueOf(rangeConstant.getStart())))
                            .append('\n');
                } else {
                    errors.append(computeTimeError((Integer) rangeConstant.getStart()));
                }
                if (!(rangeConstant.getEnd() instanceof Integer)) {
                    errors.append(TRANSLATOR.getMessage("error.invalidConstantTimeValue", valueOf(rangeConstant.getEnd())))
                            .append('\n');
                } else {
                    errors.append(computeTimeError((Integer) rangeConstant.getStart()));
                }
            }
            return errors.toString();
        }
        return TRANSLATOR.getMessage("error.timetype");
    }

    @Override
    public Object caseASTStringList(final ASTStringList object) {
        final List<String> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTIntegerRange(final ASTIntegerRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTDoubleRange(final ASTDoubleRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTOffsetList(final ASTOffsetList object) {
        final List<ASTNumberOffset> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTRangeDeclaration(final ASTRangeDeclaration object) {
        final Pair<?, ?> value = (Pair<?, ?>) doSwitch(object.getRange());
        final String name = object.getConstant();
        try {
            memory.addConstant(new RangeConstant(name, value.getKey(), value.getValue()));
        } catch (final RuntimeException ex) {
            final Class<?> clazzStart = value.getKey().getClass();
            final Class<?> clazzEnd = value.getValue().getClass();
            return TRANSLATOR.getMessage("error.range.unauthorizedtype", clazzStart, clazzEnd) + '\n';
        }
        if (parseDouble(valueOf(value.getValue())) < parseDouble(valueOf(value.getKey()))) {
            return TRANSLATOR.getMessage("error.invalidRange", value.getKey(), value.getValue()) + '\n';
        }
        return "";
    }

    @Override
    public Object caseASTListDeclaration(final ASTListDeclaration object) {
        final ArrayList<?> list = (ArrayList<?>) doSwitch(object.getList());
        final String name = object.getConstant();
        if (list.isEmpty()) {
            return TRANSLATOR.getMessage("error.emptyList", name) + '\n';
        }
        try {
            memory.addConstant(new ListConstant<>(name, list));

        } catch (final RuntimeException ex) {
            final Class<?> clazz = list.get(0).getClass();
            return TRANSLATOR.getMessage("error.list.unauthorizedtype", clazz) + '\n';
        }
        return "";
    }

    @Override
    public Object caseASTHide(final ASTHide object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getTrigger() != null) {
            errors.append(doSwitch(object.getTrigger()));
        }
        return errors;
    }

    @Override
    public Object caseASTCreate(final ASTCreate object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getParameters() != null) {
            errors.append(doSwitch(object.getParameters()));
        }
        return errors;
    }

    @Override
    public Object caseASTAlter(final ASTAlter object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getTrigger() != null) {
            errors.append(doSwitch(object.getTrigger()));
        }
        if (object.getParameters() != null) {
            errors.append(doSwitch(object.getParameters()));
        }
        return errors;
    }

    @Override
    public Object caseASTDelay(final ASTDelay object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getDelay() != null) {
            errors.append(doSwitch(object.getDelay()));
        }
        return errors;
    }

    @Override
    public Object caseASTDelayParameter(final ASTDelayParameter object) {
        return doSwitch(object.getDelay());
    }

    @Override
    public Object caseASTTrajectory(final ASTTrajectory object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getTrigger() != null) {
            errors.append(doSwitch(object.getTrigger()));
        }
        if (object.getTrajectory() != null) {
            errors.append(doSwitch(object.getTrajectory()));
        }
        return errors;
    }

    @Override
    public Object caseASTSaturate(final ASTSaturate object) {
        final StringBuilder errors = new StringBuilder();
        errors.append(doSwitch(object.getTarget()));
        errors.append(doSwitch(object.getTimeScope()));
        if (object.getTrigger() != null) {
            errors.append(doSwitch(object.getTrigger()));
        }
        errors.append(doSwitch(object.getParameters()));
        return errors;
    }

    @Override
    public Object caseASTPlane(final ASTPlane object) {
        final StringBuilder errors = new StringBuilder();
        if (object.getFilters() != null) {
            errors.append(doSwitch(object.getFilters()));
        }
        return errors;
    }

    @Override
    public Object caseASTAllPlanes(final ASTAllPlanes object) {
        final StringBuilder errors = new StringBuilder();
        if (object.getFilters() != null) {
            errors.append(doSwitch(object.getFilters()));
        }
        return errors;
    }

    @Override
    public Object caseASTRecordingValue(final ASTRecordingValue object) {
        if (object.getRatio() < 0 || object.getRatio() > 1) {
            return TRANSLATOR.getMessage("error.invalidRatioTimeValue", object.getRatio()) + '\n';
        } else {
            final long maxRelativeDate = schema.getRecording().getMaxRelativeDate();
            return (int) (object.getRatio() * maxRelativeDate) / 1000;
        }
    }

    @Override
    public Object caseASTConstantValue(final ASTConstantValue object) {
        final String name = object.getContent();
        if (memory.getConstant(name) == null) {
            return TRANSLATOR.getMessage("error.nonDeclaredConstant", name) + '\n';
        }
        return "";
    }

    private String computeRangeError(final ASTValue value,
                                     final Predicate<String> predicate,
                                     final String strType,
                                     final boolean canBeList) {
        final StringBuilder errors = new StringBuilder();
        if (value instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(value);
            if (!constantError.isEmpty()) {
                return constantError;
            }
            final Constant constant = memory.getConstant(((ASTConstantValue) value).getContent());
            if (constant instanceof RangeConstant) {
                final Object start = ((RangeConstant<?>) constant).getStart();
                final Object end = ((RangeConstant<?>) constant).getEnd();
                if (!predicate.test(start.toString())) {
                    errors.append(TRANSLATOR.getMessage("error.badvalue", strType, start)).append("\n");
                }
                if (!predicate.test(end.toString())) {
                    errors.append(TRANSLATOR.getMessage("error.badvalue", strType, end)).append("\n");
                }
            }
            if (!canBeList && constant instanceof ListConstant) {
                errors.append(TRANSLATOR.getMessage("error.badMassiveType", strType, "list")).append("\n");
            }
        }
        return errors.toString();
    }

    private static String computeFilterTriggerTypeError(final ASTValue filter) {
        if (filter instanceof ASTIntegerValue || filter instanceof ASTDoubleValue) {
            return TRANSLATOR.getMessage("error.filttype");
        } else {
            return "";
        }
    }

    private String computeFilterError(final String name) {
        final Optional<LTLFilter> filter = findLTLFilter(name, FDIT_MANAGER.getRoot());
        if (!filter.isPresent()) {
            return TRANSLATOR.getMessage("error.unknownFilter", name) + "\n";
        } else {
            filterFacade.parse(filter.get().getContent());
            final BasicDiagnostic basicDiagnostic = filterFacade.getValidationErrors();
            if (basicDiagnostic != null) {
                final StringBuilder errors = new StringBuilder();
                basicDiagnostic.getChildren().forEach(diagnostic -> errors
                        .append("\t-")
                        .append(diagnostic.getMessage())
                        .append("\n"));
                return errors.toString().isEmpty() ? "" : name + ":\n" + errors.toString();
            } else {
                return TRANSLATOR.getMessage("error.emptyFilter", name) + "\n";
            }
        }
    }

    private String computeConstantFilterErrors(final Constant filters) {
        final StringBuilder errors = new StringBuilder();
        if (filters instanceof ListConstant) {
            for (final Object filter : ((ListConstant<?>) filters).getValues()) {
                if (!(filter instanceof String)) {
                    errors.append(TRANSLATOR.getMessage("error.filttype")).append("\n");
                } else {
                    errors.append(computeFilterError((String) filter));
                }
            }
        } else {
            errors.append(TRANSLATOR.getMessage("error.expectedList"));
        }
        return errors.toString();
    }

    private String computeTriggerError(final String name) {
        final Optional<ActionTrigger> trigger = findActionTrigger(name, FDIT_MANAGER.getRoot());
        if (!trigger.isPresent()) {
            return TRANSLATOR.getMessage("error.unknownTrigger", name) + "\n";
        } else {
            triggerFacade.parse(trigger.get().getContent());
            final BasicDiagnostic basicDiagnostic = triggerFacade.getValidationErrors();
            if (basicDiagnostic != null) {
                final StringBuilder errors = new StringBuilder();
                basicDiagnostic.getChildren().forEach(diagnostic ->
                        errors.append("\t-").append(diagnostic.getMessage()).append("\n"));
                return errors.toString().isEmpty() ? "" : name + ":\n" + errors.toString();
            } else {
                return TRANSLATOR.getMessage("error.emptyTrigger", name) + "\n";
            }
        }
    }

    private String computeConstantTriggerErrors(final Constant triggers) {
        final StringBuilder errors = new StringBuilder();
        if (triggers instanceof ListConstant) {
            for (final Object trigger : ((ListConstant<?>) triggers).getValues()) {
                if (!(trigger instanceof String)) {
                    errors.append(TRANSLATOR.getMessage("error.triggtype")).append("\n");
                } else {
                    errors.append(computeTriggerError((String) trigger));
                }
            }
        } else {
            errors.append(TRANSLATOR.getMessage("error.expectedList"));
        }
        return errors.toString();
    }

    private String computeListError(final ASTValue value,
                                    final Predicate<String> predicate,
                                    final String strType,
                                    final boolean canBeRange) {
        final StringBuilder errors = new StringBuilder();
        if (value instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(value);
            if (!constantError.isEmpty()) {
                return constantError + "\n";
            }
            final Constant constant = memory.getConstant(((ASTConstantValue) value).getContent());
            if (!canBeRange && constant instanceof RangeConstant) {
                errors.append(TRANSLATOR.getMessage("error.badMassiveType", strType, "range")).append('\n');
            }
            if (constant instanceof ListConstant) {
                for (Object item : ((ListConstant<?>) constant).getValues()) {
                    if (item instanceof ASTNumberOffset) {
                        item = getOffsetValue((ASTNumberOffset) item);
                    }
                    if (!predicate.test(item.toString())) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", strType, item)).append('\n');
                    }
                }
            }
        }
        return errors.toString();
    }

    private String getOffsetValue(final ASTNumberOffset offset) {
        final ASTNumber number;
        if (offset instanceof ASTRightShift) {
            number = ((ASTRightShift) offset).getContent();
        } else if (offset instanceof ASTLeftShift) {
            number = ((ASTLeftShift) offset).getContent();
        } else {
            number = (ASTNumber) offset;
        }
        if (number instanceof ASTDoubleValue) {
            return valueOf(((ASTDoubleValue) number).getContent());
        }
        if (number instanceof ASTIntegerValue) {
            return valueOf(((ASTIntegerValue) number).getContent());
        }
        return "";
    }

    private String computeValueError(final ASTValue value, final Predicate<String> predicate, final String strType) {
        String content = null;
        if (value instanceof ASTDoubleValue) {
            content = valueOf(((ASTDoubleValue) value).getContent());
        }
        if (value instanceof ASTIntegerValue) {
            content = valueOf(((ASTIntegerValue) value).getContent());
        }
        if (value instanceof ASTStringValue) {
            content = ((ASTStringValue) value).getContent();
        }
        if (!predicate.test(content)) {
            return TRANSLATOR.getMessage("error.badvalue", strType, content) + '\n';
        }
        return "";
    }

    private String computeGroundspeedError(final ASTValue groundspeed) {
        final StringBuilder errors = new StringBuilder();
        final String groundspeedStr = "GROUNDSPEED";
        if (groundspeed instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(groundspeed);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) groundspeed).getContent());
                if (constant instanceof RangeConstant) {
                    final Object start = ((RangeConstant<?>) constant).getStart();
                    final Object end = ((RangeConstant<?>) constant).getEnd();
                    if (!isSpeedValid(start.toString())) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", groundspeedStr, start));
                    }
                    if (!isSpeedValid(end.toString())) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", groundspeedStr, end));
                    }
                }
                if (constant instanceof ListConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", groundspeedStr, "list"));
                }
            } else {
                return constantError + '\n';
            }
        } else {
            String content = null;
            if (groundspeed instanceof ASTDoubleValue) {
                content = valueOf(((ASTDoubleValue) groundspeed).getContent());
            }
            if (groundspeed instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) groundspeed).getContent());
            }
            if (groundspeed instanceof ASTStringValue) {
                content = ((ASTStringValue) groundspeed).getContent();
            }
            if (!isSpeedValid(content)) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", groundspeedStr, content))
                        .append('\n');
            }
        }
        return errors.toString();
    }

    private String computeAltitudeError(final ASTValue altitude) {
        final StringBuilder errors = new StringBuilder();
        final String altitudeStr = "ALTITUDE";
        if (altitude instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(altitude);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) altitude).getContent());
                if (constant instanceof RangeConstant) {
                    final Object start = ((RangeConstant<?>) constant).getStart();
                    final Object end = ((RangeConstant<?>) constant).getEnd();
                    if (!isNumberValid(start.toString())) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", altitudeStr, start));
                    }
                    if (!isNumberValid(end.toString())) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", altitudeStr, end));
                    }
                }
                if (constant instanceof ListConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", altitudeStr, "list"));
                }
            } else {
                return constantError + '\n';
            }
        } else {
            String content = null;
            if (altitude instanceof ASTDoubleValue) {
                content = valueOf(((ASTDoubleValue) altitude).getContent());
            }
            if (altitude instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) altitude).getContent());
            }
            if (altitude instanceof ASTStringValue) {
                content = ((ASTStringValue) altitude).getContent();
            }
            if (!isNumberValid(content)) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", altitudeStr, content))
                        .append('\n');
            }
        }
        return errors.toString();
    }

    private String computeLongitudeError(final ASTValue longitude) {
        final StringBuilder errors = new StringBuilder();
        final String longitudeStr = "LONGITUDE";
        if (longitude instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(longitude);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) longitude).getContent());
                if (constant instanceof RangeConstant) {
                    final Object start = ((RangeConstant<?>) constant).getStart();
                    final Object end = ((RangeConstant<?>) constant).getEnd();
                    if (!(isNumberValid(start.toString()) && isValidLatitude(parseDouble(start.toString())))) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", longitudeStr, start));
                    }
                    if (!(isNumberValid(end.toString()) && isValidLatitude(parseDouble(end.toString())))) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", longitudeStr, end));
                    }
                }
                if (constant instanceof ListConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", longitudeStr, "list"));
                }
            } else {
                return constantError + '\n';
            }
        } else {
            String content = null;
            if (longitude instanceof ASTDoubleValue) {
                content = valueOf(((ASTDoubleValue) longitude).getContent());
            }
            if (longitude instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) longitude).getContent());
            }
            if (longitude instanceof ASTStringValue) {
                content = ((ASTStringValue) longitude).getContent();
            }
            if (content != null && !(isNumberValid(content) && isValidLatitude(parseDouble(content)))) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", longitudeStr, content))
                        .append('\n');
            }
        }
        return errors.toString();
    }

    private String computeLatitudeError(final ASTValue latitude) {
        final StringBuilder errors = new StringBuilder();
        final String latitudeStr = "LATITUDE";
        if (latitude instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(latitude);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) latitude).getContent());
                if (constant instanceof RangeConstant) {
                    final Object start = ((RangeConstant<?>) constant).getStart();
                    final Object end = ((RangeConstant<?>) constant).getEnd();
                    if (!(isNumberValid(start.toString()) && isValidLongitude(parseDouble(start.toString())))) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", latitudeStr, start));
                    }
                    if (!(isNumberValid(end.toString()) && isValidLongitude(parseDouble(end.toString())))) {
                        errors.append(TRANSLATOR.getMessage("error.badvalue", latitudeStr, end));
                    }
                }
                if (constant instanceof ListConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", latitudeStr, "list"));
                }
            } else {
                return constantError + '\n';
            }
        } else {
            String content = null;
            if (latitude instanceof ASTDoubleValue) {
                content = valueOf(((ASTDoubleValue) latitude).getContent());
            } else if (latitude instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) latitude).getContent());
            } else if (latitude instanceof ASTStringValue) {
                content = ((ASTStringValue) latitude).getContent();
            }
            if (content != null && !(isNumberValid(content) && isValidLongitude(parseDouble(content)))) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", latitudeStr, content))
                        .append('\n');
            }
        }
        return errors.toString();
    }

    private String computeCallsignError(final ASTValue callsign) {
        final StringBuilder errors = new StringBuilder();
        final String callsignStr = "CALLSIGN";
        if (callsign instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(callsign);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) callsign).getContent());
                if (constant instanceof RangeConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", callsignStr, "range"));
                }
                if (constant instanceof ListConstant) {
                    for (final Object value : ((ListConstant<?>) constant).getValues()) {
                        if (!isCallSignValid(value.toString())) {
                            errors.append(TRANSLATOR.getMessage("error.badvalue", callsignStr, value))
                                    .append('\n');
                        }
                    }
                }
            } else {
                return constantError + '\n';
            }
        } else {
            if (callsign instanceof ASTDoubleValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", callsignStr, "float"))
                        .append('\n');
            }
            if (callsign instanceof ASTIntegerValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", callsignStr, "integer"))
                        .append('\n');
            }
            if (callsign instanceof ASTStringValue) {
                if (!isCallSignValid(((ASTStringValue) callsign).getContent())) {
                    errors.append(TRANSLATOR.getMessage("error.badvalue", callsignStr,
                            ((ASTStringValue) callsign).getContent()))
                            .append('\n');
                }
            }
        }
        return errors.toString();
    }

    private String computeSquawkError(final ASTValue squawk) {
        final StringBuilder errors = new StringBuilder();
        final String squawkStr = "SQUAWK";
        if (squawk instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(squawk);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) squawk).getContent());
                if (constant instanceof RangeConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", squawkStr, "range"));
                }
                if (constant instanceof ListConstant) {
                    for (final Object value : ((ListConstant<?>) constant).getValues()) {
                        if (!isSquawkValid(value.toString())) {
                            errors.append(TRANSLATOR.getMessage("error.badvalue", squawkStr, value))
                                    .append('\n');
                        }
                    }
                }
            } else {
                return constantError + '\n';
            }
        } else {
            if (squawk instanceof ASTDoubleValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", squawkStr, "integer"))
                        .append('\n');
            }
            String content = null;
            if (squawk instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) squawk).getContent());
            }
            if (squawk instanceof ASTStringValue) {
                content = ((ASTStringValue) squawk).getContent();
            }
            if (!isSquawkValid(content)) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", squawkStr, content))
                        .append('\n');
            }
        }
        return errors.toString();
    }

    private String computeIntError(final ASTValue integer, final String name) {
        final StringBuilder errors = new StringBuilder();
        if (integer instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(integer);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) integer).getContent());
                if (constant instanceof RangeConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", name, "range"));
                }
                if (constant instanceof ListConstant) {
                    for (final Object value : ((ListConstant<?>) constant).getValues()) {
                        if (!isIntValid(value.toString())) {
                            errors.append(TRANSLATOR.getMessage("error.badvalue", name, value)).append('\n');
                        }
                    }
                }
            } else {
                return constantError + '\n';
            }
        } else {
            if (integer instanceof ASTDoubleValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", name, "integer")).append('\n');
            }
            String content = null;
            if (integer instanceof ASTIntegerValue) {
                content = valueOf(((ASTIntegerValue) integer).getContent());
            }
            if (integer instanceof ASTStringValue) {
                content = ((ASTStringValue) integer).getContent();
            }
            if (!isIntValid(content)) {
                errors.append(TRANSLATOR.getMessage("error.badvalue", name, content)).append('\n');
            }
        }
        return errors.toString();
    }

    private String computeIcaoError(final ASTValue icao) {
        final StringBuilder errors = new StringBuilder();
        final String icaoStr = "ICAO";
        if (icao instanceof ASTConstantValue) {
            final String constantError = (String) doSwitch(icao);
            if (constantError.isEmpty()) {
                final Constant constant = memory.getConstant(((ASTConstantValue) icao).getContent());
                if (constant instanceof RangeConstant) {
                    errors.append(TRANSLATOR.getMessage("error.badMassiveType", icaoStr, "range"));
                }
                if (constant instanceof ListConstant) {
                    for (final Object value : ((ListConstant<?>) constant).getValues()) {
                        if (!isIcaoValid(value.toString())) {
                            errors.append(TRANSLATOR.getMessage("error.badvalue", icaoStr, value))
                                    .append('\n');
                        }
                    }
                }
            } else {
                return constantError + '\n';
            }
        } else {
            if (icao instanceof ASTDoubleValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", icaoStr, "float"))
                        .append('\n');
            }
            if (icao instanceof ASTIntegerValue) {
                errors.append(TRANSLATOR.getMessage("error.badtype", icaoStr, "integer"))
                        .append('\n');
            }
            if (icao instanceof ASTStringValue) {
                if (!isIcaoValid(((ASTStringValue) icao).getContent())) {
                    errors.append(TRANSLATOR.getMessage("error.badvalue", icaoStr,
                            ((ASTStringValue) icao).getContent()))
                            .append('\n');
                }
            }
        }
        return errors.toString();
    }

    private static String computeEmergencyError(final ASTValue emergency) {
        final String emergencyStr = "EMERGENCY";
        if (emergency instanceof ASTConstantValue) {
            return TRANSLATOR.getMessage("error.noCombine", emergencyStr);
        }
        if (emergency instanceof ASTDoubleValue) {
            return TRANSLATOR.getMessage("error.badtype", emergencyStr, "float") + '\n';
        }
        String content = null;
        if (emergency instanceof ASTIntegerValue) {
            content = valueOf(((ASTIntegerValue) emergency).getContent());
        }
        if (emergency instanceof ASTStringValue) {
            content = ((ASTStringValue) emergency).getContent();
        }
        if (isFlagValid(content)) {
            return "";
        } else {
            return TRANSLATOR.getMessage("error.badvalue", emergencyStr, content) + '\n';
        }
    }

    private static String computeSpiError(final ASTValue spi) {
        if (spi instanceof ASTConstantValue) {
            return TRANSLATOR.getMessage("error.noCombine", "SPI");
        }
        if (spi instanceof ASTDoubleValue) {
            return TRANSLATOR.getMessage("error.badtype", "SPI", "float") + '\n';
        }
        String content = null;
        if (spi instanceof ASTIntegerValue) {
            content = valueOf(((ASTIntegerValue) spi).getContent());
        }
        if (spi instanceof ASTStringValue) {
            content = ((ASTStringValue) spi).getContent();
        }
        if (isFlagValid(content)) {
            return "";
        } else {
            return TRANSLATOR.getMessage("error.badvalue", "SPI", content) + '\n';
        }
    }

    private static String computeTrackError(final ASTValue track) {
        if (track instanceof ASTConstantValue) {
            return TRANSLATOR.getMessage("error.noCombine", "TRACK");
        }
        String content = null;
        if (track instanceof ASTDoubleValue) {
            content = valueOf(((ASTDoubleValue) track).getContent());
        }
        if (track instanceof ASTIntegerValue) {
            content = valueOf(((ASTIntegerValue) track).getContent());
        }
        if (track instanceof ASTStringValue) {
            content = ((ASTStringValue) track).getContent();
        }
        if (isSpeedValid(content)) {
            return "";
        } else {
            return TRANSLATOR.getMessage("error.badvalue", "TRACK", content) + '\n';
        }
    }

    Memory getMemory() {
        return memory;
    }

    BooleanProperty convertibleProperty() {
        return convertible;
    }
}