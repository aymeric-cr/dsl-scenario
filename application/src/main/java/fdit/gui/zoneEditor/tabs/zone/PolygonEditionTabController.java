package fdit.gui.zoneEditor.tabs.zone;

import fdit.gui.utils.imageButton.ImageButton;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.Images.DELETE_CROSS_ICON;
import static fdit.gui.Images.PLUS_ICON;
import static fdit.gui.utils.FXUtils.setOnPrimaryButtonMouseClicked;
import static javafx.scene.input.KeyCode.ENTER;
import static org.codefx.libfx.listener.handle.ListenerHandles.createAttached;

public class PolygonEditionTabController extends ZoneEditionTabController {

    private final HashMap<DoubleProperty[], Node> coordinatesNode = newHashMap();

    public PolygonEditionTabController(final Tab zoneEditionTab,
                                       final PolygonEditionModel polygonEditionModel) {
        super(zoneEditionTab, polygonEditionModel);
    }

    @Override
    protected void initializeLabel() {
        super.initializeLabel();
        shapeNameLabel.setText(TRANSLATOR.getMessage("label.shapeName.polygon"));
    }

    @Override
    protected void initializeData() {
        addNewPolygonPointButton((PolygonEditionModel) zoneEditionModel);
        final ObservableList<DoubleProperty[]> vertices = ((PolygonEditionModel) zoneEditionModel).getVertices();
        for (final DoubleProperty[] vertex : vertices) {
            addVertexNode(vertex);
        }
        listenerHandles.add(createAttached(vertices, (ListChangeListener<? super DoubleProperty[]>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (final DoubleProperty[] vertex : change.getAddedSubList()) {
                        addVertexNode(vertex);
                    }
                }
                if (change.wasRemoved()) {
                    for (final DoubleProperty[] vertex : change.getRemoved()) {
                        removeVertexNode(vertex);
                    }
                }
            }
        }));
    }

    private void addNewPolygonPointButton(final PolygonEditionModel zoneEditionModel) {
        final ImageButton graphic = new ImageButton();
        graphic.setImage(PLUS_ICON);
        final ButtonBase addPolygonPointButton = new Button("", graphic);
        addPolygonPointButton.setOnAction(event -> zoneEditionModel.addNewVertex());
        addPolygonPointButton.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                zoneEditionModel.addNewVertex();
            }
        });
        shapeCreationData.getChildren().add(addPolygonPointButton);
    }

    private void removeVertexNode(final DoubleProperty[] vertex) {
        final Node remove = coordinatesNode.remove(vertex);
        Platform.runLater(() -> shapeCreationData.getChildren().remove(remove));
    }

    private void addVertexNode(final DoubleProperty[] polygonPoint) {
        final Node aircraftPosition = createPolygonPointNode(polygonPoint);
        final ObservableList<Node> children = shapeCreationData.getChildren();
        coordinatesNode.put(polygonPoint, aircraftPosition);
        Platform.runLater(() -> children.add(children.size() - 1, aircraftPosition));
    }

    private Node createPolygonPointNode(final DoubleProperty[] polygonPoint) {
        polygonPoint[0].set((double) Math.round(polygonPoint[0].get() * 100000) / 100000);
        polygonPoint[1].set((double) Math.round(polygonPoint[1].get() * 100000) / 100000);
        final Pane polygonPointContainer = new VBox();
        polygonPointContainer.getStyleClass().add("polygon-point-container");
        initializeLatitudeTextField(polygonPoint, polygonPointContainer);
        initializeLongitudeTextField(polygonPoint, polygonPointContainer);
        final Pane polygonPointPane = new HBox();
        final ImageButton deleteButton = new ImageButton();
        deleteButton.setImage(DELETE_CROSS_ICON);
        polygonPointPane.getChildren().addAll(polygonPointContainer, deleteButton);
        setOnPrimaryButtonMouseClicked(deleteButton, event -> ((PolygonEditionModel) zoneEditionModel).removeVertex(polygonPoint));
        polygonPointPane.getStyleClass().add("polygon-point-pane");
        return polygonPointPane;
    }
}