package fdit.history;

public class TestCommand implements Command {
    @Override
    public String getContent() {
        return "test";
    }

    @Override
    public void execute() {
    }

    @Override
    public void undo() {
    }

    @Override
    public void redo() {
    }
}
