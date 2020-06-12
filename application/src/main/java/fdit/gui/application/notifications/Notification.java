package fdit.gui.application.notifications;

import javafx.scene.image.ImageView;

import static fdit.gui.Images.ERROR_ICON;
import static fdit.gui.Images.SUCCESS_ICON;

public class Notification {

    private final String message;
    private final ImageView icon;

    private Notification(final String message, final ImageView icon) {
        this.message = message;
        this.icon = icon;
    }

    public static Notification successNotification(final String message) {
        return new Notification(message, new ImageView(SUCCESS_ICON));
    }

    public static Notification errorNotification(final String message) {
        return new Notification(message, new ImageView(ERROR_ICON));
    }

    String getMessage() {
        return message;
    }

    ImageView getIcon() {
        return icon;
    }
}