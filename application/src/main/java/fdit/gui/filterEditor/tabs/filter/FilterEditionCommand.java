package fdit.gui.filterEditor.tabs.filter;

import fdit.gui.application.commands.FditElementCommand;
import fdit.metamodel.filter.LTLFilter;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementStorage.save;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class FilterEditionCommand implements FditElementCommand<LTLFilter> {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FilterEditionCommand.class);

    private LTLFilter filter;
    private final String newName;
    private final String oldName;
    private final String newDescription;
    private final String oldDescription;
    private final String newContent;
    private final String oldContent;

    public FilterEditionCommand(final LTLFilter filter,
                                final String newName,
                                final String newDescription,
                                final String newContent) {
        this.filter = filter;
        this.newName = newName;
        this.newDescription = newDescription;
        this.newContent = newContent;
        oldName = filter.getName();
        oldContent = filter.getContent();
        oldDescription = filter.getDescription();
    }

    @Override
    public LTLFilter getSubject() {
        return filter;
    }

    @Override
    public OperationType getOperationType() {
        return EDITION;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.editFilter.descr", filter.getName());
    }

    @Override
    public void execute() throws Exception {
        editFilter(newName, newContent, newDescription);
    }

    @Override
    public void undo() throws Exception {
        editFilter(oldName, oldContent, oldDescription);
    }

    @Override
    public void redo() throws Exception {
        editFilter(newName, newContent, newDescription);
    }

    private void editFilter(final String name, final String content, final String description) throws Exception {
        filter.setName(name);
        filter.setContent(content);
        filter.setDescription(description);
        save(filter, FDIT_MANAGER.getRootFile());
    }
}