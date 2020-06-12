package fdit.history;

import java.util.Deque;
import java.util.Iterator;

import static com.google.common.collect.Queues.newArrayDeque;
import static fdit.history.Command.CommandType.POST;
import static fdit.history.Command.CommandType.PRE;
import static fdit.history.QuietCommand.createPostCommand;
import static fdit.history.QuietCommand.createPreCommand;

public class CommandExecutor {

    private final FditHistory history = new FditHistory();

    private static void removeNextCompleteCommand(final CompleteCommand completeCommand,
                                                  final Deque<Command> historyStack) {
        completeCommand.preCommands.forEach(command -> historyStack.removeFirst());
        historyStack.removeFirst();
        completeCommand.postCommands.forEach(command -> historyStack.removeFirst());
    }

    private static boolean isPostCommand(final Command postCommand) {
        return postCommand instanceof QuietCommand && ((QuietCommand) postCommand).getCommandType() == POST;
    }

    private static boolean isPreCommand(final Command preCommand) {
        return preCommand instanceof QuietCommand && ((QuietCommand) preCommand).getCommandType() == PRE;
    }

    public synchronized FditHistory getHistory() {
        return history;
    }

    public synchronized void execute(final Command command) {
        history.execute(command);
    }

    public synchronized void executePreCommand(final Command command) {
        history.execute(createPreCommand(command));
    }

    public synchronized void executePostCommand(final Command command) {
        history.execute(createPostCommand(command));
    }

    public synchronized void undo() {
        if (!canUndo()) {
            return;
        }
        Command postCommand = history.getUndoCommand();
        while (isPostCommand(postCommand)) {
            history.undo();
            postCommand = history.getUndoCommand();
        }
        history.undo();
        Command preCommand = history.getUndoCommand();
        while (isPreCommand(preCommand)) {
            history.undo();
            preCommand = history.getUndoCommand();
        }
    }

    public synchronized void redo() {
        if (!canRedo()) {
            return;
        }
        Command preCommand = history.getRedoCommand();
        while (isPreCommand(preCommand)) {
            history.redo();
            preCommand = history.getRedoCommand();
        }
        history.redo();
        Command postCommand = history.getRedoCommand();
        while (isPostCommand(postCommand)) {
            history.redo();
            postCommand = history.getRedoCommand();
        }
    }

    public synchronized boolean canUndo() {
        return getRealUndoCommand() != null;
    }

    public synchronized boolean canRedo() {
        return getRealRedoCommand() != null;
    }

    public synchronized Command getRealUndoCommand() {
        if (history.getUndoHistory().isEmpty()) {
            return null;
        }
        for (final Command command : history.getUndoHistory()) {
            if (!(command instanceof QuietCommand)) {
                return command;
            }
        }
        return null;
    }

    public synchronized Command getRealRedoCommand() {
        if (history.getRedoHistory().isEmpty()) {
            return null;
        }
        for (final Command command : history.getRedoHistory()) {
            if (!(command instanceof QuietCommand)) {
                return command;
            }
        }
        return null;
    }

    public synchronized int getUndoHistorySize() {
        return history.getUndoHistory().size();
    }

    public synchronized int getRedoHistorySize() {
        return history.getRedoHistory().size();
    }

    public synchronized void clear() {
        history.clear();
    }

    private CompleteCommand getNextUndoCompleteCommand() {
        final CompleteCommand completeCommand = new CompleteCommand();
        final Deque<Command> undoHistory = history.getUndoHistory();
        final Iterator<Command> undoHistoryIterator = undoHistory.iterator();
        Command command = null;
        while (undoHistoryIterator.hasNext()) {
            command = undoHistoryIterator.next();
            if (!isPostCommand(command)) {
                break;
            }
            completeCommand.postCommands.push(command);
        }
        completeCommand.mainCommand = command;
        while (undoHistoryIterator.hasNext()) {
            command = undoHistoryIterator.next();
            if (!isPreCommand(command)) {
                break;
            }
            completeCommand.preCommands.push(command);
        }
        return completeCommand;
    }

    private CompleteCommand getNextRedoCompleteCommand() {
        final CompleteCommand completeCommand = new CompleteCommand();
        final Deque<Command> redoHistory = history.getRedoHistory();
        final Iterator<Command> redoHistoryIterator = redoHistory.iterator();
        Command command = null;
        while (redoHistoryIterator.hasNext()) {
            command = redoHistoryIterator.next();
            if (!isPreCommand(command)) {
                break;
            }
            completeCommand.preCommands.push(command);
        }
        completeCommand.mainCommand = command;
        while (redoHistoryIterator.hasNext()) {
            command = redoHistoryIterator.next();
            if (!isPostCommand(command)) {
                break;
            }
            completeCommand.postCommands.push(command);
        }
        return completeCommand;
    }

    static class CompleteCommand {
        Deque<Command> preCommands = newArrayDeque();
        Command mainCommand;
        Deque<Command> postCommands = newArrayDeque();
    }
}
