package fdit.gui.utils;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ListView;

public class CustomCompletionPopupSkin<T> extends AutoCompletePopupSkin<T> {

    private static final int LIST_CELL_HEIGHT = 24;

    public CustomCompletionPopupSkin(final AutoCompletePopup<T> control) {
        super(control);
        final ListView<?> suggestionList = (ListView<?>) getNode();
        suggestionList.prefHeightProperty().bind(
                Bindings.min(control.visibleRowCountProperty(), Bindings.size(suggestionList.getItems()))
                        .multiply(LIST_CELL_HEIGHT).add(10));
    }
}