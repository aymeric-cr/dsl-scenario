package fdit.gui.zoneEditor;

import fdit.gui.EditorController;
import fdit.gui.FditSplitPaneSkin;
import fdit.gui.utils.imageButton.ImageButton;
import fdit.gui.zoneEditor.tabs.zone.ListZonePaneController;
import fdit.gui.zoneEditor.tabs.zone.PolygonTabCreator;
import fdit.gui.zoneEditor.world.FditWorldZoneController;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import java.io.IOException;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.gui.Images.PLUS_ICON;
import static fdit.gui.utils.FXUtils.loadFxml;
import static fdit.gui.utils.FXUtils.setCursor;
import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;

public class ZoneEditorController implements EditorController {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(ZoneEditorController.class);
    private final ZoneEditorContext contextZone = new ZoneEditorContext();
    @FXML
    private SplitPane graphicalEditorPane;
    @FXML
    private StackPane viewWorldPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab createPolygonTab;
    @FXML
    private Tab listZoneTab;
    private ListZonePaneController listZonePaneController;
    private PolygonTabCreator polygonViewCreator;
    private ListenerHandle tabPaneSelectionListenerHandler;

    public ZoneEditorController() {
        LANGUAGES_MANAGER.addListener(observable -> initializeListZoneTabText());
    }

    @Override
    public void initialize() {
        try {
            graphicalEditorPane.setSkin(new FditSplitPaneSkin(graphicalEditorPane));
            polygonViewCreator = new PolygonTabCreator(tabPane, contextZone);
            final Node worldPane = createWorldPane();
            if (worldPane != null) {
                final ImageButton graphic = new ImageButton();
                graphic.setImage(PLUS_ICON);
                createPolygonTab.setGraphic(graphic);
                tabPaneSelectionListenerHandler = ListenerHandles.createAttached(tabPane.getSelectionModel().selectedItemProperty(),
                        (observable, oldValue, newValue) -> {
                            if (newValue == createPolygonTab) {
                                polygonViewCreator.showCreateZoneEditionView();
                            }
                        });
                initializeListZoneTab();
                Platform.runLater(() -> {
                    viewWorldPane.getChildren().add(worldPane);
                    if (graphicalEditorPane.getScene() != null) {
                        setCursor(Cursor.DEFAULT, graphicalEditorPane);
                    }
                });
            }
        } catch (final IOException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose() {
        listZonePaneController.onClose();
        tabPaneSelectionListenerHandler.detach();
        for (final Tab tab : tabPane.getTabs()) {
            final EventHandler<Event> onCloseRequest = tab.getOnCloseRequest();
            if (onCloseRequest != null) {
                onCloseRequest.handle(null);
            }
        }
        tabPane.getTabs().clear();
        tabPaneSelectionListenerHandler = null;

    }

    public void editZone(final Zone zone) {
        if (zone instanceof FditPolygon) {
            polygonViewCreator.showEditZoneEditionView(zone);
        }
    }

    private void initializeListZoneTab() throws IOException {
        tabPane.getSelectionModel().select(listZoneTab);
        initializeListZoneTabText();
        listZoneTab.setContent(createListZonePane());
    }

    private void initializeListZoneTabText() {
        listZoneTab.setText(TRANSLATOR.getMessage("tab.listView"));
        createPolygonTab.setText(TRANSLATOR.getMessage("tab.polygon"));
    }

    private Node createWorldPane() throws IOException {
        return loadFxml(getClass().getResource("/fdit/gui/graphicalScenarioEditor/world/world.fxml"),
                new FditWorldZoneController(contextZone));
    }

    private Node createListZonePane() throws IOException {
        listZonePaneController = new ListZonePaneController(polygonViewCreator);
        return loadFxml(getClass().getResource("/fdit/gui/zoneEditor/tabs/zone/listZonePane.fxml"),
                listZonePaneController);
    }
}