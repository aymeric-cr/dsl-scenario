package fdit.gui.application;

import fdit.metamodel.element.FditElement;
import fdit.storage.FditStorageUtils;

import java.io.File;

import static fdit.gui.application.FditManager.FDIT_MANAGER;

public final class FditManagerUtils {

    private FditManagerUtils() {
    }

    public static File getFditElementFile(final FditElement element) {
        return FditStorageUtils.getFditElementFile(element, FDIT_MANAGER.getRootFile());
    }
}