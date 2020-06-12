package fdit.history;

public interface Command {

    String getContent();

    void execute() throws Exception;

    void undo() throws Exception;

    void redo() throws Exception;

    enum CommandType {PRE, MAIN, POST}

}