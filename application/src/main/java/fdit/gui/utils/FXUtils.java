package fdit.gui.utils;

import fdit.gui.application.concurrent.AsyncExecution;
import fdit.metamodel.FditElementVisitor;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.tools.functional.ThrowableRunnable;
import fdit.tools.functional.ThrowableSupplier;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.function.Consumer;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.gui.Images.*;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static javafx.geometry.Insets.EMPTY;
import static javafx.scene.input.KeyCode.Y;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.layout.BorderWidths.DEFAULT;

public final class FXUtils {

    private FXUtils() {
    }

    public static void setCursor(final Cursor cursor, final Node node) {
        node.getScene().setCursor(cursor);
    }

    public static <T> TreeItem<T> findTreeItem(final TreeView<T> treeView, final T value) {
        return findTreeItem(treeView.getRoot(), value);
    }

    public static void sortTreeItem(final TreeItem<FditElement> fatherTreeItem) {
        fatherTreeItem.getChildren().sort((o1, o2) ->
                o1.getValue().getName().compareToIgnoreCase(o2.getValue().getName()));
    }

    public static Alert createConfirmationDialog(final String message) {
        return new FditAlert(AlertType.CONFIRMATION, message);
    }

    public static Alert createWarningConfirmationDialog(final String message) {
        return new FditAlert(AlertType.WARNING, message);
    }

    public static Alert createErrorDialog(final String message, final Throwable throwable) {
        final StringBuilder messageBuilder = new StringBuilder(message);
        final String exceptionMessage = throwable.getMessage();
        if (exceptionMessage != null) {
            messageBuilder.append(" : ").append(exceptionMessage);
        }
        return new FditAlert(AlertType.ERROR, messageBuilder.toString());
    }

    public static Alert createErrorDialog(final String message, final String errorDetail) {
        return new FditAlert(AlertType.ERROR, message + " : " + errorDetail);
    }

    public static Alert createErrorDialog(final String message) {
        return new FditAlert(AlertType.ERROR, message);
    }

    public static AsyncExecution<Void> createSimpleAsyncExecution(final ThrowableRunnable runnable) {
        return createReturningAsyncExecution(() -> {
            runnable.runThrows();
            return null;
        });
    }

    public static void startRunnableInBackground(final ThrowableRunnable runnable) {
        createSimpleAsyncExecution(runnable).startInBackground();
    }

    public static void startRunnableInUIThread(final ThrowableRunnable runnable) {
        createSimpleAsyncExecution(runnable).startInUIThread();
    }

    public static <T> AsyncExecution<T> createReturningAsyncExecution(final ThrowableSupplier<T> supplier) {
        final AsyncExecution<T> asyncExecution = FDIT_MANAGER.getAsyncExecutionFactory().createAsyncExecution();
        asyncExecution.onStart(supplier);
        return asyncExecution;
    }

    public static <T> T loadFxml(final URL fxmlLocation, final Initializable controller) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        fxmlLoader.setController(controller);
        return fxmlLoader.load();
    }


    public static void setOnPrimaryButtonMouseClicked(final Node node, final Consumer<MouseEvent> eventConsumer) {
        node.setOnMouseClicked(event -> {
            if (event.getButton() == PRIMARY) {
                eventConsumer.accept(event);
            }
        });
    }

    public static void setOnDoubleClick(final Node node, final Consumer<MouseEvent> eventConsumer) {
        node.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == PRIMARY) {
                eventConsumer.accept(event);
            }
        });
    }

    public static void removeMouseEvents(final Node node) {
        node.setOnMouseClicked(null);
        node.setOnMouseExited(null);
        node.setOnMouseEntered(null);
        node.setOnMouseDragEntered(null);
        node.setOnMouseDragExited(null);
        node.setOnMouseDragged(null);
        node.setOnMouseDragOver(null);
        node.setOnMouseDragReleased(null);
        node.setOnMouseMoved(null);
        node.setOnMouseReleased(null);
        node.setOnMousePressed(null);
        node.setOnScroll(null);
    }


    public static void uninstallTooltip(final Node node) {
        node.getProperties().clear();
    }

    public static void installContextMenu(final Node node, final PopupWindow contextMenu) {
        if (node != null) {
            node.setOnContextMenuRequested(event -> {
                contextMenu.show(node, event.getScreenX(), event.getScreenY());
                event.consume();
            });
            node.setOnMouseClicked(event -> {
                if (!event.isSecondaryButtonDown()) {
                    contextMenu.hide();
                    event.consume();
                }
            });
        }
    }

    public static void removeContextMenu(final Node node) {
        node.setOnContextMenuRequested(null);
    }

    public static void positionCaretToEnd(final TextInputControl textField) {
        textField.positionCaret(textField.getText().length());
    }

    public static void initializeTextFieldShortcuts(final Node textField) {
        textField.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                if (event.getCode() == Z) {
                    FDIT_MANAGER.getCommandExecutor().undo();
                    event.consume();
                }
                if (event.getCode() == Y) {
                    FDIT_MANAGER.getCommandExecutor().redo();
                    event.consume();
                }
            }
        });
    }

    /*public static void requestClose(final Tab tab) {
        final TabPaneBehavior behavior = getBehavior(tab);
        if (behavior.canCloseTab(tab)) {
            behavior.closeTab(tab);
        }
    }*/

    /*private static TabPaneBehavior getBehavior(final Tab tab) {
        return ((TabPaneSkin) tab.getTabPane().getSkin()).getBehavior();
    }*/

    public static void setTooltipStartTiming(final double millis) {
        try {
            Class TTBehaviourClass = null;
            final Class<?>[] declaredClasses = Tooltip.class.getDeclaredClasses();
            for (final Class clazz : declaredClasses) {
                if ("javafx.scene.control.Tooltip.TooltipBehavior".equals(clazz.getCanonicalName())) {
                    TTBehaviourClass = clazz;
                    break;
                }
            }
            if (TTBehaviourClass == null) {
                return;
            }
            final Constructor constructor = TTBehaviourClass.getDeclaredConstructor(
                    Duration.class, Duration.class, Duration.class, boolean.class);
            if (constructor == null) {
                return;
            }
            constructor.setAccessible(true);
            final Object newTTBehaviour = constructor.newInstance(
                    new Duration(millis), new Duration(15000),
                    new Duration(200), false);
            if (newTTBehaviour == null) {
                return;
            }
            final Field ttbehaviourField = Tooltip.class.getDeclaredField("BEHAVIOR");
            if (ttbehaviourField == null) {
                return;
            }
            ttbehaviourField.setAccessible(true);
            ttbehaviourField.set(Tooltip.class, newTTBehaviour);

        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException("Error during tooltip duration setting");
        }
    }


    public static Background createBackground(final Paint color) {
        return new Background(new BackgroundFill(color,
                CornerRadii.EMPTY,
                EMPTY));
    }


    public static Border createBorder(final Paint color) {
        return new Border(new BorderStroke(color, SOLID, CornerRadii.EMPTY, DEFAULT));
    }

    public static ImageView getImageView(final FditElement element) {
        return new FditElementVisitor<ImageView>() {
            @Override
            public ImageView visitDirectory(final Directory directory) {
                return new ImageView(DIRECTORY_ICON);
            }

            @Override
            public ImageView visitSchema(final Schema schema) {
                return new ImageView(TEXTUAL_SCENARIO_ICON);
            }

            @Override
            public ImageView visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                return null;
            }

            @Override
            public ImageView visitExecution(final Execution execution) {
                return new ImageView(START_EXECUTION_ICON);
            }

            @Override
            public ImageView visitRecording(final Recording recording) {
                return new ImageView(getRecordingIcon(recording));
            }

            @Override
            public ImageView visitZone(final Zone zone) {
                return new ImageView(ZONES_ICON);
            }

            @Override
            public ImageView visitLTLFilter(final LTLFilter ltlFilter) {
                return new ImageView(LTLFILTER_ICON);
            }

            @Override
            public ImageView visitAlterationTrigger(ActionTrigger trigger) {
                return new ImageView(TRIGGER_ICON);
            }
        }.accept(element);
    }

    private static <T> TreeItem<T> findTreeItem(final TreeItem<T> father, final T value) {
        if (father.getValue() == value) {
            return father;
        }
        for (final TreeItem<T> child : father.getChildren()) {
            final TreeItem<T> foundTreeItem = findTreeItem(child, value);
            if (foundTreeItem != null) {
                return foundTreeItem;
            }
        }
        return null;
    }
}