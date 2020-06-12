package fdit.metamodel.zone;

import fdit.metamodel.element.FditElement;
import javafx.scene.shape.Shape;

import java.util.UUID;

@SuppressWarnings("AbstractClassNamingConvention")
public abstract class Zone extends FditElement {

    private final UUID id;
    protected Shape zoneShape;
    private Double altitudeLowerBound;
    private Double altitudeUpperBound;

    protected Zone(final String name,
                   final UUID id,
                   final Double altitudeLowerBound,
                   final Double altitudeUpperBound) {
        super(name);
        this.id = id;
        this.altitudeLowerBound = altitudeLowerBound;
        this.altitudeUpperBound = altitudeUpperBound;
    }

    protected Zone(final String name, final Zone otherZone) {
        super(name);
        id = otherZone.id;
        altitudeLowerBound = otherZone.altitudeLowerBound;
        altitudeUpperBound = otherZone.altitudeUpperBound;
    }

    public UUID getId() {
        return id;
    }

    public Double getAltitudeLowerBound() {
        return altitudeLowerBound;
    }

    public void setAltitudeLowerBound(final Double altitudeLowerBound) {
        this.altitudeLowerBound = altitudeLowerBound;
    }

    public Double getAltitudeUpperBound() {
        return altitudeUpperBound;
    }

    public void setAltitudeUpperBound(final Double altitudeUpperBound) {
        this.altitudeUpperBound = altitudeUpperBound;
    }

    protected abstract void createShapeFromZone();

    public Shape getZoneShape() {
        return zoneShape;
    }

    public interface ZoneVisitor<T> {

        T visitPolygon(final FditPolygon polygon);

        default T accept(final Zone zone) {
            if (zone instanceof FditPolygon) {
                return visitPolygon((FditPolygon) zone);
            }
            throw new IllegalArgumentException("Unknown zone type");
        }
    }
}
