package fdit.gui.schemaEditor.schemaInterpretation.memory;

import org.apache.commons.math3.exception.NotANumberException;

public class RangeConstant<T> extends Constant {

    private final T start;
    private final T end;

    public RangeConstant(final String name, final T start, final T end) {
        super(name);
        if (start instanceof Integer || start instanceof Float || start instanceof Double) {
            this.start = start;
        } else {
            throw new RuntimeException("Unauthorized type: " + start.getClass());
        }
        if (end instanceof Integer || end instanceof Float || end instanceof Double) {
            this.end = end;
        } else {
            throw new RuntimeException("Unauthorized type: " + end.getClass());
        }
    }

    public T getStart() {
        return start;
    }

    public T getEnd() {
        return end;
    }

    public interface RangeConstantTypeSwitch<T> {

        T visitInteger(final RangeConstant rangeConstant);

        T visitDouble(final RangeConstant rangeConstant);

        T visitFloat(final RangeConstant rangeConstant);

        default T visitDefault() {
            throw new NotANumberException();
        }

        default T doSwitch(final RangeConstant rangeConstant) {
            if (rangeConstant.getStart() instanceof Integer &&
                    rangeConstant.getEnd() instanceof Integer) {
                return visitInteger(rangeConstant);
            }
            if (rangeConstant.getStart() instanceof Double &&
                    rangeConstant.getEnd() instanceof Double) {
                return visitDouble(rangeConstant);
            }
            if (rangeConstant.getStart() instanceof Float &&
                    rangeConstant.getEnd() instanceof Float) {
                return visitFloat(rangeConstant);
            }
            return visitDefault();
        }
    }
}