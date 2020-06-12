package fdit.gui.application.notifications;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import static fdit.gui.Images.WAITING_ICON;
import static java.lang.Boolean.TRUE;
import static javafx.util.Duration.millis;
import static javafx.util.Duration.seconds;

public final class Notifier {

    private static final double WIDTH = 300;
    private static final double HEIGHT = 50;

    private Notifier() {
    }

    public static void showNotification(final Notification notification, final Scene scene) {
        registerNotificationStylesheet(scene);
        final Label messageLabel = new Label(notification.getMessage(), notification.getIcon());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message");
        final Pane popupContent = new StackPane(messageLabel);
        popupContent.setMinSize(WIDTH, HEIGHT);
        popupContent.setMaxWidth(WIDTH);
        popupContent.getStyleClass().add("notification");
        final Popup popup = new Popup();
        final Window window = scene.getWindow();
        popup.setX(window.getX() + window.getWidth() - WIDTH - 10);
        popup.setY(window.getY() + window.getHeight() - HEIGHT - 10);
        popup.getContent().add(popupContent);
        final Timeline fadeAnimation = createFadeAnimation(popup, seconds(5));
        popup.show(window);
        Platform.runLater(fadeAnimation::play);
    }

    private static Timeline createFadeAnimation(final Popup popup, final Duration delay) {
        final KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1.0D);
        final KeyValue fadeOutEnd = new KeyValue(popup.opacityProperty(), 0.0D);
        final KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        final KeyFrame kfEnd = new KeyFrame(millis(500), fadeOutEnd);
        final Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(delay);
        timeline.setOnFinished((ActionEvent actionEvent) -> Platform.runLater(popup::hide));
        return timeline;
    }

    public static void showLoadingNotification(final StringProperty loadingMessage,
                                               final BooleanProperty stopProperty,
                                               final Scene scene) {
        registerNotificationStylesheet(scene);
        if (!stopProperty.get()) {
            final ImageView waitingIcon = new ImageView(WAITING_ICON);
            final Labeled messageLabel = new Label(loadingMessage.get(), waitingIcon);
            messageLabel.textProperty().bind(loadingMessage);
            messageLabel.getStyleClass().add("message");
            final VBox popupLayout = new VBox();
            popupLayout.setSpacing(10.0D);
            popupLayout.setPadding(new Insets(10.0D, 10.0D, 10.0D, 10.0D));
            popupLayout.getChildren().add(messageLabel);
            final Pane popupContent = new StackPane();
            popupContent.setPrefSize(WIDTH, HEIGHT);
            popupContent.getStyleClass().add("notification");
            popupContent.getChildren().add(popupLayout);
            final Popup popup = new Popup();
            final Window window = scene.getWindow();
            popup.setX((window.getX() + window.getWidth() - WIDTH) / 2);
            popup.setY((window.getY() + window.getHeight() - HEIGHT) / 2);
            popup.getContent().add(popupContent);
            final Timeline fadeAnimation = createLoadingAnimation(waitingIcon);
            popup.show(window);
            stopProperty.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(final ObservableValue<? extends Boolean> observable,
                                    final Boolean oldValue,
                                    final Boolean newValue) {
                    if (TRUE.equals(newValue)) {
                        fadeAnimation.stop();
                        popup.hide();
                        stopProperty.removeListener(this);
                    }
                }
            });
            Platform.runLater(fadeAnimation::play);
        }
    }

    private static Timeline createLoadingAnimation(final Node icon) {
        final KeyValue rotationBegin = new KeyValue(icon.rotateProperty(), 0);
        final KeyValue rotationEnd = new KeyValue(icon.rotateProperty(), 360);
        final KeyFrame kfRotationBegin = new KeyFrame(Duration.ZERO, rotationBegin);
        final KeyFrame kfRotationEnd = new KeyFrame(millis(1500), rotationEnd);
        final Timeline timeline = new Timeline(kfRotationBegin, kfRotationEnd);
        timeline.setOnFinished((ActionEvent actionEvent) ->
                Platform.runLater(timeline::play));
        return timeline;
    }

    private static void registerNotificationStylesheet(final Scene scene) {
        final String stylesheetUrl = Notifier.class.getResource("notification.css").toExternalForm();
        if (!scene.getStylesheets().contains(stylesheetUrl)) {
            scene.getStylesheets().add(stylesheetUrl);
        }
    }
}