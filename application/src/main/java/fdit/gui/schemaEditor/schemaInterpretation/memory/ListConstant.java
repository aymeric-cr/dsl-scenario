package fdit.gui.schemaEditor.schemaInterpretation.memory;

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

        T visitInteger(final ListConstant<?> listConstant);

        T visitDouble(final ListConstant<?> listConstant);

        T visitFloat(final ListConstant<?> listConstant);

        T visitString(final ListConstant<?> listConstant);

        default T visitDefault() {
            throw new RuntimeException("Empty list or unknown type");
        }

        default T doSwitch(final ListConstant<?> listConstant) {
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Integer) {
                return visitInteger(listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Double) {
                return visitDouble(listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof Float) {
                return visitFloat(listConstant);
            }
            if (!listConstant.getValues().isEmpty() && listConstant.getValues().get(0) instanceof String) {
                return visitString(listConstant);
            }
            return visitDefault();
        }
    }
}
