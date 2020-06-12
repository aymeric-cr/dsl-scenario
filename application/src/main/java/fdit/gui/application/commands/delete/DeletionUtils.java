package fdit.gui.application.commands.delete;

import fdit.gui.executionEditor.SchemaDeletionCommand;
import fdit.history.Command;
import fdit.history.CommandExecutor;
import fdit.metamodel.FditElementVisitor;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.DirectoryUtils;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Files.createTempDir;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.metamodel.aircraft.AircraftUtils.randomUUID;
import static fdit.metamodel.element.DirectoryUtils.gatherAllExecutions;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.exists;
import static fdit.tools.stream.StreamUtils.filter;
import static java.util.Collections.singleton;
import static org.apache.commons.io.FileUtils.forceMkdir;

public final class DeletionUtils {

    public static final File GLOBAL_TMP_DIR_LOCATION = createTempDir();
    private static final MessageTranslator TRANSLATOR = createMessageTranslator(DeletionUtils.class);

    private DeletionUtils() {
    }

    public static void delete(final FditElement element) throws IOException {
        delete(singleton(element));
    }

    public static void delete(final Collection<FditElement> elements) throws IOException {
        final File commandTmpDir = createCommandTmpDir();
        final Collection<FditElement> normalizedSelection = keepOnlyAncestors(elements);
        final Collection<FditElement> allDeletedElements = gatherAllDeletedElements(elements);
        for (final FditElement element : normalizedSelection) {
            final File subTmpDir = createSubTmpDir(commandTmpDir);
            delete(element, subTmpDir, allDeletedElements);
        }
        FDIT_MANAGER.getCommandExecutor().execute(emptyCommand());
    }

    private static void delete(final FditElement element,
                               final File tmpDirLocation,
                               final Collection<FditElement> allDeletedElements) {
        new FditElementVisitor<Void>() {
            @Override
            public Void visitDirectory(final Directory directory) {
                FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(directory,
                        tmpDirLocation));
                return null;
            }

            @Override
            public Void visitSchema(final Schema schema) {
                deleteSchema(schema, tmpDirLocation, allDeletedElements);
                return null;
            }

            @Override
            public Void visitAlterationSpecification(final AlterationSpecification alterationSpecification) {
                //NEVER CALL -> Deletion is a consequence of graphical scenario deletion
                return null;
            }

            @Override
            public Void visitExecution(final Execution execution) {
                FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(
                        execution,
                        tmpDirLocation));
                return null;
            }

            @Override
            public Void visitRecording(final Recording recording) {
                FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(recording,
                        tmpDirLocation));
                return null;
            }

            @Override
            public Void visitZone(final Zone zone) {
                deleteZone(zone, tmpDirLocation);
                return null;
            }

            @Override
            public Void visitLTLFilter(final LTLFilter ltlFilter) {
                FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(ltlFilter,
                        tmpDirLocation));
                return null;
            }

            @Override
            public Void visitAlterationTrigger(ActionTrigger trigger) {
                FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(trigger,
                        tmpDirLocation));
                return null;
            }
        }.accept(element);
    }

    private static File createCommandTmpDir() throws IOException {
        final File tmpDir = new File(GLOBAL_TMP_DIR_LOCATION, randomUUID().toString());
        forceMkdir(tmpDir);
        return tmpDir;
    }

    private static File createSubTmpDir(final File commandTmpDir) throws IOException {
        final File tmpDir = new File(commandTmpDir, randomUUID().toString());
        forceMkdir(tmpDir);
        return tmpDir;
    }

    private static void deleteSchema(final Schema schema,
                                     final File tmpDirLocation,
                                     final Collection<FditElement> allDeletedElements) {
        final Collection<Execution> executions =
                filter(gatherAllExecutions(FDIT_MANAGER.getRoot()),
                        exec -> !allDeletedElements.contains(exec) && isUsed(schema, exec));
        for (final Execution execution : executions) {
            FDIT_MANAGER.getCommandExecutor().executePreCommand(new SchemaDeletionCommand(execution, schema));
        }
        FDIT_MANAGER.getCommandExecutor().executePreCommand(new FditElementDeletionCommand(schema, tmpDirLocation));
    }

    private static void deleteZone(final Zone zone, final File tmpDirLocation) {
        final CommandExecutor executor = FDIT_MANAGER.getCommandExecutor();
        executor.executePreCommand(new FditElementDeletionCommand(zone, tmpDirLocation));
    }

    private static boolean isUsed(final Schema schema,
                                  final Execution execution) {
        return execution.getSchemas().contains(schema);
    }

    private static Collection<FditElement> keepOnlyAncestors(final Collection<FditElement> deletedElements) {
        final Collection<Directory> deletedDirs = filter(deletedElements, Directory.class);
        return filter(deletedElements,
                deletedElement -> !exists(deletedDirs, dir -> DirectoryUtils.isAncestorOf(dir, deletedElement)));
    }

    private static Collection<FditElement> gatherAllDeletedElements(final Iterable<FditElement> elements) {
        final Collection<FditElement> deletedElements = newArrayList();
        for (final FditElement element : elements) {
            if (element instanceof Directory) {
                deletedElements.addAll(gatherAllDeletedElements(DirectoryUtils.gatherChildren((Directory) element)));
            }
            deletedElements.add(element);
        }
        return deletedElements;
    }

    private static Command emptyCommand() {
        return new Command() {
            @Override
            public String getContent() {
                return TRANSLATOR.getMessage("command.delete.descr");
            }

            @Override
            public void execute() {

            }

            @Override
            public void undo() {

            }

            @Override
            public void redo() {

            }
        };
    }
}