package fdit.gui.filterEditor;

import fdit.gui.EditorController;
import fdit.gui.filterEditor.tabs.filter.FilterTabCreator;
import fdit.gui.filterEditor.tabs.filter.FilterViewCreator;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.gui.utils.imageButton.ImageButton;
import fdit.metamodel.filter.LTLFilter;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static fdit.gui.Images.PLUS_ICON;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.element.DirectoryUtils.gatherAllLTLFilters;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.input.MouseButton.PRIMARY;

public class FilterEditorController implements EditorController {

    private final FilterEditorContext contextEditor = new FilterEditorContext();
    @FXML
    private TabPane filterTabPane;
    @FXML
    private Tab createFilterTab;
    @FXML
    private TableView<LTLFilter> filtersTableView;
    @FXML
    private TableColumn<LTLFilter, String> filterNameColumn;
    private FilterViewCreator filterViewCreator;

    @Override
    public void initialize() {
        final ImageButton graphic = new ImageButton();
        graphic.setImage(PLUS_ICON);
        createFilterTab.setGraphic(graphic);
        filterViewCreator = new FilterTabCreator(filterTabPane, contextEditor);
        filterTabPane.setOnMouseClicked(event -> {
            if (filterTabPane.getSelectionModel().getSelectedItem() == createFilterTab) {
                filterViewCreator.showCreateFilterEditionView();
            }
        });
        initializeListFilterTab();
    }

    private void initializeListFilterTab() {
        filtersTableView.getItems().addAll(gatherAllLTLFilters(FDIT_MANAGER.getRoot()));
        filtersTableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        filtersTableView.setRowFactory(param -> {
            final TableRow<LTLFilter> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == PRIMARY && event.getClickCount() == 2) {
                    filterViewCreator.showEditFilterEditionView(row.getItem());
                }
            });
            return row;
        });
        initializeFilterTableViewColumns();
    }

    private void initializeFilterTableViewColumns() {
        filterNameColumn.setCellValueFactory(param -> new ThreadSafeStringProperty(param.getValue().getName()));
    }

    @Override
    public void onClose() {
        for (final Tab tab : filterTabPane.getTabs()) {
            final EventHandler<Event> onCloseRequest = tab.getOnCloseRequest();
            if (onCloseRequest != null) {
                onCloseRequest.handle(null);
            }
        }
        filterTabPane.getTabs().clear();
    }

    public void editFilter(final LTLFilter filter) {
        filterViewCreator.showEditFilterEditionView(filter);
    }
}