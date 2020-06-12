package fdit.gui.utils.dialog;

import fdit.gui.utils.FditAlert;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Collection;

import static fdit.gui.utils.FXUtils.createConfirmationDialog;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.filter;
import static java.util.Collections.singleton;

public final class DeletionConfirmationUtils {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(DeletionConfirmationUtils.class);

    private DeletionConfirmationUtils() {
    }

    public static boolean confirmDeletion(final FditElement element) {
        return confirmDeletion(singleton(element));
    }

    public static boolean confirmDeletion(final Collection<FditElement> elements) {
        final StringBuilder message = new StringBuilder();
        if (elements.size() == 1) {
            message.append(TRANSLATOR.getMessage("confirmation.deletion.singular"));
        } else {
            message.append(TRANSLATOR.getMessage("confirmation.deletion.plural"));
        }
        final Collection<Zone> zones = filter(elements, Zone.class);
        final Alert confirmationDialog = createConfirmationDialog(message.toString());
        confirmationDialog.showAndWait();
        return confirmationDialog.getResult() == ButtonType.OK;
    }

    public static void createAlertDeletionImpossible(final Collection<FditElement> elements) {
        final StringBuilder message = new StringBuilder(TRANSLATOR.getMessage("confirmation.deletion.impossible"));
        for (final FditElement element : elements) {
            message.append('\n');
            message.append(element.getName());
        }
        final Alert dialog = new FditAlert(AlertType.WARNING, message.toString());
        dialog.showAndWait();
    }
}
