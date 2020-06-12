package fdit.gui.application;

import fdit.gui.application.commands.FditElementCommand;
import fdit.gui.application.commands.FditElementCommand.OperationType;
import fdit.gui.application.concurrent.AsyncExecutionFactory;
import fdit.gui.application.concurrent.FXAsyncExecutionFactory;
import fdit.gui.application.treeView.FditTreeItem;
import fdit.history.Command;
import fdit.history.Command.CommandType;
import fdit.history.CommandExecutor;
import fdit.history.FditHistoryListener;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static fdit.gui.application.FditManagerUtils.getFditElementFile;
import static fdit.gui.application.ProjectLoader.importFditElement;
import static fdit.gui.application.ProjectLoader.loadProject;
import static fdit.gui.application.commands.FditElementCommand.OperationType.EDITION;
import static fdit.storage.FditElementExtensions.suffixWithExtension;
import static fdit.tools.io.FileUtils.deleteSubDirStartWith;
import static org.apache.commons.io.FileUtils.*;

@SuppressWarnings("Singleton")
public final class FditManager {

    public static final FditManager FDIT_MANAGER = new FditManager();
    private static final String VERSION = "1.4.1";
    private final Map<String, Locale> languages = newHashMap();
    private Character separator;
    private FditTreeItem rootTreeItem;
    private CommandExecutor commandExecutor;
    private Root root;
    private File rootFile;
    private Collection<FditManagerListener> listeners;
    private AsyncExecutionFactory asyncExecutionFactory;

    private FditManager() {
        initialize();
    }

    public void initialize() {
        if (SystemUtils.IS_OS_WINDOWS) {
            separator = '\\';
        }
        if (SystemUtils.IS_OS_UNIX) {
            separator = '/';
        }
        commandExecutor = new CommandExecutor();
        commandExecutor.getHistory().addListener(new FditHistoryListener() {
            @Override
            public void commandExecuted(final Command command, final CommandType commandType) {
                if (command instanceof FditElementCommand) {
                    final FditElementCommand<?> fditElementCommand = (FditElementCommand<?>) command;
                    fireFditElementEvent(fditElementCommand.getSubject(), fditElementCommand.getOperationType());
                }
            }

            @Override
            public void commandUndone(final Command command, final CommandType commandType) {
                if (command instanceof FditElementCommand) {
                    final FditElementCommand<?> fditElementCommand = (FditElementCommand<?>) command;
                    fireFditElementEvent(fditElementCommand.getSubject(), fditElementCommand.getOperationType());
                }
            }

            @Override
            public void commandRedone(final Command command, final CommandType commandType) {
                if (command instanceof FditElementCommand) {
                    final FditElementCommand<?> fditElementCommand = (FditElementCommand<?>) command;
                    fireFditElementEvent(fditElementCommand.getSubject(), fditElementCommand.getOperationType());
                }
            }

            private void fireFditElementEvent(final FditElement subject, final OperationType operationType) {
                if (operationType == EDITION) {
                    listeners.forEach(listener -> listener.elementEdited(subject));
                }
            }
        });
        initializeLanguages();
        listeners = newArrayList();
        asyncExecutionFactory = new FXAsyncExecutionFactory();
    }

    private void initializeLanguages() {
        languages.put("English", Locale.ENGLISH);
        languages.put("Français", Locale.FRANCE);
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public Root getRoot() {
        return root;
    }

    public File getRootFile() {
        return rootFile;
    }

    Map<String, Locale> getLanguages() {
        return languages;
    }

    public void loadRoot(final File rootFile, final RecordingInDatabaseLoadingCallback callback) {
        commandExecutor.clear();
        this.rootFile = rootFile;
        root = ProjectLoader.loadRoot(rootFile);
        loadProject(root, rootFile, callback);
        listeners.forEach(FditManagerListener::rootChanged);
    }

    public void deleteHiddenDirectories() throws IOException {
        if (rootFile == null) {
            return;
        }
        deleteSubDirStartWith(rootFile, ".");
    }

    public void reloadRoot(final RecordingInDatabaseLoadingCallback callback) {
        if (rootFile == null) {
            return;
        }
        loadRoot(rootFile, callback);
    }

    public void importFile(final File file, final RecordingInDatabaseLoadingCallback callback) throws IOException {
        copyFileToDirectory(file, rootFile);
        importFditElement(root, file, root, rootFile, clazz ->
                BaseStationRecording.class.equals(clazz) ||
                        SiteBaseStationRecording.class.equals(clazz), callback).forEach(fditElement ->
                listeners.forEach(listener -> listener.elementAdded(fditElement)));
    }

    public void addFditElement(final FditElement element, final Directory father) {
        element.setFather(father);
        father.addChild(element);
        listeners.forEach(listener -> listener.elementAdded(element));
    }

    public void removeFditElement(final FditElement element) throws IOException {
        if (element instanceof Root) {
            throw new IllegalArgumentException("Cannot delete root");
        }
        final File elementFile = getFditElementFile(element);
        forceDelete(elementFile);
        element.getFather().removeChild(element);
        element.setFather(null);
        if (element instanceof Directory) {
            for (final FditElement child : ((Directory) element).getChildren()) {
                listeners.forEach(listener -> listener.elementRemoved(child));
            }
        }
        listeners.forEach(listener -> listener.elementRemoved(element));
    }

    public void renameFditElement(final FditElement element, final String newName) throws IOException {
        final File oldFile = getFditElementFile(element);
        final String completeFileName;
        if (element instanceof Directory) {
            completeFileName = newName;
        } else {
            completeFileName = suffixWithExtension(newName, element.getClass());
        }
        final File newFile = new File(oldFile.getParent(), completeFileName);
        Files.move(oldFile.toPath(), newFile.toPath());
        element.setName(newName);
        if (element instanceof Root) {
            rootFile = newFile;
        }
        listeners.forEach(listener -> listener.elementRenamed(element));
    }

    public void moveFditElement(final FditElement element, final Directory newFather) throws IOException {
        if (element instanceof Root) {
            throw new IllegalArgumentException("Cannot move root");
        }
        final File newFatherFile = getFditElementFile(newFather);
        final File elementFile = getFditElementFile(element);
        if (element instanceof Directory) {
            moveDirectoryToDirectory(elementFile, newFatherFile, false);
        } else {
            moveFileToDirectory(elementFile, newFatherFile, false);
        }
        final Directory oldFather = element.getFather();
        oldFather.removeChild(element);
        newFather.addChild(element);
        element.setFather(newFather);
        listeners.forEach(listener -> listener.elementMoved(element, oldFather));
    }

    public AsyncExecutionFactory getAsyncExecutionFactory() {
        return asyncExecutionFactory;
    }

    public void setAsyncExecutionFactory(final AsyncExecutionFactory asyncExecutionFactory) {
        this.asyncExecutionFactory = asyncExecutionFactory;
    }

    FditTreeItem getRootTreeItem() {
        return rootTreeItem;
    }

    void setRootTreeItem(final FditTreeItem rootTreeItem) {
        this.rootTreeItem = rootTreeItem;
        rootTreeItem.getSelectedItems().addListener((ListChangeListener<? super TreeItem<FditElement>>) observable ->
                listeners.forEach(fditManagerListener ->
                        fditManagerListener.elementChanged(observable)));
        listeners.forEach(FditManagerListener::rootTreeItemChanged);
    }

    public void addListener(final FditManagerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final FditManagerListener listener) {
        listeners.remove(listener);
    }

    public Character getSeparator() {
        return separator;
    }

    public String getVersion() {
        return VERSION;
    }
}