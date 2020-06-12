package fdit.metamodel.alteration.scope;

public interface Scope {

    interface ScopeVisitor<T> {
        T visitGeoArea(final GeoArea geoArea);

        T visitGeoThreshold(final GeoThreshold geoThreshold);

        T visitGeoTime(final GeoTime geoTime);

        T visitGeoTimeWindow(final GeoTimeWindow geoTimeWindow);

        T visitTimeWindow(final TimeWindow timeWindow);

        T visitTrigger(final Trigger trigger);

        default T accept(final Scope scope) {
            if (scope instanceof GeoArea) {
                return visitGeoArea((GeoArea) scope);
            }
            if (scope instanceof GeoThreshold) {
                return visitGeoThreshold((GeoThreshold) scope);
            }
            if (scope instanceof GeoTime) {
                return visitGeoTime((GeoTime) scope);
            }
            if (scope instanceof GeoTimeWindow) {
                return visitGeoTimeWindow((GeoTimeWindow) scope);
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