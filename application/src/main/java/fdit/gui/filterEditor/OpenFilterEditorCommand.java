package fdit.gui.filterEditor;

import fdit.history.Command;
import fdit.metamodel.filter.LTLFilter;
import fdit.tools.i18n.MessageTranslator;

import static fdit.gui.filterEditor.OpenFilterEditorCommand.OpenFilterType.EXISTING_FILTER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class OpenFilterEditorCommand implements Command {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(OpenFilterEditorCommand.class);
    private final OpenFilterType filterType;
    private final LTLFilter filterToOpen;

    public OpenFilterEditorCommand(final LTLFilter filterToOpen) {
        filterType = EXISTING_FILTER;
        this.filterToOpen = filterToOpen;
    }

    public OpenFilterType getFilterType() {
        return filterType;
    }

    public LTLFilter getFilterToOpen() {
        return filterToOpen;
    }

    @Override
    public String getContent() {
        return TRANSLATOR.getMessage("command.createFilter.descr");
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

    public enum OpenFilterType {EXISTING_FILTER, ALL_FILTERS}
}
