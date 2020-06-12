package fdit.history;

import static fdit.history.Command.CommandType.POST;
import static fdit.history.Command.CommandType.PRE;

class QuietCommand implements Command {

    private final CommandType commandType;
    private final Command command;

    private QuietCommand(final Command command, final CommandType commandType) {
        this.command = command;
        this.commandType = commandType;
    }

    static QuietCommand createPreCommand(final Command command) {
        return new QuietCommand(command, PRE);
    }

    static QuietCommand createPostCommand(final Command command) {
        return new QuietCommand(command, POST);
    }

    Command getCommand() {
        return command;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public String getContent() {
        return command.getContent();
    }

    @Override
    public void execute() throws Exception {
        command.execute();
    }

    @Override
    public void undo() throws Exception {
        command.undo();
    }

    @Override
    public void redo() throws Exception {
        command.redo();
    }

}
