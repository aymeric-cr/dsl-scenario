package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class SchemaCreationCommand implements FditElementCommand<Schema> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(SchemaCreationCommand.class);

    private final Directory father;
    private final String name;
    private final Recording recording;
    private final String content;
    private Schema createdscenario;

    public SchemaCreationCommand(final Directory father,
                                 final String name,
                                 final String content,
                                 final Recording recording) {
        this.father = father;
        this.name = name;
        this.content = content;
        this.recording = recording;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public Schema getSubject() {
        return createdscenario;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createScenario.descr", name);
    }

    @Override
    public void execute() throws Exception {
        createdscenario = new Schema(name, "", content, recording);
        processScenario();
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(createdscenario);
    }

    @Override
    public void redo() throws Exception {
        processScenario();
    }

    private void processScenario() {
        FDIT_MANAGER.addFditElement(createdscenario, father);
        save(createdscenario, FDIT_MANAGER.getRootFile());
    }
}