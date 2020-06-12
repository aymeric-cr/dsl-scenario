package fdit.gui.utils;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.ComboBox;

public class UpdateableComboBox<T> extends ComboBox<T> {

    public UpdateableComboBox() {
        setSkin(new UpdateableComboBoxSkin<T>(this));
    }

    public void refresh() {
        ((UpdateableComboBoxSkin) getSkin()).refeshView();
    }

    static class UpdateableComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        /***************************************************************************
         *                                                                         *
         * Constructors                                                            *
         *                                                                         *
         *************************************************************************
         * @param comboBox*/
        UpdateableComboBoxSkin(final ComboBox<T> comboBox) {
            super(comboBox);
        }

        void refeshView() {
            getListView().refresh();
            updateDisplayNode();
        }
    }
}
