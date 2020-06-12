package fdit.gui.filterEditor.tabs.filter;

import fdit.metamodel.filter.LTLFilter;

public interface FilterViewCreator {
    void showCreateFilterEditionView();

    void showEditFilterEditionView(LTLFilter filter);
}
