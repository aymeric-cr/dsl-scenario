package fdit.metamodel.recording;

import fdit.metamodel.aircraft.Aircraft;

import java.util.function.Predicate;

import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.tools.predicate.PredicateUtils.and;

public final class RecordingHelper {

    private RecordingHelper() {
    }

    public static Predicate<Recording> aRecording(final Predicate<Recording>... predicates) {
        return and(predicates);
    }

    public static Predicate<Recording> withAircrafts(final Predicate<Aircraft>... aircrafts) {
        return recording -> {
            if (aircrafts.length == recording.getAircrafts().size()) {
                return containsOnly(aircrafts).test(recording.getAircrafts());
            } else {
                System.err.println("Expected aircraft number: " + aircrafts.length + ". Got:" + recording.getAircrafts().size());
                return false;
            }
        };
    }

    public static Predicate<Recording> withMaxRelativeDate(final long maxRelativeDate) {
        return recording -> {
            if (recording.getMaxRelativeDate() == maxRelativeDate) {
                return true;
            } else {
                System.err.println("Expected relative date: " + maxRelativeDate + ".Got: " + recording.getMaxRelativeDate());
                return false;
            }
        };
    }
}