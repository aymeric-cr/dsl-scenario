package fdit.storage;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.Root;
import fdit.metamodel.recording.Recording;
import org.junit.Test;

import java.io.File;

import static fdit.metamodel.element.DirectoryUtils.findDirectory;
import static fdit.metamodel.element.DirectoryUtils.findRecording;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.testTools.PredicateAssert.assertEqual;

public class FditStorageUtilsTest extends FditTestCase {

    @Test
    public void testGetFditElementFile() {
        final Root root = root(folder("dir",
                bstRecording("recording"),
                folder("subDir",
                        bstRecording("subRecording"))));
        final Directory dir = findDirectory("dir", root).get();
        final Directory subDir = findDirectory("subDir", dir).get();
        final Recording recording = findRecording("recording", dir).get();
        final Recording subRecording = findRecording("subRecording", subDir).get();
        final File rootFile = getRootFile();

        assertEqual(getFditElementFile(getRoot(), rootFile), rootFile);
        assertEqual(getFditElementFile(dir, rootFile), findFile("dir"));
        assertEqual(getFditElementFile(subDir, rootFile), findFile("subDir"));
        assertEqual(getFditElementFile(recording, rootFile), findFile("recording.bst"));
        assertEqual(getFditElementFile(subRecording, rootFile), findFile("subRecording.bst"));
    }

}
