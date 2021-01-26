package fdit.metamodel.alteration.scope;

public interface Scope {

    interface ScopeVisitor<T> {

        T visitGeoThreshold(final GeoThreshold geoThreshold);

        T visitTimeWindow(final TimeWindow timeWindow);

        T visitTrigger(final Trigger trigger);

        default T accept(final Scope scope) {
            if (scope instanceof GeoThreshold) {
                return visitGeoThreshold((GeoThreshold) scope);
            }
            if (scope instanceof TimeWindow) {
                return visitTimeWindow((TimeWindow) scope);
            }
            if (scope instanceof Trigger) {
                return visitTrigger((Trigger) scope);
            }
            throw new RuntimeException("Unkonwn scope");
        }
    }
}