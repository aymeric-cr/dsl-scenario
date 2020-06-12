package fdit.gui.application.commands.create;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.element.Directory;
import fdit.metamodel.filter.LTLFilter;
import fdit.tools.i18n.MessageTranslator;

import java.util.UUID;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.CREATION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class LTLFilterCreationCommand implements FditElementCommand<LTLFilter> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(LTLFilterCreationCommand.class);

    private final Directory father;
    private final String name;
    private final UUID id;
    private final String content;
    private LTLFilter createdFilter;

    public LTLFilterCreationCommand(final Directory father,
                                    final String name,
                                    final UUID id) {
        this.father = father;
        this.name = name;
        this.id = id;
        content = "";
    }

    private static LTLFilter createDefaultFilter(final String name,
                                                 final UUID id,
                                                 final String content) {
        return new LTLFilter(name, id, "", content);
    }

    @Override
    public LTLFilter getSubject() {
        return createdFilter;
    }

    @Override
    public OperationType getOperationType() {
        return CREATION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createFilter.descr");
    }

    @Override
    public void execute() throws Exception {
        createdFilter = createDefaultFilter(name, id, content);
        addAndSaveFilter();
    }

    @Override
    public void undo() throws Exception {
        FDIT_MANAGER.removeFditElement(createdFilter);
    }

    @Override
    public void redo() throws Exception {
        addAndSaveFilter();
    }

    private void addAndSaveFilter() {
        FDIT_MANAGER.addFditElement(createdFilter, father);
        save(createdFilter, FDIT_MANAGER.getRootFile());
    }
}