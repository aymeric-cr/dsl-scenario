package fdit.gui.application.commands.rename;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import org.junit.Test;

import java.io.File;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.FditElementHelper.aRoot;
import static fdit.metamodel.element.DirectoryUtils.findDirectory;
import static fdit.metamodel.schema.SchemaHelper.*;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.xpathsExist;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public class RenameFditElementCommandTest extends FditTestCase {

    @Test
    public void renameRoot() throws Exception {
        final File rootFile = fileSystem.newFolder("root");
        FDIT_MANAGER.loadRoot(rootFile, recordingLoadingCallback());
        assertThat(getRoot(), aRoot("root"));

        getExecutor().execute(new RenameFditElementCommand(getRoot(), "renamed"));
        assertThat(getRoot(), aRoot("renamed"));

        undo();
        assertThat(getRoot(), aRoot("root"));

        redo();
        assertThat(getRoot(), aRoot("renamed"));

        FDIT_MANAGER.loadRoot(FDIT_MANAGER.getRootFile(), recordingLoadingCallback());
        assertThat(getRoot(), aRoot("renamed"));
    }

    @Test
    public void updateScenario_whenRecordingFolderRenamed() {
        final Saver<Recording> recording = create();
        root(
                folder("dir1",
                        folder("subDir1",
                                bstRecording("record", recording))),
                folder("dir2",
                        schema("scenario",
                                content("alter all planes"),
                                description(""),
                                withRecording(recording))));
        final File scenarioFile = findFile("scenario.scenario");

        getExecutor().execute(new RenameFditElementCommand(findDirectory("dir1", getRoot()).get(), "newDir1"));
        assertThat(scenarioFile, aFile("scenario.scenario",
                xpathsExist("//properties/entry[@key='content'][text()='alter all planes']"),
                xpathsExist("//properties/entry[@key='bstPath'][text()='" + separatorsToUnix("../newDir1/subDir1/record.bst") + "']"),
                xpathsExist("//properties/entry[4]").negate()));// Third entry should not be present

        undo();
        assertThat(scenarioFile, aFile("scenario.scenario",
                xpathsExist("//properties/entry[@key='content'][text()='alter all planes']"),
                xpathsExist("//properties/entry[@key='bstPath'][text()='" + separatorsToUnix("../dir1/subDir1/record.bst") + "']"),
                xpathsExist("//properties/entry[4]").negate()));

        redo();
        assertThat(scenarioFile, aFile("scenario.scenario",
                xpathsExist("//properties/entry[@key='content'][text()='alter all planes']"),
                xpathsExist("//properties/entry[@key='bstPath'][text()='" + separatorsToUnix("../newDir1/subDir1/record.bst") + "']"),
                xpathsExist("//properties/entry[4]").negate()));
    }
}