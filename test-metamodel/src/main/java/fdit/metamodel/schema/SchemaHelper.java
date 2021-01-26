package fdit.metamodel.schema;

import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import fdit.tools.functional.ThrowableConsumer;

import java.util.function.Predicate;

public final class SchemaHelper {

    private SchemaHelper() {
    }

    public static ThrowableConsumer<Schema> content(final String content) {
        return tscenario -> tscenario.setContent(content);
    }

    public static ThrowableConsumer<Schema> description(final String descr) {
        return scenario -> scenario.setDescription(descr);
    }

   public static ThrowableConsumer<Schema> withRecording(final Saver<Recording> saver) {
        return scenario -> scenario.setRecording(saver.get());
    }

    public static Predicate<Schema> withRecording(final Predicate<? super FditElement> predicate) {
        return scenario -> predicate.test(scenario.getRecording());
    }
}
