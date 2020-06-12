package fdit.gui.application.commands.delete;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Root;
import fdit.testTools.predicate.FilePredicate;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static fdit.gui.application.commands.delete.DeletionUtils.delete;
import static fdit.metamodel.FditElementHelper.aDirectory;
import static fdit.metamodel.FditElementHelper.*;
import static fdit.metamodel.element.DirectoryUtils.findDirectory;
import static fdit.metamodel.element.DirectoryUtils.findScenario;
import static fdit.metamodel.scenario.ScenarioHelper.content;
import static fdit.metamodel.scenario.ScenarioHelper.description;
import static fdit.testTools.FileTestUtils.createEmptyFile;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.FilePredicate.*;
import static java.util.Arrays.asList;

public class DeletionTest extends FditTestCase {

    @Test
    public void deleteDirectory() throws Exception {
        final Root root = root(
                folder("dir",
                        scenario("scenario", content("alter all planes"), description(""))));
        final File dirFile = findFile("dir");
        createEmptyFile(dirFile, "subFile.txt");
        createEmptyFile(dirFile, "otherSubFile.any");

        assertThat(root, aRoot(
                aDirectory("dir",
                        aScenario("scenario", "alter all planes", ""))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("dir",
                        aFile("scenario.scenario",
                                containsAll("<entry key=\"content\">alter all planes</entry>\n")),
                        aFile("subFile.txt"),
                        aFile("otherSubFile.any"))));

        delete(findDirectory("dir", root).get());

        assertThat(root, aRoot());
        assertThat(listFiles(getRootFile()), Collection::isEmpty);

        undo();
        assertThat(root, aRoot(
                aDirectory("dir",
                        aScenario("scenario", "alter all planes", ""))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("dir",
                        aFile("scenario.scenario",
                                containsAll("<entry key=\"content\">alter all planes</entry>\n")),
                        aFile("subFile.txt"),
                        aFile("otherSubFile.any"))));

        redo();
        assertThat(root, aRoot());
        assertThat(listFiles(getRootFile()), Collection::isEmpty);
    }

    @Test
    public void deleteDirectoryAndChild() throws Exception {
        final Root root = root(
                folder("dir",
                        scenario("scenario", content("alter all planes"), description(""))));
        final File dirFile = findFile("dir");
        createEmptyFile(dirFile, "subFile.txt");
        createEmptyFile(dirFile, "otherSubFile.any");

        assertThat(root, aRoot(
                aDirectory("dir",
                        aScenario("scenario", "alter all planes", ""))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("dir",
                        aFile("scenario.scenario",
                                containsAll("<entry key=\"content\">alter all planes</entry>\n")),
                        aFile("subFile.txt"),
                        aFile("otherSubFile.any"))));

        delete(asList(findDirectory("dir", root).get(), findScenario("scenario", root).get()));

        assertThat(root, aRoot());
        assertThat(listFiles(getRootFile()), Collection::isEmpty);

        undo();
        assertThat(root, aRoot(
                aDirectory("dir",
                        aScenario("scenario", "alter all planes", ""))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("dir",
                        aFile("scenario.scenario",
                                containsAll("<entry key=\"content\">alter all planes</entry>\n")),
                        aFile("subFile.txt"),
                        aFile("otherSubFile.any"))));

        redo();
        assertThat(root, aRoot());
        assertThat(listFiles(getRootFile()), Collection::isEmpty);
    }

    @Test
    public void cannotRemoveRoot() throws IOException {
        assertThat(getRootFile().exists());

        delete(getRoot());
        assertThat(getRootFile().exists());
    }
}