package fdit.gui.application;

import fdit.gui.application.notifications.LoadingNotifier;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.FditHistoryListener;
import fdit.tools.i18n.MessageTranslator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.sun.javafx.application.LauncherImpl.launchApplication;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.gui.Images.APPLCATION_ICON;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.application.notifications.Notification.successNotification;
import static fdit.gui.application.notifications.Notifier.showNotification;
import static fdit.gui.utils.FXUtils.createErrorDialog;
import static fdit.gui.utils.FXUtils.setTooltipStartTiming;
import static fdit.history.Command.CommandType.MAIN;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.getUserDirectoryPath;
import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;

public class FditApplication extends Application {

    public static final String USER_FDIT_PATH = getUserDirectoryPath() + separatorChar + ".fdit";
    private static final String DATABASE_FILE = USER_FDIT_PATH + separatorChar + "fdit-db";
    private static final String DATABASE_URL = "jdbc:h2:" + DATABASE_FILE;

    private static final File LOG_FILE = new File(USER_FDIT_PATH + separatorChar + ".log");

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditApplication.class);

    private static final Style STYLE = Style.LIGHT;

    private static Stage primaryStage;
    private static Application thisApplication;

    private final List<String> ignoredExceptions = newArrayList();
    private Scene scene;

    private static void saveInLogFile(final Throwable throwable) {
        try {
            final List<String> strings = newArrayList();
            final Path logPath = LOG_FILE.toPath();
            if (LOG_FILE.exists()) {
                strings.addAll(readAllLines(logPath));
            }
            strings.add(Instant.now().toString());
            strings.add(getStackTrace(throwable));
            strings.add("\n---------------------------------------------\n");
            write(logPath, strings);
        } catch (final Exception ignored) {
        }
    }

    public static void main(final String[] args) {
        launchApplication(FditApplication.class, FditPreloader.class, args);
    }

    public static Application getApplication() {
        return thisApplication;
    }

    @Override
    public void init() throws Exception {
        setDefaultUncaughtExceptionHandler(this::showError);
        FDIT_DATABASE.open(DATABASE_URL);
        FDIT_MANAGER.getCommandExecutor().getHistory().addListener(createHistoryListener());
        setTooltipStartTiming(1000);
        notifyPreloader(new Preloader.ProgressNotification(0.0));
        initializeIgnoredExceptions();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("window.fxml"));
        final Parent root = fxmlLoader.load();
        scene = new Scene(root);
        new JMetro(scene, STYLE);
        LoadingNotifier.setScene(scene);
        setHorizontalDividerPosition(primaryStage, root);
        primaryStage.setTitle(TRANSLATOR.getMessage("windowTitle"));
        primaryStage.getIcons().add(APPLCATION_ICON);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        FditApplication.primaryStage = primaryStage;
        FditApplication.thisApplication = this;
        primaryStage.show();
    }

    private void initializeIgnoredExceptions() {
        // TODO : temporary solution
        ignoredExceptions.add("com.sun.javafx.scene.traversal.TraversalEngine");
    }

    @Override
    public void stop() {
        FDIT_DATABASE.close();
        System.exit(0);
    }

    private FditHistoryListener createHistoryListener() {
        return new FditHistoryListener() {
            @Override
            public void commandExecuted(final Command command, final CommandType commandType) {
                if (commandType == MAIN) {
                    displayNotification(TRANSLATOR.getMessage("notification.commandExecuted",
                            command.getContent()));
                }
            }

            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                if (commandType == MAIN) {
                    displayNotification(TRANSLATOR.getMessage("notification.commandUndone", command.getContent()));
                }
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                if (commandType == MAIN) {
                    displayNotification(TRANSLATOR.getMessage("notification.commandRedone", command.getContent()));
                }
            }

            @Override
            public void commandFailed(final Command command, final Throwable throwable) {
                showError(currentThread(), throwable);
            }

            private void displayNotification(final String message) {
                Platform.runLater(() -> showNotification(successNotification(message), scene));
            }
        };
    }

    @SuppressWarnings("unused")
    private void showError(final Thread thread, final Throwable throwable) {
        Platform.runLater(() -> {
            if (isIgnoredException(throwable)) {
                return;
            }
            createErrorDialog(
                    TRANSLATOR.getMessage("errorMessage"), throwable).showAndWait();
            saveInLogFile(throwable);
            scene.setCursor(Cursor.DEFAULT);
        });
    }

    private boolean isIgnoredException(final Throwable throwable) {
        if (throwable.getStackTrace().length == 0) {
            return false;
        } else {
            return ignoredExceptions.contains(throwable.getStackTrace()[0].toString());
        }
    }

    private void setHorizontalDividerPosition(final Window primaryStage, final Parent root) {
        primaryStage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable,
                                final Boolean oldValue,
                                final Boolean newValue) {
                if (newValue) {
                    final SplitPane splitPane = (SplitPane) root.lookup("#verticalSplitPane");
                    splitPane.setDividerPositions(0.15);
                    observable.removeListener(this);
                }
            }
        });
    }
}