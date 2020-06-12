package fdit.gui.application.commands.load;

import fdit.history.Command;
import fdit.metamodel.execution.Execution;
import fdit.tools.i18n.MessageTranslator;

import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class LoadExecutionCommand implements Command {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(LoadExecutionCommand.class);

    private final Execution execution;

    public LoadExecutionCommand(final Execution execution) {
        this.execution = execution;
    }

    public Execution getExecution() {
        return execution;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.loadExecution.descr", execution.getName());
    }

    @Override
    public void execute() throws Exception {
    }

    @Override
    public void undo() throws Exception {
    }

    @Override
    public void redo() throws Exception {
    }
}