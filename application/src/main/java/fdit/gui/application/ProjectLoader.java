package fdit.gui.application;

import fdit.metamodel.FditElementTypeVisitor;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.storage.recording.RecordingInDatabaseLoadingCallback;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.storage.FditElementExtensions.getElementTypeFrom;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.storage.execution.ExecutionLoader.loadExecution;
import static fdit.storage.filter.LTLFilterStorage.loadLTLFilter;
import static fdit.storage.recording.RecordingStorage.loadRecording;
import static fdit.storage.scenario.SchemaStorage.loadTextualScenario;
import static fdit.storage.trigger.ActionTriggerStorage.loadAlterationTrigger;
import static fdit.storage.zone.ZoneLoader.loadZone;
import static fdit.tools.io.FileUtils.listFiles;
import static fdit.tools.stream.StreamUtils.filter;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singleton;

public final class ProjectLoader {

    private ProjectLoader() {
    }

    public static Root loadRoot(final File rootFile) {
        final Root root = new Root(buildFditElementName(rootFile));
        createTree(root, rootFile);
        return root;
    }

    static void loadProject(final Root root,
                            final File rootFile,
                            final RecordingInDatabaseLoadingCallback callback) {
        loadAllFditElements(root, rootFile, root, rootFile, clazz ->
                BaseStationRecording.class.equals(clazz)
                        || SiteBaseStationRecording.class.equals(clazz)
                        || Zone.class.equals(clazz)
                        || ActionTrigger.class.equals(clazz)
                        || LTLFilter.class.equals(clazz), callback);
        loadAllFditElements(root, rootFile, root, rootFile, Schema.class::equals, callback);
        loadAllFditElements(root, rootFile, root, rootFile, Execution.class::equals, callback);
    }

    private static void createTree(final Directory father, final File fatherFile) {
        for (final File childDir : filter(listFiles(fatherFile), file ->
                file.isDirectory() && !file.getName().startsWith("."))) {
            createDirectory(father, childDir);
        }
    }

    private static void createDirectory(final Directory father,
                                        final File dirFile) {
        final Directory directory = new Directory(buildFditElementName(dirFile));
        createTree(directory, dirFile);
        father.addChild(directory);
        directory.setFather(father);
    }

    private static void loadAllFditElements(final Directory directory,
                                            final File directoryFile,
                                            final Root root,
                                            final File rootFile,
                                            final Predicate<Class<? extends FditElement>> loadType,
                                            final RecordingInDatabaseLoadingCallback callback) {
        for (final File childFile : listFiles(directoryFile)) {
            loadFditElement(directory, childFile, root, rootFile, loadType, callback);
        }
    }

    private static void loadFditElement(final Directory father,
                                        final File elementFile,
                                        final Root root,
                                        final File rootFile,
                                        final Predicate<Class<? extends FditElement>> loadTypePredicate,
                                        final RecordingInDatabaseLoadingCallback callback) {
        final Optional<Class<? extends FditElement>> optType = getElementTypeFrom(elementFile);
        if (!optType.isPresent()) {
            return;
        }
        final Class<? extends FditElement> type = optType.get();
        if (type.equals(Directory.class)) {
            findDirectory(buildFditElementName(elementFile), father).ifPresent(directory ->
                    loadAllFditElements(directory, elementFile, root, rootFile, loadTypePredicate, callback));
            return;
        }
        if (!loadTypePredicate.test(type)) {
            return;
        }
        loadFditElement(elementFile, root, rootFile, type, callback).forEach(loadedElement -> {
            father.addChild(loadedElement);
            loadedElement.setFather(father);
        });
    }

    public static Iterable<FditElement> importFditElement(final Directory father,
                                                          final File elementFile,
                                                          final Root root,
                                                          final File rootFile,
                                                          final Predicate<Class<? extends FditElement>> loadTypePredicate,
                                                          final RecordingInDatabaseLoadingCallback callback) {
        final Optional<Class<? extends FditElement>> optType = getElementTypeFrom(elementFile);
        if (!optType.isPresent()) {
            return EMPTY_LIST;
        }
        final Class<? extends FditElement> type = optType.get();
        if (type.equals(Directory.class)) {
            findDirectory(buildFditElementName(elementFile), father).ifPresent(directory -> {
                loadAllFditElements(directory, elementFile, root, rootFile, loadTypePredicate, callback);
            });
            return EMPTY_LIST;
        }
        if (!loadTypePredicate.test(type)) {
            return EMPTY_LIST;
        }
        final Iterable<FditElement> loadedElements = loadFditElement(elementFile, root, rootFile, type, callback);
        loadedElements.forEach(loadedElement -> {
            father.addChild(loadedElement);
            loadedElement.setFather(father);
        });
        return loadedElements;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static Iterable<FditElement> loadFditElement(final File elementFile,
                                                         final Root root,
                                                         final File rootFile,
                                                         final Class<? extends FditElement> type,
                                                         final RecordingInDatabaseLoadingCallback callback) {
        return new FditElementTypeVisitor<Collection<FditElement>>() {
            @Override
            public Collection<FditElement> visitAlteration() {
                return EMPTY_LIST; // loaded with graphicalScenario
            }

            @Override
            public Collection<FditElement> visitRecording() {
                return singleton(loadRecording(elementFile, callback));
            }

            @Override
            public Collection<FditElement> visitScenario() {
                try {
                    return singleton(loadTextualScenario(elementFile,
                            rootFile,
                            gatherAllRecordings(root)));
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Collection<FditElement> visitZone() {
                try {
                    return singleton(loadZone(elementFile, gatherAllZones(root)));
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Collection<FditElement> visitDirectory() {
                return EMPTY_LIST;
            }

            @Override
            public Collection<FditElement> visitExecution() {
                try {
                    return singleton(loadExecution(
                            elementFile,
                            gatherAllScenario(root),
                            gatherAllRecordings(root)));
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Collection<FditElement> visitLTLFilter() {
                try {
                    return singleton(loadLTLFilter(elementFile));
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Collection<FditElement> visitAlterationTrigger() {
                try {
                    return singleton(loadAlterationTrigger(elementFile));
                } catch (final Exception e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }
        }.accept(type);
    }
}