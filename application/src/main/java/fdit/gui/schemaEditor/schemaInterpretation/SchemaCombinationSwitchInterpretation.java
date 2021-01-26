package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.ide.AttackScenarioFacade;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Constant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.ListConstant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Memory;
import fdit.gui.schemaEditor.schemaInterpretation.memory.RangeConstant;
import fdit.metamodel.schema.Schema;
import javafx.util.Pair;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.valueOf;
import static org.apache.commons.collections4.IterableUtils.get;

public class SchemaCombinationSwitchInterpretation extends AttackScenarioSwitch<Object> {

    private static final String DOLLARS_REGEX = "\\$";

    private final Memory memory;
    private Schema schema;
    private final AttackScenarioFacade attackScenarioFacade;
    private final Map<Constant, Integer> occurences = newHashMap();

    public SchemaCombinationSwitchInterpretation(final AttackScenarioFacade attackScenarioFacade)
            throws NoSuchAlgorithmException {
        this.attackScenarioFacade = attackScenarioFacade;
        memory = new Memory();
    }

    public List<Schema> processCombination(final Schema schema) {
        memory.clear();
        this.schema = schema;
        try {
            attackScenarioFacade.parse(schema.getContent());
        } catch (IOException e) {
            return newArrayList();
        }
        return (List<Schema>) doSwitch(get(attackScenarioFacade.getAST(), 0));
    }

    private static String[][] allUniqueCombinations(final LinkedHashMap<String, Vector<String>> dataStructure) {
        int n = dataStructure.keySet().size();
        int solutions = 1;

        for (Vector<String> vector : dataStructure.values()) {
            solutions *= vector.size();
        }

        String[][] allCombinations = new String[solutions + 1][];
        allCombinations[0] = dataStructure.keySet().toArray(new String[n]);

        for (int i = 0; i < solutions; i++) {
            Vector<String> combination = new Vector<>(n);
            int j = 1;
            for (Vector<String> vec : dataStructure.values()) {
                combination.add(vec.get((i / j) % vec.size()));
                j *= vec.size();
            }
            allCombinations[i + 1] = combination.toArray(new String[n]);
        }
        return allCombinations;
    }

    @Override
    public Object caseASTScenario(final ASTScenario object) {
        object.getDeclarations().forEach(astDeclaration -> memory.addConstant((Constant) doSwitch(astDeclaration)));
        object.getInstructions().forEach(this::doSwitch);
        final String[][] result = allUniqueCombinations(formatMemoryToCombination());
        if (result.length > 0 && result[0].length == occurences.values().stream().reduce(0, Integer::sum)) {
            final List<Schema> schemas = newArrayList();
            final String[] names = result[0];
            for (int i = 1; i < result.length; i++) {
                String content = getInstructionsText(object);
                for (int j = 0; j < result[i].length; j++) {
                    content = content.replace(names[j], result[i][j]);
                }
                final Schema generatedSchema
                        = new Schema(this.schema.getName(), this.schema.getDescription(), content.trim());
                generatedSchema.setRecording(this.schema.getRecording());
                generatedSchema.setFather(this.schema.getFather());
                schemas.add(generatedSchema);
            }
            return schemas;
        }
        throw new RuntimeException("Error occurred during parameters combination");
    }

    @Override
    public Object caseASTTarget(final ASTTarget target) {
        if (target.getFilters() != null) {
            doSwitch(target.getFilters());
        }
        return null;
    }

    @Override
    public Object caseASTReplayTarget(final ASTReplayTarget replayTarget) {
        if (replayTarget.getFilters() != null) {
            doSwitch(replayTarget.getFilters());
        }
        doSwitch(replayTarget.getRecording());
        return null;
    }

    @Override
    public Object caseASTWayPoints(final ASTWayPoints wayPoints) {
        wayPoints.getWaypoints().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTWayPoint(final ASTWayPoint wayPoint) {
        doSwitch(wayPoint.getTime());
        doSwitch(wayPoint.getLatitude());
        doSwitch(wayPoint.getLongitude());
        doSwitch(wayPoint.getAltitude());
        return null;
    }

    @Override
    public Object caseASTParameters(final ASTParameters parameters) {
        parameters.getItems().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTDelayParameter(final ASTDelayParameter delayParameter) {
        doSwitch(delayParameter.getDelay());
        return null;
    }

    @Override
    public Object caseASTParameter(final ASTParameter parameter) {
        doSwitch(parameter.getValue());
        return null;
    }

    @Override
    public Object caseASTSaturationParameters(final ASTSaturationParameters saturationParameters) {
        saturationParameters.getItems().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTSaturationParameter(final ASTSaturationParameter saturationParameter) {
        doSwitch(saturationParameter.getValue());
        return null;
    }

    @Override
    public Object caseASTCreationParameters(final ASTCreationParameters creationParameters) {
        creationParameters.getItems().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTCreationParameter(final ASTCreationParameter creationParameter) {
        doSwitch(creationParameter.getValue());
        return null;
    }

    @Override
    public Object caseASTHideParameters(final ASTHideParameters hideParameters) {
        hideParameters.getItems().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTHideParameter(final ASTHideParameter hideParameter) {
        doSwitch(hideParameter.getValue());
        return null;
    }

    @Override
    public Object caseASTTrigger(final ASTTrigger trigger) {
        doSwitch(trigger.getTriggername());
        return null;
    }

    @Override
    public Object caseASTTime(final ASTTime time) {
        doSwitch(time.getRealTime());
        return null;
    }

    @Override
    public Object caseASTFilters(final ASTFilters filters) {
        filters.getFilters().forEach(this::doSwitch);
        return null;
    }

    @Override
    public Object caseASTHide(final ASTHide hide) {
        if (hide.getParameters() != null) {
            doSwitch(hide.getParameters());
        }
        doSwitch(hide.getTimeScope());
        if (hide.getTrigger() != null) {
            doSwitch(hide.getTrigger());
        }
        doSwitch(hide.getTarget());
        return null;
    }

    @Override
    public Object caseASTCreate(final ASTCreate create) {
        doSwitch(create.getParameters());
        doSwitch(create.getTimeScope());
        doSwitch(create.getTrajectory());
        return null;
    }

    @Override
    public Object caseASTAlter(final ASTAlter alter) {
        doSwitch(alter.getParameters());
        doSwitch(alter.getTimeScope());
        if (alter.getTrigger() != null) {
            doSwitch(alter.getTrigger());
        }
        doSwitch(alter.getTarget());
        return null;
    }

    @Override
    public Object caseASTTrajectory(final ASTTrajectory trajectory) {
        doSwitch(trajectory.getTimeScope());
        if (trajectory.getTrigger() != null) {
            doSwitch(trajectory.getTrigger());
        }
        doSwitch(trajectory.getTarget());
        doSwitch(trajectory.getTrajectory());
        return null;
    }

    @Override
    public Object caseASTSaturate(final ASTSaturate saturate) {
        doSwitch(saturate.getParameters());
        doSwitch(saturate.getTimeScope());
        if (saturate.getTrigger() != null) {
            doSwitch(saturate.getTrigger());
        }
        doSwitch(saturate.getTarget());
        return null;
    }

    @Override
    public Object caseASTReplay(final ASTReplay replay) {
        doSwitch(replay.getParameters());
        doSwitch(replay.getTimeScope());
        doSwitch(replay.getTarget());
        return null;
    }

    @Override
    public Object caseASTDelay(final ASTDelay delay) {
        doSwitch(delay.getTimeScope());
        doSwitch(delay.getTarget());
        doSwitch(delay.getDelay());
        return null;
    }

    @Override
    public Object caseASTPlane(final ASTPlane plane) {
        if (plane.getFilters() != null) {
            doSwitch(plane.getFilters());
        }
        return null;
    }

    @Override
    public Object caseASTAllPlanes(final ASTAllPlanes allPlanes) {
        if (allPlanes.getFilters() != null) {
            doSwitch(allPlanes.getFilters());
        }
        return null;
    }

    @Override
    public Object caseASTPlaneFrom(final ASTPlaneFrom planeFrom) {
        if (planeFrom.getFilters() != null) {
            doSwitch(planeFrom.getFilters());
        }
        doSwitch(planeFrom.getRecording());
        return null;
    }

    @Override
    public Object caseASTAllPlaneFrom(final ASTAllPlaneFrom allPlaneFrom) {
        if (allPlaneFrom.getFilters() != null) {
            doSwitch(allPlaneFrom.getFilters());
        }
        doSwitch(allPlaneFrom.getRecording());
        return null;
    }

    @Override
    public Object caseASTParamNoise(final ASTParamNoise paramNoise) {
        doSwitch(paramNoise.getValue());
        return null;
    }

    @Override
    public Object caseASTParamDrift(final ASTParamDrift paramDrift) {
        doSwitch(paramDrift.getValue());
        return null;
    }

    @Override
    public Object caseASTAt(final ASTAt at) {
        doSwitch(at.getTime());
        return null;
    }

    @Override
    public Object caseASTWindow(final ASTWindow window) {
        doSwitch(window.getStart());
        doSwitch(window.getEnd());
        return null;
    }

    @Override
    public Object caseASTConstantValue(final ASTConstantValue constantValue) {
        final Constant constant = memory.getConstant(constantValue.getContent());
        if (occurences.containsKey(constant)) {
            occurences.put(constant, occurences.get(constant) + 1);
        } else {
            occurences.put(constant, 1);
        }
        constantValue.setContent(constantValue.getContent() + "_" + occurences.get(constant));
        return memory.getConstant(constantValue.getContent());
    }

    @Override
    public Object caseASTVariableValue(final ASTVariableValue variableValue) {
        final Constant constant = memory.getConstant(variableValue.getContent());
        if (occurences.containsKey(constant)) {
            occurences.put(constant, occurences.get(constant) + 1);
        } else {
            occurences.put(constant, 1);
        }
        return null;
    }

    private LinkedHashMap<String, Vector<String>> formatMemoryToCombination() {
        final LinkedHashMap<String, Vector<String>> data = new LinkedHashMap<>();
        for (final Constant constant : memory.getConstants()) {
            final String name = constant.getName();
            final Vector<String> vector = new Vector<>();
            if (constant instanceof RangeConstant) {
                final RangeConstant rangeConstant = (RangeConstant) constant;
                new RangeConstant.RangeConstantTypeSwitch<Void>() {

                    @Override
                    public Void visitInteger(final RangeConstant rangeConstant) {
                        vector.add(valueOf((Integer) rangeConstant.getStart() - 1));
                        vector.add(valueOf((Integer) rangeConstant.getStart() + 1));
                        vector.add(valueOf((Integer) rangeConstant.getEnd() - 1));
                        vector.add(valueOf((Integer) rangeConstant.getEnd() + 1));
                        return null;
                    }

                    @Override
                    public Void visitDouble(final RangeConstant rangeConstant) {
                        vector.add(valueOf((Double) rangeConstant.getStart() - 1.0));
                        vector.add(valueOf((Double) rangeConstant.getStart() + 1.0));
                        vector.add(valueOf((Double) rangeConstant.getEnd() - 1.0));
                        vector.add(valueOf((Double) rangeConstant.getEnd() + 1.0));
                        return null;
                    }

                    @Override
                    public Void visitFloat(final RangeConstant rangeConstant) {
                        vector.add(valueOf((Float) rangeConstant.getStart() - 1.0f));
                        vector.add(valueOf((Float) rangeConstant.getStart() + 1.0f));
                        vector.add(valueOf((Float) rangeConstant.getEnd() - 1.0f));
                        vector.add(valueOf((Float) rangeConstant.getEnd() + 1.0f));
                        return null;
                    }
                }.doSwitch(rangeConstant);
            }
            if (constant instanceof ListConstant) {
                final ListConstant<?> listConstant = (ListConstant<?>) constant;
                new ListConstant.ListConstantTypeSwitch<Void>() {
                    @Override
                    public Void visitInteger(final ListConstant<Integer> listConstant) {
                        for (final Integer value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitDouble(final ListConstant<Double> listConstant) {
                        for (final Double value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitFloat(final ListConstant<Float> listConstant) {
                        for (final Float value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitString(final ListConstant<String> listConstant) {
                        for (final String value : listConstant.getValues()) {
                            vector.add('\"' + valueOf(value) + '\"');
                        }
                        return null;
                    }

                    @Override
                    public Void visitOffset(ListConstant<ASTNumberOffset> listConstant) {
                        for (final ASTNumberOffset offset : listConstant.getValues()) {
                            vector.add(numberOffsetToString(offset));
                        }
                        return null;
                    }

                    @Override
                    public Void visitRecordingValue(ListConstant<ASTRecordingValue> listConstant) {
                        for (final ASTRecordingValue recval : listConstant.getValues()) {
                            vector.add(recvalToString(recval));
                        }
                        return null;
                    }
                }.doSwitch(listConstant);
            }
            if (occurences.containsKey(constant)) {
                for (int i = 1; i <= occurences.get(constant); i++) {
                    data.put(name.replaceAll(DOLLARS_REGEX, DOLLARS_REGEX + "_") + "_" + i, vector);
                }
            }
        }
        return data;
    }

    @Override
    public Object caseASTDoubleRange(final ASTDoubleRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTIntegerRange(final ASTIntegerRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTStringList(final ASTStringList object) {
        final List<String> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTOffsetList(final ASTOffsetList object) {
        final List<ASTNumberOffset> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTListDeclaration(final ASTListDeclaration object) {
        return new ListConstant(object.getConstant(), (List) doSwitch(object.getList()));
    }

    @Override
    public Object caseASTRangeDeclaration(final ASTRangeDeclaration object) {
        final Pair value = (Pair) doSwitch(object.getRange());
        return new RangeConstant(object.getConstant(), value.getKey(), value.getValue());
    }

    @Override
    public Object caseASTStringValue(final ASTStringValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTIntegerValue(final ASTIntegerValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTDoubleValue(final ASTDoubleValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTLeftShift(final ASTLeftShift object) {
        return "<<" + doSwitch(object.getContent());
    }

    @Override
    public Object caseASTRightShift(final ASTRightShift object) {
        return ">>" + doSwitch(object.getContent());
    }

    @Override
    public Object caseASTRecordingValue(final ASTRecordingValue object) {
        return object.getRatio() + " * " + object.getContent();
    }

    private String numberOffsetToString(final ASTNumberOffset number) {
        return valueOf(doSwitch(number));
    }

    private String recvalToString(final ASTRecordingValue recval) {
        return valueOf(doSwitch(recval));
    }

    private String getInstructionsText(final ASTScenario astScenario) {
        final StringBuilder contentBuilder = new StringBuilder();
        for (final EObject dring : astScenario.getInstructions()) {
            contentBuilder.append(NodeModelUtils.findActualNodeFor(dring).getText()).append('\n');
        }
        String content = contentBuilder.toString();
        for (final Constant constant : memory.getConstants()) {
            if (occurences.containsKey(constant)) {
                int cpt = 0;
                final String replacement = constant.getName().replaceFirst(DOLLARS_REGEX, DOLLARS_REGEX + "_");
                final Matcher matcher = Pattern.compile(("\\") + constant.getName()).matcher(content);
                while (matcher.find()) {
                    content = content.replaceFirst(("\\") + constant.getName(), ("\\") + replacement + "_" + ++cpt);
                }
            }
        }
        return content;
    }
}