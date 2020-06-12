package fdit.gui.application.concurrent;

public class FXAsyncExecutionFactory implements AsyncExecutionFactory {

    @Override
    public <T> AsyncExecution<T> createAsyncExecution() {
        return new FXAsyncExecution<T>();
    }
}
