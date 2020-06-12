package fdit.gui.application.treeView;

import javafx.scene.Node;

public class RecordingCell {
    private final Node recordingNode;

    public RecordingCell(final Node recordingNode) {
        this.recordingNode = recordingNode;
    }

    public Node getRecordingNode() {
        return recordingNode;
    }
}
