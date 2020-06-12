package fdit.gui.filterEditor;

import fdit.gui.filterEditor.tabs.filter.FilterEditionModel;
import fdit.metamodel.filter.LTLFilter;
import javafx.collections.ObservableList;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javafx.collections.FXCollections.observableArrayList;

public class FilterEditorContext {

    private final ObservableList<FilterEditionModel> filterEditionModels = observableArrayList();

    public void addFilterEditionModel(final FilterEditionModel filterEditionModel) {
        filterEditionModels.add(filterEditionModel);
    }

    public ObservableList<FilterEditionModel> getFilterEditionModels() {
        return filterEditionModels;
    }

    public void removeAllModel() {
        filterEditionModels.clear();
    }

    public void removeFilterEditionModel(final FilterEditionModel filterEditionModel) {
        filterEditionModels.remove(filterEditionModel);
    }

    public Optional<FilterEditionModel> findFilterEditionModel(final LTLFilter filter) {
        for (final FilterEditionModel filterEditionModel : filterEditionModels) {
            if (filterEditionModel.getEditedFilter() == filter) {
                return of(filterEditionModel);
            }
        }
        return empty();
    }
}