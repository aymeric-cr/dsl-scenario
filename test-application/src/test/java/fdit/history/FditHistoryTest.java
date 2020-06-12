package fdit.history;

import org.junit.Test;

import static fdit.history.Command.CommandType.*;
import static fdit.history.QuietCommand.createPostCommand;
import static fdit.history.QuietCommand.createPreCommand;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class FditHistoryTest {

    @Test
    public void testFireRealCommand() {
        final Command commandTest = new TestCommand();
        final Command preCommand = createPreCommand(commandTest);
        final Command postCommand = createPostCommand(commandTest);
        final FditHistory history = new FditHistory();
        final FditHistoryListener historyListener = mock(FditHistoryListener.class);
        history.addListener(historyListener);
        history.execute(preCommand);
        history.execute(commandTest);
        history.execute(postCommand);
        verify(historyListener, times(1)).commandExecuted(commandTest, PRE);
        verify(historyListener, times(1)).commandExecuted(commandTest, MAIN);
        verify(historyListener, times(1)).commandExecuted(commandTest, POST);
    }

    @Test
    public void removeOldestCommand_whenMaxCapacityReached() {
        final Command preFirstCommand = createPreCommand(new TestCommand());
        final Command firstCommand = new TestCommand();
        final Command postFirstCommand = createPostCommand(new TestCommand());
        final FditHistory fditHistory = new FditHistory();
        fditHistory.execute(preFirstCommand);
        fditHistory.execute(firstCommand);
        fditHistory.execute(postFirstCommand);
        final Command otherCommand = new TestCommand();
        for (int i = 1; i <= 97; i++) {
            fditHistory.execute(otherCommand);
        }
        assertSame(fditHistory.getUndoHistory().peekLast(), preFirstCommand);
        fditHistory.execute(otherCommand);
        assertSame(fditHistory.getUndoHistory().peekLast(), otherCommand);
    }

    @Test
    public void executeThrowableCommand() throws Exception {
        final Command command1 = new TestCommand();
        final Command throwableCommand = mock(Command.class);
        final FditHistory fditHistory = new FditHistory();
        doThrow(new Exception()).when(throwableCommand).execute();

        fditHistory.execute(command1);
        fditHistory.undo();
        assertEquals(fditHistory.getUndoHistory().size(), 0);
        assertEquals(fditHistory.getRedoHistory().size(), 1);

        fditHistory.execute(throwableCommand);
        assertEquals(fditHistory.getUndoHistory().size(), 0);
        assertEquals(fditHistory.getRedoHistory().size(), 0);
    }
}