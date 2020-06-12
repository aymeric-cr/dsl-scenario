package fdit.metamodel.alteration.parameters;

public enum Mode {
    SIMPLE,
    OFFSET,
    NOISE,
    DRIFT;

    public interface ModeSwitch<T> {
        T visitSimple();

        T visitOffset();

        T visitNoise();

        T visitDrift();

        default T doSwitch(final Mode mode) {
            switch (mode) {
                case DRIFT:
                    return visitDrift();
                case NOISE:
                    return visitNoise();
                case OFFSET:
                    return visitOffset();
                default:
                    return visitSimple();
            }
        }
    }
}