package fdit.gui.schemaEditor.schemaInterpretation.memory;

import fdit.dsl.attackScenario.ASTNumberOffset;
import fdit.dsl.attackScenario.ASTRecordingValue;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class ListConstant<T> extends Constant {

    private final List<T> values = newArrayList();

    public ListConstant(final String name, final List<T> values) {
        super(name);
        if (values.isEmpty()) {
            throw new RuntimeException("Constant list cannot be empty");
        }
        if (values.stream().allMatch(t -> t instanceof String) ||
                values.stream().allMatch(t -> t instanceof Integer) ||
                values.stream().allMatch(t -> t instanceof Double) ||
                values.stream().allMatch(t -> t instanceof ASTNumberOffset) ||
                values.stream().allMatch(t -> t instanceof ASTRecordingValue) ||
                values.stream().allMatch(t -> t instanceof Float)) {
            this.values.addAll(values);
        } else {
            throw new RuntimeException("Unauthorized type for a constant list");
        }
    }

    public List<T> getValues() {
        return values;
    }

    public interface ListConstantTypeSwitch<T> {

        T visitInteger(final ListConstant<Integer> listConstant);

        T visitDouble(final ListConstant<Double> listConstant);

        T visitFloat(final ListConstant<Float> listConstant);

        T visitString(final ListConstant<String> listConstant);

        T visitOffset(final ListConstant<ASTNumberOffset> listConstant);

        T visitRecordingValue(final ListConstant<ASTRecordingValue> listConstant);

        default T visitDefault() {
            throw new RuntimeException("Empty list or unknown type");
        }

        default T doSwitch(final ListConstant<?> listConstant) {
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Integer) {
                return visitInteger((ListConstant<Integer>) listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Double) {
                return visitDouble((ListConstant<Double>) listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Float) {
                return visitFloat((ListConstant<Float>) listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof String) {
                return visitString((ListConstant<String>) listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof ASTNumberOffset) {
                return visitOffset((ListConstant<ASTNumberOffset>) listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof ASTRecordingValue) {
                return visitRecordingValue((ListConstant<ASTRecordingValue>) listConstant);
            }
            return visitDefault();
        }
    }
}