package fdit.history;

import fdit.gui.application.commands.load.LoadAlterationTriggerCommand;
import fdit.gui.application.commands.load.LoadExecutionCommand;
import fdit.gui.application.commands.load.LoadScenarioCommand;
import fdit.gui.filterEditor.OpenFilterEditorCommand;
import fdit.history.Command.CommandType;

import java.util.Collection;
import java.util.Deque;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Queues.newArrayDeque;
import static fdit.history.Command.CommandType.*;

public class FditHistory {

    private static final int MAX_STACK_SIZE = 100;

    private final Deque<Command> undoHistory = newArrayDeque();
    private final Deque<Command> redoHistory = newArrayDeque();
    private final Collection<FditHistoryListener> listeners = newArrayList();

    private static Command getRealCommand(final Command command) {
        if (command instanceof QuietCommand) {
            return ((QuietCommand) command).getCommand();
        } else {
            return command;
        }
    }

    private static CommandType getCommandType(final Command command) {
        if (command instanceof QuietCommand) {
            return ((QuietCommand) command).getCommandType();
        }
        return MAIN;
    }

    void execute(final Command command) {
        try {
            command.execute();
        } catch (final Exception e) {
            redoHistory.clear();
            fireExceptionThrown(command, e);
            return;
        }
        if (undoHistory.size() == MAX_STACK_SIZE) {
            removeCompleteLastUndoCommand();
        }
        pushToHistory(undoHistory, command);
        redoHistory.clear();
        fireCommandExecutedEvent(command);
    }

    void undo() {
        final Command command = undoHistory.pop();
        try {
            command.undo();
        } catch (final Exception e) {
            redoHistory.clear();
            fireExceptionThrown(command, e);
            return;
        }
        pushToHistory(redoHistory, command);
        fireCommandUndoneEvent(command);
    }

    void redo() {
        final Command command = redoHistory.pop();
        try {
            command.redo();
        } catch (final Exception e) {
            redoHistory.clear();
            fireExceptionThrown(command, e);
            return;
        }
        pushToHistory(undoHistory, command);
        fireCommandRedoneEvent(command);
    }

    private void pushToHistory(final Deque<Command> history, final Command command) {
        if (canBeUndo(command)) {
            history.push(command);
        }
    }

    private boolean canBeUndo(final Command command) {
        return !(command instanceof OpenFilterEditorCommand) &&
                !(command instanceof LoadScenarioCommand) &&
                !(command instanceof LoadAlterationTriggerCommand) &&
                !(command instanceof LoadExecutionCommand);
    }

    Deque<Command> getUndoHistory() {
        return undoHistory;
    }

    Deque<Command> getRedoHistory() {
        return redoHistory;
    }

    Command getUndoCommand() {
        return undoHistory.peek();
    }

    Command getRedoCommand() {
        return redoHistory.peek();
    }

    public void addListener(final FditHistoryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final FditHistoryListener listener) {
        listeners.remove(listener);
    }

    void clear() {
        undoHistory.clear();
        redoHistory.clear();
    }

    private void removeCompleteLastUndoCommand() {
        Command preCommand = undoHistory.peekLast();
        while (preCommand instanceof QuietCommand && ((QuietCommand) preCommand).getCommandType() == PRE) {
            undoHistory.removeLast();
            preCommand = undoHistory.peekLast();
        }
        undoHistory.removeLast();
        Command postCommand = undoHistory.peekLast();
        while (postCommand instanceof QuietCommand && ((QuietCommand) postCommand).getCommandType() == POST) {
            undoHistory.removeLast();
            postCommand = undoHistory.peekLast();
        }
    }

    private void fireCommandExecutedEvent(final Command command) {
        newArrayList(listeners).forEach(fditHistoryListener ->
                fditHistoryListener.commandExecuted(getRealCommand(command), getCommandType(command)));
    }

    private void fireCommandUndoneEvent(final Command command) {
        newArrayList(listeners).forEach(fditHistoryListener ->
                fditHistoryListener.commandUndone(getRealCommand(command), getCommandType(command)));
    }

    private void fireCommandRedoneEvent(final Command command) {
        newArrayList(listeners).forEach(fditHistoryListener ->
                fditHistoryListener.commandRedone(getRealCommand(command), getCommandType(command)));
    }

    private void fireExceptionThrown(final Command command, final Throwable throwable) {
        newArrayList(listeners).forEach(fditHistoryListener -> fditHistoryListener.commandFailed(getRealCommand(command),
                throwable));
    }
}
