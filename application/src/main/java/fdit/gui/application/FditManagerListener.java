package fdit.gui.application;

import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.TreeItem;

public interface FditManagerListener {

    default void rootChanged() {
    }

    default void rootTreeItemChanged() {
    }

    default void elementChanged(final Change<? extends TreeItem<FditElement>> elementSelected) {

    }

    default void elementAdded(final FditElement elementAdded) {

    }

    default void elementRemoved(final FditElement elementRemoved) {

    }

    default void elementEdited(final FditElement editedElement) {

    }

    default void elementRenamed(final FditElement editedElement) {

    }

    default void elementMoved(final FditElement movedElement, final Directory oldFather) {

    }
}
