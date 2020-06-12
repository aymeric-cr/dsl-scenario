package fdit.gui.application.commands.create;

import fdit.gui.application.FditTestCase;
import fdit.testTools.predicate.FilePredicate;
import org.junit.Test;

import java.io.File;

import static fdit.metamodel.FditElementHelper.aDirectory;
import static fdit.metamodel.FditElementHelper.aRoot;
import static fdit.metamodel.element.DirectoryUtils.findDirectory;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.CollectionPredicate.isEmpty;
import static fdit.testTools.predicate.FilePredicate.listFiles;

public class CreateDirectoryCommandTest extends FditTestCase {

    @Test
    public void createDirectories() {
        final File rootFile = getRootFile();
        assertThat(getRoot(), aRoot());
        assertThat(listFiles(rootFile), isEmpty());

        getExecutor().execute(new CreateDirectoryCommand(getRoot(), "dir"));
        assertThat(getRoot(), aRoot(aDirectory("dir")));
        assertThat(listFiles(rootFile),
                containsOnly(FilePredicate.aDirectory("dir")));

        getExecutor().execute(new CreateDirectoryCommand(findDirectory("dir", getRoot()).get(), "subDir"));
        assertThat(getRoot(),
                aRoot(
                        aDirectory("dir",
                                aDirectory("subDir"))));
        assertThat(listFiles(rootFile),
                containsOnly(FilePredicate.aDirectory("dir",
                        FilePredicate.aDirectory("subDir"))));

        undo();
        assertThat(getRoot(), aRoot(aDirectory("dir")));
        assertThat(listFiles(rootFile),
                containsOnly(FilePredicate.aDirectory("dir")));

        undo();
        assertThat(getRoot(), aRoot());
        assertThat(listFiles(rootFile), isEmpty());

        redo();
        assertThat(getRoot(), aRoot(aDirectory("dir")));
        assertThat(listFiles(rootFile),
                containsOnly(FilePredicate.aDirectory("dir")));

        redo();
        assertThat(getRoot(),
                aRoot(
                        aDirectory("dir",
                                aDirectory("subDir"))));
        assertThat(listFiles(rootFile),
                containsOnly(FilePredicate.aDirectory("dir",
                        FilePredicate.aDirectory("subDir"))));
    }
}