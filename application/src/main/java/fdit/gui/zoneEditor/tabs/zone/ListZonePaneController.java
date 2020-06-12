package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.EditorController;
import fdit.gui.application.FditManager;
import fdit.gui.application.FditManagerListener;
import fdit.gui.utils.ThreadSafeObjectProperty;
import fdit.gui.utils.ThreadSafeStringProperty;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.codefx.libfx.listener.handle.ListenerHandle;

import java.util.function.Function;

import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.element.DirectoryUtils.gatherAllZones;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.lang.String.valueOf;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.codefx.libfx.listener.handle.ListenerHandles.createFor;

public class ListZonePaneController implements EditorController {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ListZonePaneController.class);
    private final PolygonTabCreator polygonTabCreator;
    @FXML
    private ScrollPane listZone;
    @FXML
    private TableView<Zone> zoneTableView;
    @FXML
    private TableColumn<Zone, String> zoneNameColumn;
    @FXML
    private TableColumn<Zone, String> zoneLowerAltitudeColumn;
    @FXML
    private TableColumn<Zone, String> zoneUpperAltitudeColumn;
    @FXML
    private TableColumn<Zone, Zone> zoneVerticesColumn;
    private ListenerHandle fditManagerListenerHandler;

    public ListZonePaneController(final PolygonTabCreator polygonTabCreator) {
        this.polygonTabCreator = polygonTabCreator;
        LANGUAGES_MANAGER.addListener(observable -> initializeColumnsTexts());
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static void intializeCellContent(final Zone item,
                                             final boolean empty,
                                             final Labeled tableCell,
                                             final Function<Zone, String> zoneFormat) {
        if (item == null || empty) {
            tableCell.setText(null);
            tableCell.setText("");
        } else {
            tableCell.setText(zoneFormat.apply(item));
        }
    }

    @Override
    public void initialize() {
        initializeZoneTableView();
        fditManagerListenerHandler = createFor(FDIT_MANAGER, createFditManagerListener())
                .onAttach(FditManager::addListener)
                .onDetach(FditManager::removeListener)
                .buildAttached();
    }

    @Override
    public void onClose() {
        fditManagerListenerHandler.detach();
        fditManagerListenerHandler = null;
    }

    private void initializeZoneTableView() {
        zoneTableView.getItems().addAll(gatherAllZones(FDIT_MANAGER.getRoot()));
        zoneTableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        zoneTableView.setRowFactory(param -> {
            final TableRow<Zone> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == PRIMARY && event.getClickCount() == 2) {
                    if (row.getItem() instanceof FditPolygon) {
                        polygonTabCreator.showEditZoneEditionView(row.getItem());
                    }
                }
            });
            return row;
        });
        initializeZoneViewColumns();
    }

    private FditManagerListener createFditManagerListener() {
        return new FditManagerListener() {
            @Override
            public void elementAdded(final FditElement createdElement) {
                if (createdElement instanceof Zone) {
                    Platform.runLater(() -> zoneTableView.getItems().add(0, (Zone) createdElement));
                }
            }

            @Override
            public void elementRemoved(final FditElement removedElement) {
                if (removedElement instanceof Zone) {
                    Platform.runLater(() -> zoneTableView.getItems().remove(removedElement));
                }
            }
        };
    }

    private void initializeZoneViewColumns() {
        initializeColumnsTexts();

        zoneNameColumn.setCellValueFactory(param -> new ThreadSafeStringProperty(param.getValue().getName()));

        zoneLowerAltitudeColumn.setCellValueFactory(param ->
                new ThreadSafeStringProperty(valueOf(param.getValue().getAltitudeLowerBound())));

        zoneUpperAltitudeColumn.setCellValueFactory(param ->
                new ThreadSafeStringProperty(valueOf(param.getValue().getAltitudeUpperBound())));

        zoneVerticesColumn.setCellValueFactory(param -> new ThreadSafeObjectProperty<>(param.getValue()));
        zoneVerticesColumn.setCellFactory(createZoneTypeCellFactory());

    }

    private void initializeColumnsTexts() {
        zoneNameColumn.setText(TRANSLATOR.getMessage("title.zoneName"));
        zoneLowerAltitudeColumn.setText(TRANSLATOR.getMessage("title.lowerAltitude"));
        zoneUpperAltitudeColumn.setText(TRANSLATOR.getMessage("title.upperAltitude"));
        zoneVerticesColumn.setText(TRANSLATOR.getMessage("title.vertices"));
    }

    private Callback<TableColumn<Zone, Zone>, TableCell<Zone, Zone>> createZoneTypeCellFactory() {
        return param -> new TableCell<Zone, Zone>() {
            final Function<Zone, String> zoneFormat = ((Zone.ZoneVisitor<String>) polygon -> {
                final StringBuilder verticeToStringBuilder = new StringBuilder();
                verticeToStringBuilder.append('[');
                for (final Coordinates coordinates : polygon.getVertices()) {
                    verticeToStringBuilder
                            .append(" (")
                            .append(coordinates.getLatitude())
                            .append(';')
                            .append(coordinates.getLongitude())
                            .append(") ,");
                }
                final int length = verticeToStringBuilder.length();
                verticeToStringBuilder.replace(length - 1, length, "]");
                return verticeToStringBuilder.toString();
            })::accept;

            @Override
            protected void updateItem(final Zone item, final boolean empty) {
                super.updateItem(item, empty);
                intializeCellContent(item, empty, this, zoneFormat);
            }
        };
    }
}
