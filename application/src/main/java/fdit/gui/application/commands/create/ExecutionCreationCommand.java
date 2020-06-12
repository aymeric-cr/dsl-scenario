package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.execution.Execution;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ExecutionCreationCommand implements FditElementCommand<Execution> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ExecutionCreationCommand.class);

    private final String name;
    private final Directory father;
    private Execution execution;

    public ExecutionCreationCommand(final String name, final Directory father) {
        this.name = name;
        this.father = father;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public Execution getSubject() {
        return execution;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createExecution.descr", name);
    }

    @Override
    public void execute() throws Exception {
        execution = new Execution(name);
        processExecution();
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(execution);
    }

    @Override
    public void redo() throws Exception {
        processExecution();
    }

    private void processExecution() {
        FDIT_MANAGER.addFditElement(execution, father);
        save(execution, FDIT_MANAGER.getRootFile());
    }
}