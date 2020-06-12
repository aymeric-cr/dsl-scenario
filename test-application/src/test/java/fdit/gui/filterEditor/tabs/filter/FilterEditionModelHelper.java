package fdit.gui.filterEditor.tabs.filter;

import java.util.function.Predicate;

import static fdit.tools.predicate.PredicateUtils.and;

class FilterEditionModelHelper {

    private FilterEditionModelHelper() {

    }

    static Predicate<FilterEditionModel> aFilterEditionModel(final Predicate<FilterEditionModel>... constraintes) {
        return filterEditionModel -> and(constraintes).test(filterEditionModel);
    }

    static Predicate<FilterEditionModel> aName(final String name) {
        return filterEditionModel -> filterEditionModel.getName().equals(name);
    }

    static Predicate<FilterEditionModel> aDescription(final String description) {
        return filterEditionModel -> filterEditionModel.getDescription().equals(description);
    }

    static Predicate<FilterEditionModel> aContent(final String content) {
        return filterEditionModel -> filterEditionModel.getContent().equals(content);
    }
}