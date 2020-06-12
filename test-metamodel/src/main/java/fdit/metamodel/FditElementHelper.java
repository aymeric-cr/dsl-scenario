package fdit.metamodel;

import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.scenario.Scenario;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.testTools.Saver;

import java.util.UUID;
import java.util.function.Predicate;

import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.tools.predicate.PredicateUtils.and;
import static java.lang.String.valueOf;
import static java.util.UUID.fromString;

public final class FditElementHelper {

    private FditElementHelper() {
    }

    public static UUID defaultIdForTest() {
        return fromString("1-1-1-1-1");
    }

    public static UUID generateIdForTest(final int id) {
        final String idContent = valueOf(id);
        return fromString(idContent + '-' + idContent + '-' + idContent + '-' + idContent + '-' + idContent);
    }

    @SafeVarargs
    public static Predicate<Root> aRoot(final Predicate<? super FditElement>... children) {
        return root -> {
            if (containsOnly(children).test(root.getChildren())) {
                return true;
            } else {
                System.err.println("Unexpected root's children: " +
                        root.getChildren().size() + " instead of " + children.length + ".");
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<Root> aRoot(final String name, final Predicate<? super FditElement>... children) {
        return root -> {
            if (root.getName().equals(name)) {
                if (containsOnly(children).test(root.getChildren())) {
                    return true;
                } else {
                    System.err.println("Unexpected root's children: " +
                            root.getChildren().size() + " instead of " + children.length + ".");
                    return false;
                }
            } else {
                System.err.println("Root name : " + root.getName() + " different from : " + name);
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<? super FditElement> aDirectory(final String name,
                                                            final Predicate<? super FditElement>... children) {
        return dir -> {
            if (dir.getName().equals(name)) {
                return containsOnly(children).test(((Directory) dir).getChildren());
            } else {
                System.err.println("Directory name : " + dir.getName() + " different from : " + name);
                return false;
            }
        };
    }

    public static Predicate<? super FditElement> aRecording(final Saver<Recording> recordingSaver) {
        return recording -> {
            if (recordingSaver.get() == recording) {
                return true;
            } else {
                System.err.println("Expected recording:" + recording + ". Got: " + recordingSaver.get());
                return false;
            }
        };
    }

    public static Predicate<? super FditElement> aRecording(final String name) {
        return recording -> {
            if (recording.getName().equals(name)) {
                return true;
            } else {
                System.err.println("Expected recording name:" + recording.getName() + ". Got: " + name);
                return false;
            }
        };
    }

    public static Coordinates coordinates(final double latitude, final double longitude) {
        return new Coordinates(latitude, longitude);
    }

    @SafeVarargs
    public static Predicate<? super FditElement> aPolygon(final Predicate<? super FditPolygon>... constraints) {
        return fditElement -> and(constraints).test((FditPolygon) fditElement);
    }

    public static Predicate<? super Zone> withZoneName(final String name) {
        return fdit -> {
            if (fdit.getName().equals(name)) {
                return true;
            } else {
                System.err.println("Expected zone name : " + fdit.getName() + ". Got: " + name);
                return false;
            }
        };
    }

    public static Predicate<? super Zone> withId(final UUID uuid) {
        return zone -> {
            if (zone.getId().equals(uuid)) {
                return true;
            } else {
                System.err.println("Zone ID : " + zone.getId() + " different from " + uuid);
                return false;
            }
        };
    }

    public static Predicate<? super Zone> withLowerAltitude(final double lowerAltitude) {
        return fditPolygon -> {
            if (fditPolygon.getAltitudeLowerBound() == lowerAltitude) {
                return true;
            } else {
                System.err.println("Polygon lower altitude : " + fditPolygon.getAltitudeLowerBound() +
                        " different from " + lowerAltitude);
                return false;
            }
        };
    }

    public static Predicate<? super Zone> withUpperAltitude(final double upperAltitude) {
        return fditPolygon -> {
            if (fditPolygon.getAltitudeUpperBound() == upperAltitude) {
                return true;
            } else {
                System.err.println("Polygon upper altitude : " + fditPolygon.getAltitudeUpperBound() +
                        " different from " + upperAltitude);
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<? super FditPolygon> withVertices(final Predicate<Coordinates>... vertices) {
        return fditPolygon -> containsOnly(vertices).test(fditPolygon.getVertices());
    }

    public static Predicate<Coordinates> aVertex(final double latitude,
                                                 final double longitude) {
        return coordinates -> {
            if (coordinates.getLatitude() == latitude && coordinates.getLongitude() == longitude) {
                return true;
            } else {
                System.err.println("Vertex (" + coordinates.getLatitude() + ',' + coordinates.getLongitude() + ')' +
                        " different from : Vertex (" + latitude + ',' + longitude + ')');
                return false;
            }
        };
    }

    @SafeVarargs
    public static Predicate<? super FditElement> aScenario(final String name,
                                                           final String content,
                                                           final String description,
                                                           final Predicate<Scenario>... constraints) {
        return fditElement -> {
            if (!fditElement.getName().equals(name)) {
                System.err.println("Expected scenario name: " + name + ". Got: " + fditElement.getName());
                return false;
            }
            if (!((Scenario) fditElement).getDescription().equals(description)) {
                System.err.println("Expected scenario description: " + description +
                        ". Got: " + ((Scenario) fditElement).getDescription());
                return false;
            }
            if (!((Scenario) fditElement).getContent().equals(content)) {
                System.err.println("Expected scenario content: " + content +
                        ". Got: " + ((Scenario) fditElement).getContent());
                return false;
            }
            return and(constraints).test((Scenario) fditElement);
        };
    }

    @SafeVarargs
    public static Predicate<? super FditElement> anLTLFilter(final String name,
                                                             final String content,
                                                             final String description,
                                                             final Predicate<LTLFilter>... constraints) {
        return fditElement -> {
            if (!fditElement.getName().equals(name)) {
                System.err.println("Expected scenario name: " + name + ". Got: " + fditElement.getName());
                return false;
            }
            if (!((LTLFilter) fditElement).getDescription().equals(description)) {
                System.err.println("Expected scenario description: " + description +
                        ". Got: " + ((LTLFilter) fditElement).getDescription());
                return false;
            }
            if (!((LTLFilter) fditElement).getContent().equals(content)) {
                System.err.println("Expected scenario content: " + content + ". Got: " + fditElement.getName());
                return false;
            }
            return and(constraints).test((LTLFilter) fditElement);
        };
    }

    @SafeVarargs
    public static Predicate<? super FditElement> anActionTrigger(final String name,
                                                                 final String content,
                                                                 final String description,
                                                                 final Predicate<ActionTrigger>... constraints) {
        return fditElement -> {
            if (!fditElement.getName().equals(name)) {
                System.err.println("Expected trigger name: " + name + ". Got: " + fditElement.getName());
                return false;
            }
            if (!((ActionTrigger) fditElement).getDescription().equals(description)) {
                System.err.println("Expected trigger description: " + description +
                        ". Got: " + ((ActionTrigger) fditElement).getDescription());
                return false;
            }
            if (!((ActionTrigger) fditElement).getContent().equals(content)) {
                System.err.println("Expected trigger content: " + content + ". Got: " + fditElement.getName());
                return false;
            }
            return and(constraints).test((ActionTrigger) fditElement);
        };
    }
}