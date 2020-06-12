package fdit.storage.execution;

import fdit.metamodel.execution.Execution;
import fdit.metamodel.schema.Schema;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;

import static fdit.metamodel.execution.Execution.SCHEMA;
import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.storage.FditStorageUtils.getRelativePath;
import static fdit.storage.execution.ExecutionStorage.*;
import static fdit.tools.jdom.Jdom2Utils.*;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.lang.String.valueOf;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public final class ExecutionSaver {

    private ExecutionSaver() {
    }

    public static void saveExecution(final Execution execution,
                                     final File rootFile) throws Exception {
        final File executionFile = getFditElementFile(execution, rootFile);
        try (final OutputStream alterationOutputStream = new FileOutputStream(executionFile)) {
            writeDocument(new Document(createExecutionElement(execution, rootFile)), alterationOutputStream);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static Element createExecutionElement(final Execution execution,
                                                  final File rootFile) {
        return createElement(EXECUTION, children(
                createRecordingElement(execution, rootFile),
                createSchemasElement(execution.getSchemas(), execution, rootFile)));
    }

    private static Element createRecordingElement(final Execution execution,
                                                  final File rootFile) {
        final String path = execution.getRecording() == EMPTY_RECORDING ? "" :
                separatorsToUnix(valueOf(getRelativePath(execution, execution.getRecording(), rootFile)));
        return createElement(RECORDING, text((path)));
    }

    private static Element createSchemasElement(final Collection<Schema> schemas,
                                                final Execution execution,
                                                final File rootFile) {
        final Collection<Element> elementsSchemas = mapping(schemas, schema ->
                toSchemaElement(schema, execution, rootFile));
        return createElement(SCHEMAS, children(elementsSchemas));
    }

    private static Element toSchemaElement(final Schema schema,
                                           final Execution execution,
                                           final File rootFile) {
        final String scenarioPath = schema == SCHEMA ? "" :
                separatorsToUnix(valueOf(getRelativePath(execution, schema, rootFile)));
        return createElement(ExecutionStorage.SCHEMA, text(scenarioPath));
    }
}