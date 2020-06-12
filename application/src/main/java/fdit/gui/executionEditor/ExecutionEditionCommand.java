package fdit.gui.executionEditor;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.recording.Recording;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ExecutionEditionCommand implements FditElementCommand<Execution> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ExecutionEditionCommand.class);

    private final Execution execution;
    private final Recording newRecording;
    private final Recording oldRecording;

    public ExecutionEditionCommand(final Execution execution, final Recording newRecording) {
        this.execution = execution;
        this.newRecording = newRecording;
        oldRecording = execution.getRecording();
    }

    Recording getOldRecording() {
        return oldRecording;
    }

    Recording getNewRecording() {
        return newRecording;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public Execution getSubject() {
        return execution;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editExecution.descr");
    }

    @Override
    public void execute() throws Exception {
        editExecution(newRecording);
    }

    @Override
    public void undo() throws Exception {
        editExecution(oldRecording);
    }

    @Override
    public void redo() throws Exception {
        editExecution(newRecording);
    }

    private void editExecution(final Recording recording) {
        execution.setRecording(recording);
        save(execution, FDIT_MANAGER.getRootFile());
    }
}