package fdit.gui.application.treeView;

import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static fdit.tools.stream.StreamUtils.filter;

public final class DraggableTreeView {

    private DraggableTreeView() {
    }

    public static Callback<TreeView<FditElement>, TreeCell<FditElement>>
    initializeDraggableCellFactory(final Supplier<MenuableTreeCell<FditElement>> menuableTreeCellSupplier,
                                   final BiConsumer<FditElement, Directory> moveAction) {
        final FditElement[] itemDragged = new FditElement[1];
        return treeView -> {
            final MenuableTreeCell<FditElement> menuableTreeCell = menuableTreeCellSupplier.get();

            addSelectionListener(treeView, menuableTreeCell);

            menuableTreeCell.setOnDragDetected(event -> {
                final FditElement item = menuableTreeCell.getItem();
                if (item == null) {
                    return;
                }
                final Dragboard dragBoard = menuableTreeCell.startDragAndDrop(TransferMode.MOVE);
                final Map content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, item.getName());
                dragBoard.setContent(content);
                itemDragged[0] = item;
                event.consume();
            });
            menuableTreeCell.setOnDragDone(Event::consume);

            menuableTreeCell.setOnDragOver(dragEvent -> {
                if (itemDragged[0] != null) {
                    if (itemDragged[0] != menuableTreeCell.getItem()) {
                        menuableTreeCell.setStyle("-fx-border-color:darkblue;");
                        dragEvent.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                dragEvent.consume();
            });

            menuableTreeCell.setOnDragExited(dragEvent -> menuableTreeCell.setStyle("-fx-border-color:white;"));

            menuableTreeCell.setOnDragDropped(dragEvent -> {
                final FditElement item = menuableTreeCell.getItem();
                final FditElement itemToMove = itemDragged[0];
                final Directory newFather;
                if (item instanceof Directory) {
                    newFather = (Directory) item;
                } else {
                    newFather = item.getFather();
                }
                moveAction.accept(itemToMove, newFather);
                itemDragged[0] = null;
                dragEvent.consume();
            });

            return menuableTreeCell;
        };
    }

    private static void addSelectionListener(final TreeView<FditElement> treeView,
                                             final MenuableTreeCell<FditElement> treeCell) {
        final MultipleSelectionModel<TreeItem<FditElement>> selectionModel = treeView.getSelectionModel();
        selectionModel.getSelectedItems().addListener((ListChangeListener<TreeItem<FditElement>>) change -> {
            final ObservableList<? extends TreeItem<FditElement>> changeList = change.getList();
            final FditElement element = treeCell.getItem();
            for (final MenuableTreeItem<FditElement> treeItem : filter(changeList, MenuableTreeItem.class)) {
                if (treeItem.getValue() == element) {
                    treeCell.setContextMenu(treeItem.getContextMenu());
                }
            }
        });
    }
}