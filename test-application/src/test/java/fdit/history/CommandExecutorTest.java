package fdit.history;

import org.junit.Test;

import java.util.function.Predicate;

import static fdit.history.Command.CommandType.POST;
import static fdit.history.Command.CommandType.PRE;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.isEmpty;
import static fdit.testTools.predicate.CollectionPredicate.isSequence;
import static org.junit.Assert.*;

public class CommandExecutorTest {

    private static Predicate<? super Command> aCommand(final Command expectedCommand) {
        return expectedCommand::equals;
    }

    private static Predicate<? super Command> aPreCommand(final Command preCommand) {
        return command -> {
            final QuietCommand quietCommand = (QuietCommand) command;
            return quietCommand.getCommandType() == PRE &&
                    quietCommand.getCommand() == preCommand;
        };
    }

    private static Predicate<? super Command> aPostCommand(final Command postCommand) {
        return command -> {
            final QuietCommand quietCommand = (QuietCommand) command;
            return quietCommand.getCommandType() == POST &&
                    quietCommand.getCommand() == postCommand;
        };
    }

    @Test
    public void testUndoRedo() {
        final Command command1 = new TestCommand();
        final Command command2 = new TestCommand();
        final Command command3 = new TestCommand();
        final CommandExecutor executor = new CommandExecutor();
        assertFalse(executor.canUndo());
        assertFalse(executor.canRedo());

        executor.redo();
        assertFalse(executor.canUndo());
        assertFalse(executor.canRedo());

        executor.undo();
        assertFalse(executor.canUndo());
        assertFalse(executor.canRedo());

        executor.execute(command1);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);

        executor.undo();
        assertFalse(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealRedoCommand(), command1);

        executor.redo();
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);

        executor.execute(command2);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command2);

        executor.execute(command3);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command3);

        executor.undo();
        assertTrue(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command2);
        assertSame(executor.getRealRedoCommand(), command3);

        executor.redo();
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command3);

        executor.undo();
        executor.undo();
        executor.undo();
        executor.undo();
        assertFalse(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealRedoCommand(), command1);
    }

    @Test
    public void clearRedoStackAfterNewCommand() {
        final Command command1 = new TestCommand();
        final Command command2 = new TestCommand();
        final Command command3 = new TestCommand();
        final CommandExecutor executor = new CommandExecutor();

        executor.execute(command1);
        executor.execute(command2);
        executor.undo();
        assertTrue(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);
        assertSame(executor.getRealRedoCommand(), command2);

        executor.execute(command3);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command3);

        executor.undo();
        assertTrue(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);
        assertSame(executor.getRealRedoCommand(), command3);
    }

    @Test
    public void testQuietCommands() {
        final Command preCommand1 = new TestCommand();
        final Command command1 = new TestCommand();
        final Command postCommand11 = new TestCommand();
        final Command postCommand12 = new TestCommand();
        final Command preCommand2 = new TestCommand();
        final Command command2 = new TestCommand();
        final Command command3 = new TestCommand();
        final CommandExecutor executor = new CommandExecutor();

        executor.executePreCommand(preCommand1);
        executor.execute(command1);
        executor.executePostCommand(postCommand11);
        executor.executePostCommand(postCommand12);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);
        assertThat(executor.getHistory().getUndoHistory(),
                isSequence(aPostCommand(postCommand12),
                        aPostCommand(postCommand11),
                        aCommand(command1),
                        aPreCommand(preCommand1)));
        assertThat(executor.getHistory().getRedoHistory(), isEmpty());

        executor.undo();
        assertFalse(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealRedoCommand(), command1);
        assertThat(executor.getHistory().getUndoHistory(), isEmpty());
        assertThat(executor.getHistory().getRedoHistory(),
                isSequence(aPreCommand(preCommand1),
                        aCommand(command1),
                        aPostCommand(postCommand11),
                        aPostCommand(postCommand12)));

        executor.redo();
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);
        assertThat(executor.getHistory().getUndoHistory(),
                isSequence(aPostCommand(postCommand12),
                        aPostCommand(postCommand11),
                        aCommand(command1),
                        aPreCommand(preCommand1)));
        assertThat(executor.getHistory().getRedoHistory(), isEmpty());

        executor.executePreCommand(preCommand2);
        executor.execute(command2);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command2);
        assertThat(executor.getHistory().getUndoHistory(),
                isSequence(aCommand(command2),
                        aPreCommand(preCommand2),
                        aPostCommand(postCommand12),
                        aPostCommand(postCommand11),
                        aCommand(command1),
                        aPreCommand(preCommand1)));
        assertThat(executor.getHistory().getRedoHistory(), isEmpty());

        executor.undo();
        assertTrue(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command1);
        assertSame(executor.getRealRedoCommand(), command2);
        assertThat(executor.getHistory().getUndoHistory(),
                isSequence(aPostCommand(postCommand12),
                        aPostCommand(postCommand11),
                        aCommand(command1),
                        aPreCommand(preCommand1)));
        assertThat(executor.getHistory().getRedoHistory(),
                isSequence(aPreCommand(preCommand2), aCommand(command2)));

        executor.undo();
        assertFalse(executor.canUndo());
        assertTrue(executor.canRedo());
        assertSame(executor.getRealRedoCommand(), command1);
        assertThat(executor.getHistory().getUndoHistory(), isEmpty());
        assertThat(executor.getHistory().getRedoHistory(),
                isSequence(aPreCommand(preCommand1),
                        aCommand(command1),
                        aPostCommand(postCommand11),
                        aPostCommand(postCommand12),
                        aPreCommand(preCommand2),
                        aCommand(command2)));

        executor.execute(command3);
        assertTrue(executor.canUndo());
        assertFalse(executor.canRedo());
        assertSame(executor.getRealUndoCommand(), command3);
        assertThat(executor.getHistory().getUndoHistory(), isSequence(aCommand(command3)));
        assertThat(executor.getHistory().getRedoHistory(), isEmpty());
    }


}