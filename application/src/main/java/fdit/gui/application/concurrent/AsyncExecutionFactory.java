package fdit.gui.application.concurrent;

public interface AsyncExecutionFactory {

    <T> AsyncExecution<T> createAsyncExecution();
}
