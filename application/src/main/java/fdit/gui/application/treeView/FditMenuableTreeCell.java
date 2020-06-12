package fdit.gui.application.treeView;

import fdit.metamodel.element.FditElement;
import fdit.metamodel.recording.Recording;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.Images.ARROW_DOWN;
import static fdit.gui.Images.ARROW_RIGHT;
import static fdit.gui.utils.FXUtils.getImageView;

public final class FditMenuableTreeCell extends MenuableTreeCell<FditElement> {

    private final Map<Recording, RecordingCell> recordingCells = newHashMap();

    public FditMenuableTreeCell() {
    }

    private static RecordingCell formatRecordingCell(final Recording recording) {
        final VBox node = new VBox();
        final RecordingCell recordingCell = new RecordingCell(node);
        node.setFillWidth(true);
        final Rectangle arrowRight = newArrowRight();
        final Pane itemName = new HBox(arrowRight,
                getImageView(recording),
                new Label(recording.getName()));
        node.getChildren().add(itemName);
        node.getStyleClass().add("filebrowserScenarioListCell");
        return recordingCell;
    }

    private static Rectangle newArrowRight() {
        return new Rectangle(ARROW_DOWN.getWidth(),
                ARROW_DOWN.getHeight(),
                new ImagePattern(ARROW_RIGHT));
    }

    @Override
    protected Node formatGraphic(final FditElement item) {
        if (item instanceof Recording) {
            if (recordingCells.containsKey(item)) {
                return recordingCells.get(item).getRecordingNode();
            }
            final RecordingCell recordingCell = formatRecordingCell((Recording) item);
            recordingCells.put((Recording) item, recordingCell);
            return recordingCell.getRecordingNode();
        }
        return getImageView(item);
    }

    @Override
    protected String formatItem(final FditElement element) {
        if (element instanceof Recording) {
            return null;
        }
        return element.getName();
    }
}