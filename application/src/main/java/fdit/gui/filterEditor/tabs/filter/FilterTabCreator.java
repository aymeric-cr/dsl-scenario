package fdit.gui.filterEditor.tabs.filter;

import fdit.gui.filterEditor.FilterEditorContext;
import fdit.gui.utils.RenamableTab;
import fdit.ltlcondition.ide.LTLConditionFacade;
import fdit.metamodel.filter.LTLFilter;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.utils.FXUtils.loadFxml;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;

public class FilterTabCreator implements FilterViewCreator {

    private final TabPane tabPane;
    private final Map<FilterEditionModel, Tab> tabModelsMapping = newHashMap();
    private final FilterEditorContext contextFilter;

    public FilterTabCreator(final TabPane tabPane, final FilterEditorContext contextFilter) {
        this.tabPane = tabPane;
        this.contextFilter = contextFilter;
    }

    private static FilterEditionModel createFilterEditionModel(final LTLFilter filter) {
        final FilterEditionModel filterEditionModel = new FilterEditionModel(LTLConditionFacade.get(), filter.getId());
        filterEditionModel.setEditedFilter(filter);
        filterEditionModel.restoreModelFromEditedFilter();
        return filterEditionModel;
    }

    @Override
    public void showCreateFilterEditionView() {
        final FilterEditionModel filterEditionModel = new FilterEditionModel(LTLConditionFacade.get(), randomUUID());
        createFilterEditionTab(filterEditionModel);
        contextFilter.addFilterEditionModel(filterEditionModel);
    }

    @Override
    public void showEditFilterEditionView(final LTLFilter filter) {
        final Optional<FilterEditionModel> filterEditionModelOptional =
                contextFilter.findFilterEditionModel(filter);
        if (filterEditionModelOptional.isPresent()) {
            tabPane.getSelectionModel().select(tabModelsMapping.get(filterEditionModelOptional.get()));
            return;
        }
        final FilterEditionModel filterEditionModel = createFilterEditionModel(filter);
        createFilterEditionTab(filterEditionModel);
        contextFilter.addFilterEditionModel(filterEditionModel);
    }

    private void createFilterEditionTab(final FilterEditionModel filterEditionModel) {
        try {
            final RenamableTab filterEditionTab = new RenamableTab();
            final FilterEditionTabController controller = new FilterEditionTabController(
                    filterEditionTab,
                    filterEditionModel);
            filterEditionTab.setOnCloseRequest(event -> {
                contextFilter.removeFilterEditionModel(filterEditionModel);
                controller.closeTab();
                tabModelsMapping.remove(filterEditionModel);
            });
            final Node contentPane = loadFxml(getClass().getResource("filterCreationTab.fxml"),
                    controller);
            tabPane.getTabs().add(filterEditionTab);
            tabPane.getSelectionModel().select(filterEditionTab);
            filterEditionTab.setContent(contentPane);
            tabModelsMapping.put(filterEditionModel, filterEditionTab);
        } catch (final IOException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}