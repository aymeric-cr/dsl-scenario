package fdit.gui.filterEditor.tabs.filter;

import fdit.metamodel.filter.LTLFilter;

public final class FilterEditionUtils {

    private FilterEditionUtils() {
    }

    public static LTLFilter createFilterFromModel(final FilterEditionModel filterEditionModel) {
        return new LTLFilter(
                filterEditionModel.getName(),
                filterEditionModel.getId(),
                filterEditionModel.getDescription(),
                filterEditionModel.getContent());
    }
}