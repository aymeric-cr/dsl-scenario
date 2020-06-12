package fdit.gui.application.treeView;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

public abstract class MenuableTreeItem<T> extends TreeItem<T> {

    MenuableTreeItem(final T value) {
        super(value);
    }

    public abstract ContextMenu getContextMenu();
}
