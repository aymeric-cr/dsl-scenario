package fdit.gui.application.treeView;

import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

public abstract class MenuableTreeCell<T> extends TreeCell<T> {

    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item == null ? "" : formatItem(item));
            final TreeItem<T> treeItem = getTreeItem();
            final Node node = formatGraphic(item);
            setGraphic(node);
            treeItem.setGraphic(node);
        }
    }

    protected abstract Node formatGraphic(final T item);

    protected abstract String formatItem(final T item);
}
