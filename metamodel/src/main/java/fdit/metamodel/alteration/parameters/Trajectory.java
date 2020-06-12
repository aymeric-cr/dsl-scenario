package fdit.metamodel.alteration.parameters;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

public class Trajectory implements ActionParameter {

    private final Collection<AircraftWayPoint> wayPoints = newArrayList();

    public Trajectory(final Collection<AircraftWayPoint> wayPoints) {
        this.wayPoints.addAll(wayPoints);
    }

    public Collection<AircraftWayPoint> getWayPoints() {
        return wayPoints;
    }
}