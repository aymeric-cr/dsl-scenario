package fdit.gui.application.commands.create;

import fdit.gui.application.FditTestCase;
import org.junit.Test;

import static fdit.metamodel.FditElementHelper.aRoot;
import static fdit.metamodel.FditElementHelper.aScenario;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.CollectionPredicate.isEmpty;
import static fdit.testTools.predicate.FilePredicate.*;

public class ScenarioCreationCommandTest extends FditTestCase {

    @Test
    public void testScenarioCreationCommand() {
        getExecutor().execute(new ScenarioCreationCommand(getRoot(), "scenario", "", EMPTY_RECORDING));
        assertThat(getRoot(), aRoot(aScenario("scenario", "", "")));
        assertThat(listFiles(getRootFile()),
                containsOnly(aFile("scenario.scenario", containsAll("<entry key=\"description\"/>"))));

        undo();
        assertThat(getRoot(), aRoot());
        assertThat(listFiles(getRootFile()), isEmpty());

        redo();
        assertThat(getRoot(), aRoot(aScenario("scenario", "", "")));
        assertThat(listFiles(getRootFile()),
                containsOnly(aFile("scenario.scenario", containsAll("<entry key=\"description\"/>"))));
    }
}