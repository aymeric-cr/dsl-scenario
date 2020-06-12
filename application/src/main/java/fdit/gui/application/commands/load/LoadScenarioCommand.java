package fdit.gui.application.commands.load;

import fdit.history.Command;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class LoadScenarioCommand implements Command {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(LoadScenarioCommand.class);

    private final Schema schema;

    public LoadScenarioCommand(final Schema schema) {
        this.schema = schema;
    }

    public Schema gescenario() {
        return schema;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.loadScenario.descr", schema.getName());
    }

    @Override
    public void execute() throws Exception {
        final Recording recording = schema.getRecording();
        if (recording != EMPTY_RECORDING) {
            recording.load();
        }
    }

    @Override
    public void undo() throws Exception {
        final Recording recording = schema.getRecording();
        if (recording != EMPTY_RECORDING) {
            recording.unload();
        }
    }

    @Override
    public void redo() throws Exception {
        final Recording recording = schema.getRecording();
        if (recording != EMPTY_RECORDING) {
            recording.load();
        }
    }
}
