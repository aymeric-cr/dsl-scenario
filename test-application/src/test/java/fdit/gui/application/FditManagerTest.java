package fdit.gui.application;

import fdit.history.CommandExecutor;
import fdit.history.TestCommand;
import fdit.storage.recording.TestRecordingInDatabaseLoadingCallback;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FditManagerTest extends FditTestCase {

    @Test
    public void clearCommandExecutor_whenRootBrowserChanged() throws IOException {
        final CommandExecutor executor = FDIT_MANAGER.getCommandExecutor();
        executor.execute(new TestCommand());
        assertTrue(executor.canUndo());

        final File newRoot = new File(fileSystem.getRoot(), "newRoot");
        forceMkdir(newRoot);
        FDIT_MANAGER.loadRoot(newRoot, new TestRecordingInDatabaseLoadingCallback());
        assertFalse(executor.canUndo());
    }
}