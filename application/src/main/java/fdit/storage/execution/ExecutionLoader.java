package fdit.storage.execution;

import fdit.metamodel.execution.Execution;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManagerUtils.getFditElementFile;
import static fdit.metamodel.execution.Execution.SCHEMA;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.storage.execution.ExecutionStorage.RECORDING;
import static fdit.storage.execution.ExecutionStorage.SCHEMAS;
import static fdit.tools.io.FileUtils.areSameFiles;
import static fdit.tools.jdom.Jdom2Utils.createFromInputStream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public final class ExecutionLoader {

    private ExecutionLoader() {
    }

    public static Execution loadExecution(final File executionFile,
                                          final Iterable<Schema> scenarios,
                                          final Iterable<Recording> recordings) throws Exception {
        try (final InputStream executionInputStream = new FileInputStream(executionFile)) {
            final Document executionDocument = createFromInputStream(executionInputStream);
            final Element executionElement = executionDocument.getRootElement();
            return getExecution(
                    executionFile,
                    executionElement,
                    scenarios,
                    recordings);
        }
    }

    @SuppressWarnings("OptionalIsPresent")
    private static Execution getExecution(final File executionFile,
                                          final Element executionElement,
                                          final Iterable<Schema> schemas,
                                          final Iterable<Recording> recordings) {
        final List<Schema> retrievedSchemas = getSchemas(
                executionElement.getChild(SCHEMAS).getChildren(), executionFile, schemas);
        final Optional<Recording> optionalRecording =
                retrieveRecording(executionElement.getChild(RECORDING).getText(), executionFile, recordings);
        final Recording recording = optionalRecording.isPresent() ? optionalRecording.get() : EMPTY_RECORDING;
        final Execution execution = new Execution(buildFditElementName(executionFile));
        for (final Schema retrievedSchema : retrievedSchemas) {
            execution.addSchema(retrievedSchema);
        }
        execution.setRecording(recording);
        return execution;
    }

    private static Optional<Recording> retrieveRecording(final String recordingRelativePath,
                                                         final File executionFile,
                                                         final Iterable<Recording> recordings) {
        final File recordingFile = new File(executionFile.getParentFile(), recordingRelativePath);
        for (final Recording recording : recordings) {
            if (areSameFiles(getFditElementFile(recording), recordingFile)) {
                return of(recording);
            }
        }
        return empty();
    }

    private static List<Schema> getSchemas(final List<Element> children,
                                           final File executionFile,
                                           final Iterable<Schema> schemas) {
        final List<Schema> result = newArrayList();
        children.forEach(element -> {
            final Schema schema = getSchema(element, executionFile, schemas);
            result.add(schema);
        });
        return result;
    }

    private static Schema getSchema(final Element schemaElement,
                                    final File executionFile,
                                    final Iterable<Schema> scenarios) {
        final Optional<Schema> optionalSchema = retrieveSchema(
                separatorsToUnix(schemaElement.getText()),
                executionFile,
                scenarios);
        return optionalSchema.orElse(SCHEMA);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static Optional<Schema> retrieveSchema(String path,
                                                   final File executionFile,
                                                   final Iterable<Schema> scenarios) {
        if (path != null) {
            final File schemaFile = new File(executionFile.getParentFile(), path);
            for (final Schema schema : scenarios) {
                if (areSameFiles(getFditElementFile(schema), schemaFile)) {
                    return of(schema);
                }
            }
        }
        return empty();
    }
}