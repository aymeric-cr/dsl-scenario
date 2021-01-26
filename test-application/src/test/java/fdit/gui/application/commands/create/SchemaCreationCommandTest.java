package fdit.gui.application.commands.create;

import fdit.gui.application.FditTestCase;
import org.junit.Test;

import static fdit.metamodel.FditElementHelper.aRoot;
import static fdit.metamodel.FditElementHelper.aSchema;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.CollectionPredicate.isEmpty;
import static fdit.testTools.predicate.FilePredicate.*;

public class SchemaCreationCommandTest extends FditTestCase {

    @Test
    public void testSchemaCreationCommand() {
        getExecutor().execute(new SchemaCreationCommand(getRoot(), "scenario", "", EMPTY_RECORDING));
        assertThat(getRoot(), aRoot(aSchema("scenario", "", "")));
        assertThat(listFiles(getRootFile()),
                containsOnly(aFile("scenario.scenario", containsAll("<entry key=\"description\"/>"))));

        undo();
        assertThat(getRoot(), aRoot());
        assertThat(listFiles(getRootFile()), isEmpty());

        redo();
        assertThat(getRoot(), aRoot(aSchema("scenario", "", "")));
        assertThat(listFiles(getRootFile()),
                containsOnly(aFile("scenario.scenario", containsAll("<entry key=\"description\"/>"))));
    }
}