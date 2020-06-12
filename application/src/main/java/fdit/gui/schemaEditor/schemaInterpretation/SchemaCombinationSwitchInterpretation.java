package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.xtext.standalone.AttackScenarioDslFacade;
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
import java.util.Vector;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;
import static org.apache.commons.collections4.IterableUtils.get;

public class SchemaCombinationSwitchInterpretation extends AttackScenarioSwitch<Object> {

    private final Memory memory;
    private Schema schema;
    private final AttackScenarioDslFacade attackScenarioDslFacade;

    SchemaCombinationSwitchInterpretation(final AttackScenarioDslFacade attackScenarioDslFacade)
            throws NoSuchAlgorithmException {
        this.attackScenarioDslFacade = attackScenarioDslFacade;
        memory = new Memory();
    }

    public List<Schema> processCombination(final Schema schema) {
        memory.clear();
        this.schema = schema;
        try {
            attackScenarioDslFacade.parse(schema.getContent());
        } catch (IOException e) {
            return newArrayList();
        }
        return (List<Schema>) doSwitch(get(attackScenarioDslFacade.getAST(), 0));
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
        final String[][] result = allUniqueCombinations(formatMemoryToCombination());
        if (result.length > 0 && result[0].length == memory.getConstants().size()) {
            final List<Schema> schemas = newArrayList();
            final String[] names = result[0];
            for (int i = 1; i < result.length; i++) {
                String content = getInstructionsText(object);
                for (int j = 0; j < result[i].length; j++) {
                    content = content.replace(names[j], result[i][j]);
                }
                final Schema generatedSchema
                        = new Schema(schema.getName(), schema.getDescription(), content.trim());
                generatedSchema.setRecording(schema.getRecording());
                generatedSchema.setFather(schema.getFather());
                schemas.add(generatedSchema);
            }
            return schemas;
        }
        throw new RuntimeException("Error occurred during parameters combination");
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
                final ListConstant listConstant = (ListConstant) constant;
                new ListConstant.ListConstantTypeSwitch<Void>() {
                    @Override
                    public Void visitInteger(ListConstant listConstant) {
                        for (final Object value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitDouble(ListConstant listConstant) {
                        for (final Object value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitFloat(ListConstant listConstant) {
                        for (final Object value : listConstant.getValues()) {
                            vector.add(valueOf(value));
                        }
                        return null;
                    }

                    @Override
                    public Void visitString(ListConstant listConstant) {
                        for (final Object value : listConstant.getValues()) {
                            vector.add('\"' + valueOf(value) + '\"');
                        }
                        return null;
                    }
                }.doSwitch(listConstant);
            }
            data.put(name, vector);
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
    public Object caseASTIntegerList(final ASTIntegerList object) {
        final List<Integer> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTDoubleList(final ASTDoubleList object) {
        final List<Double> values = newArrayList();
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
    public Object caseASTConstantValue(final ASTConstantValue object) {
        return memory.getConstant(object.getContent());
    }

    private String getInstructionsText(final ASTScenario asscenario) {
        final StringBuilder contentBuilder = new StringBuilder();
        for (final EObject dring : asscenario.getInstructions()) {
            contentBuilder.append(NodeModelUtils.findActualNodeFor(dring).getText()).append('\n');
        }
        return contentBuilder.toString();
    }
}