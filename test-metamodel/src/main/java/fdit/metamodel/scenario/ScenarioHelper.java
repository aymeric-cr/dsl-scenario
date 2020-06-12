package fdit.metamodel.scenario;

import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import fdit.tools.functional.ThrowableConsumer;

import java.util.function.Predicate;

public final class ScenarioHelper {

    private ScenarioHelper() {
    }

    public static ThrowableConsumer<Scenario> content(final String content) {
        return scenario -> scenario.setContent(content);
    }

    public static ThrowableConsumer<Scenario> description(final String description) {
        return scenario -> scenario.setDescription(description);
    }

    public static ThrowableConsumer<Scenario> withRecording(final Saver<Recording> saver) {
        return scenario -> scenario.setRecording(saver.get());
    }

    public static Predicate<Scenario> withRecording(final Predicate<? super FditElement> predicate) {
        return scenario -> predicate.test(scenario.getRecording());
    }
}
