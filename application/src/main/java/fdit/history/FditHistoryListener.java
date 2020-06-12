package fdit.history;

import fdit.history.Command.CommandType;

public interface FditHistoryListener {

    default void commandExecuted(final Command command, final CommandType commandType) {
    }

    default void commandUndone(final Command command, final CommandType commandType) {
    }

    default void commandRedone(final Command command, final CommandType commandType) {
    }

    default void commandFailed(final Command command, final Throwable throwable) {
    }
}
