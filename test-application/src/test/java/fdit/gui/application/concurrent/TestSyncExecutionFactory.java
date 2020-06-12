package fdit.gui.application.concurrent;

public class TestSyncExecutionFactory implements AsyncExecutionFactory {

    @Override
    public <T> AsyncExecution<T> createAsyncExecution() {
        return new TestSyncExecution<T>();
    }
}
