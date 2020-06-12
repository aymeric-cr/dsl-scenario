package fdit.gui.application.commands.move;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Directory;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import fdit.testTools.predicate.FilePredicate;
import org.junit.Test;

import static fdit.metamodel.FditElementHelper.*;
import static fdit.metamodel.element.DirectoryUtils.findDirectory;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.listFiles;

public class MoveFditElementCommandTest extends FditTestCase {

    @Test
    public void testMoveFile() {
        final Saver<Recording> recording = create();
        root(
                folder("newFather"),
                folder("folder",
                        folder("subFolder",
                                bstRecording("recording", recording))));

        final Directory newFather = findDirectory("newFather", getRoot()).get();
        getExecutor().execute(new MoveFditElementCommand(recording.get(), newFather));
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aRecording("recording")),
                aDirectory("folder",
                        aDirectory("subFolder"))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        aFile("recording.bst")),
                FilePredicate.aDirectory("folder",
                        FilePredicate.aDirectory("subFolder"))));

        undo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather"),
                aDirectory("folder",
                        aDirectory("subFolder",
                                aRecording("recording")))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather"),
                FilePredicate.aDirectory("folder",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst")))));

        redo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aRecording("recording")),
                aDirectory("folder",
                        aDirectory("subFolder"))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        aFile("recording.bst")),
                FilePredicate.aDirectory("folder",
                        FilePredicate.aDirectory("subFolder"))));
    }

    @Test
    public void testMoveFolder() {
        final Saver<Recording> recording = create();
        root(
                folder("newFather"),
                folder("folder",
                        folder("subFolder",
                                bstRecording("recording", recording))));
        final Directory folder = findDirectory("folder", getRoot()).get();
        final Directory subFolder = findDirectory("subFolder", folder).get();
        final Directory newFather = findDirectory("newFather", getRoot()).get();

        getExecutor().execute(new MoveFditElementCommand(subFolder, newFather));
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aDirectory("subFolder",
                                aRecording("recording"))),
                aDirectory("folder")));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst"))),
                FilePredicate.aDirectory("folder")));

        undo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather"),
                aDirectory("folder",
                        aDirectory("subFolder",
                                aRecording("recording")))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather"),
                FilePredicate.aDirectory("folder",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst")))));

        redo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aDirectory("subFolder",
                                aRecording("recording"))),
                aDirectory("folder")));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst"))),
                FilePredicate.aDirectory("folder")));
    }

    @Test
    public void testMoveDir_updateImpactedFiles() {
        final Saver<Recording> recordingSaver = create();
        root(
                folder("newFather"),
                folder("folder",
                        folder("subFolder",
                                bstRecording("recording", recordingSaver))));
        final Directory folder = findDirectory("folder", getRoot()).get();
        final Directory newFather = findDirectory("newFather", getRoot()).get();
        final Directory subFolder = findDirectory("subFolder", folder).get();
        getExecutor().execute(new MoveFditElementCommand(subFolder, newFather));
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aDirectory("subFolder",
                                aRecording("recording"))),
                aDirectory("folder")));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst"))),
                FilePredicate.aDirectory("folder")));
        undo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather"),
                aDirectory("folder",
                        aDirectory("subFolder",
                                aRecording("recording")))));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather"),
                FilePredicate.aDirectory("folder",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst")))));
        redo();
        assertThat(getRoot(), aRoot(
                aDirectory("newFather",
                        aDirectory("subFolder",
                                aRecording("recording"))),
                aDirectory("folder")));
        assertThat(listFiles(getRootFile()), containsOnly(
                FilePredicate.aDirectory("newFather",
                        FilePredicate.aDirectory("subFolder",
                                aFile("recording.bst"))),
                FilePredicate.aDirectory("folder")));
    }
}