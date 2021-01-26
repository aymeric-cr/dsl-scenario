package fdit.gui.application;

import fdit.gui.application.commands.create.*;
import fdit.gui.application.concurrent.TestSyncExecutionFactory;
import fdit.history.CommandExecutor;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.Root;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.*;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.FditPolygon;
import fdit.metamodel.zone.Zone;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;
import fdit.storage.recording.TestRecordingInDatabaseLoadingCallback;
import fdit.storage.recording.baseStation.BstContentLoader;
import fdit.testTools.Saver;
import fdit.testTools.rules.FileSystemPlugin;
import fdit.tools.functional.ThrowableConsumer;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.io.Files.getNameWithoutExtension;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.FditElementExtensions.suffixWithExtension;
import static fdit.storage.FditElementStorage.save;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.testTools.FileTestUtils.createEmptyFile;
import static fdit.testTools.Saver.create;
import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static fdit.tools.io.FileUtils.listFiles;
import static fdit.tools.stream.StreamUtils.tryFind;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public abstract class FditTestCase {

    @Rule
    public final FileSystemPlugin fileSystem = new FileSystemPlugin();

    protected static CommandExecutor getExecutor() {
        return FDIT_MANAGER.getCommandExecutor();
    }

    protected static void undo() {
        FDIT_MANAGER.getCommandExecutor().undo();
    }

    protected static void redo() {
        FDIT_MANAGER.getCommandExecutor().redo();
    }

    protected static Root getRoot() {
        return FDIT_MANAGER.getRoot();
    }

    protected static File findFile(final File father, final String fileName) {
        final Optional<File> foundFile = tryFind(listFiles(father),
                file -> file.getName().equals(fileName));
        if (foundFile.isPresent()) {
            return foundFile.get();
        }
        for (final File folder : requireNonNull(father.listFiles(File::isDirectory))) {
            final File found = findFile(folder, fileName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @SafeVarargs
    public static Root root(final Consumer<Directory>... contents) {
        final Root root = getRoot();
        acceptAll(root, contents);
        return root;
    }

    @SafeVarargs
    protected static ThrowableConsumer<Directory> folder(final String name,
                                                         final Consumer<Directory>... children) {
        return father -> {
            final CreateDirectoryCommand command = new CreateDirectoryCommand(father, name);
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final Directory directory = command.getSubject();
            acceptAll(directory, children);
        };
    }

    protected static ThrowableConsumer<Directory> actionTrigger(final String name) {
        return father -> {
            final ActionTriggerCreationCommand command = new ActionTriggerCreationCommand(father, name, randomUUID());
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final ActionTrigger trigger = command.getSubject();
            save(trigger, FDIT_MANAGER.getRootFile());
        };
    }

    protected static ThrowableConsumer<Directory> actionTrigger(final String name,
                                                                final UUID id,
                                                                final Consumer<ActionTrigger>... consumers) {
        return father -> {
            final ActionTriggerCreationCommand command = new ActionTriggerCreationCommand(father, name, id);
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final ActionTrigger trigger = command.getSubject();
            acceptAll(trigger, consumers);
            save(trigger, FDIT_MANAGER.getRootFile());
        };
    }

    protected static ThrowableConsumer<Directory> schema(final String name) {
        return schema(name, EMPTY_RECORDING);
    }

    protected static ThrowableConsumer<Directory> schema(final String name,
                                                         final Consumer<Schema>... consumers) {
        return schema(name, EMPTY_RECORDING, consumers);
    }

    protected static ThrowableConsumer<Directory> schema(final String name,
                                                         final Recording recording,
                                                         final Consumer<Schema>... consumers) {
        return father -> {
            final SchemaCreationCommand command = new SchemaCreationCommand(father, name, "", recording);
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final Schema scenario = command.getSubject();
            acceptAll(scenario, consumers);
            save(scenario, FDIT_MANAGER.getRootFile());
        };
    }

    public static ThrowableConsumer<Directory> schema(final String name,
                                                      final Saver<Recording> recording,
                                                      final Consumer<Schema>... consumers) {
        return father -> schema(name, recording.get(), consumers).accept(father);
    }

    public static ThrowableConsumer<Directory> schema(final String name,
                                                      final Saver<Recording> recording,
                                                      final Saver<Schema> saver,
                                                      final Consumer<Schema>... consumers) {
        return father -> {
            final Schema scenario = createSchema(
                    name,
                    "",
                    recording.get(),
                    father,
                    consumers);
            saver.save(scenario);
        };
    }

    private static Schema createSchema(final String name,
                                           final String content,
                                           final Recording recording,
                                           final Directory father,
                                           final Consumer<Schema>... consumers) {
        final SchemaCreationCommand command = new SchemaCreationCommand(father, name, content, recording);
        FDIT_MANAGER.getCommandExecutor().execute(command);
        final Schema scenario = command.getSubject();
        acceptAll(scenario, consumers);
        save(scenario, FDIT_MANAGER.getRootFile());
        return scenario;
    }

    protected static ThrowableConsumer<Directory> bstRecording(final Saver<Recording> saver) {
        return bstRecording("withRecording", saver);
    }

    protected static ThrowableConsumer<Directory> bstRecording(final Saver<Recording> saver,
                                                               final File bstFile,
                                                               final Aircraft... aircrafts) {
        return recording(father -> {
            try {
                FileUtils.copyFile(bstFile, createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        bstFile.getName()));
                return new BaseStationRecording(getNameWithoutExtension(bstFile.getName()), recordingContentLoader(0, aircrafts));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    public static ThrowableConsumer<Directory> bstRecording(final Saver<Recording> saver, final File bstFile) {
        return recording(father -> {
            try {
                FileUtils.copyFile(bstFile, createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        suffixWithExtension(bstFile.getName(), BaseStationRecording.class)));
                return new BaseStationRecording(bstFile.getName(), new BstContentLoader(bstFile, recordingLoadingCallback()));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    public static ThrowableConsumer<Directory> bstRecording(final String name) {
        return bstRecording(name, create(), 0);
    }

    public static ThrowableConsumer<Directory> bstRecording(final String name, final Saver<Recording> saver) {
        return bstRecording(name, saver, 0);
    }

    public static ThrowableConsumer<Directory> bstRecording(final String name,
                                                            final Saver<Recording> saver,
                                                            final long maxRelativeDate,
                                                            final Aircraft... aircrafts) {
        return recording(father -> {
            try {
                createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        suffixWithExtension(name, BaseStationRecording.class));
                return new BaseStationRecording(name, recordingContentLoader(maxRelativeDate, aircrafts));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    public static ThrowableConsumer<Directory> bstRecording(final String name,
                                                            final Saver<Recording> saver,
                                                            final long maxRelativeDate,
                                                            final long firstDate,
                                                            final Aircraft... aircrafts) {
        return recording(father -> {
            try {
                createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        suffixWithExtension(name, BaseStationRecording.class));
                return new BaseStationRecording(name, recordingContentLoader(maxRelativeDate, firstDate, aircrafts));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    protected static ThrowableConsumer<Directory> sbsRecording(final Saver<Recording> saver) {
        return sbsRecording("withRecording", saver);
    }

    protected static ThrowableConsumer<Directory> sbsRecording(final Saver<Recording> saver,
                                                               final File sbsFile,
                                                               final Aircraft... aircrafts) {
        return recording(father -> {
            try {
                FileUtils.copyFile(sbsFile, createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        sbsFile.getName()));
                return new SiteBaseStationRecording(getNameWithoutExtension(sbsFile.getName()), recordingContentLoader(0, aircrafts));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    public static ThrowableConsumer<Directory> sbsRecording(final String name) {
        return sbsRecording(name, create(), 0);
    }

    public static ThrowableConsumer<Directory> sbsRecording(final String name, final Saver<Recording> saver) {
        return sbsRecording(name, saver, 0);
    }

    public static ThrowableConsumer<Directory> sbsRecording(final String name,
                                                            final Saver<Recording> saver,
                                                            final long maxRelativeDate,
                                                            final Aircraft... aircrafts) {
        return recording(father -> {
            try {
                createEmptyFile(getFditElementFile(father, FDIT_MANAGER.getRootFile()),
                        suffixWithExtension(name, SiteBaseStationRecording.class));
                return new SiteBaseStationRecording(name, recordingContentLoader(maxRelativeDate, aircrafts));
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }, saver);
    }

    public static ThrowableConsumer<Directory> recording(final Function<Directory, Recording> recordingBuilder,
                                                         final Saver<Recording> saver) {
        return father -> {
            final Recording recording = recordingBuilder.apply(father);
            FDIT_MANAGER.addFditElement(recording, father);
            recording.load();
            saver.save(recording);
        };
    }

    protected static ThrowableConsumer<Directory> ltlFilter(final String name) {
        return father -> {
            final LTLFilterCreationCommand command = new LTLFilterCreationCommand(father, name, randomUUID());
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final LTLFilter ltlFilter = command.getSubject();
            save(ltlFilter, FDIT_MANAGER.getRootFile());
        };
    }

    protected static ThrowableConsumer<Directory> ltlFilter(final String name,
                                                            final Saver<LTLFilter> saver,
                                                            final UUID id,
                                                            final String description,
                                                            final String content) {
        return father -> {
            final LTLFilterCreationCommand command = new LTLFilterCreationCommand(father, name, id);
            FDIT_MANAGER.getCommandExecutor().execute(command);
            final LTLFilter ltlFilter = command.getSubject();
            ltlFilter.setDescription(description);
            ltlFilter.setContent(content);
            saver.save(ltlFilter);
            save(ltlFilter, FDIT_MANAGER.getRootFile());
        };
    }

    private static RecordingContentLoader recordingContentLoader(final long maxRelativeDate,
                                                                 final Aircraft... aircrafts) {
        return () -> new RecordingContentLoaderResult(1, asList(aircrafts), maxRelativeDate);
    }

    private static RecordingContentLoader recordingContentLoader(final long maxRelativeDate,
                                                                 final long firstDate,
                                                                 final Aircraft... aircrafts) {
        return () -> new RecordingContentLoaderResult(1, asList(aircrafts), maxRelativeDate, firstDate);
    }

    public static RecordingInDatabaseLoadingCallback recordingLoadingCallback() {
        return new TestRecordingInDatabaseLoadingCallback();
    }

    @Before
    public void setUp() throws Exception {
        FDIT_MANAGER.initialize();
        FDIT_MANAGER.setAsyncExecutionFactory(new TestSyncExecutionFactory());
        FDIT_MANAGER.loadRoot(getRootFile(), recordingLoadingCallback());
    }

    protected File getRootFile() {
        return fileSystem.getRoot();
    }

    protected File findFile(final String fileName) {
        return findFile(fileSystem.getRoot(), fileName);
    }
}