package fdit.storage.schema;

import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;

import java.io.*;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Properties;

import static fdit.metamodel.recording.Recording.EMPTY_RECORDING;
import static fdit.storage.FditStorageUtils.*;
import static fdit.tools.stream.StreamUtils.find;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang.CharEncoding.UTF_8;

public final class SchemaStorage {

    private static final String DESCRIPTION = "description";
    private static final String BST_PATH = "bstPath";
    private static final String CONTENT = "content";

    private SchemaStorage() {
    }

    public static Schema loadSchema(final File scenarioFile,
                                    final File rootFile,
                                    final Iterable<Recording> loadedRecordings) throws Exception {
        final Properties scenarioProperties = loadScenarioProperties(scenarioFile);
        final String scenarioName = buildFditElementName(scenarioFile);
        final String description = scenarioProperties.getProperty(DESCRIPTION);
        final String relativeBstPath = scenarioProperties.getProperty(BST_PATH);
        final String content = scenarioProperties.getProperty(CONTENT);
        final Schema schema;
        if (relativeBstPath != null) {
            final File bstFile = new File(scenarioFile.getParentFile(), separatorsToUnix(relativeBstPath));
            Recording associatedRecording;
            try {
                associatedRecording = find(loadedRecordings, recording -> {
                    try {
                        return Files.isSameFile(getFditElementFile(recording, rootFile).toPath(), bstFile.toPath());
                    } catch (final IOException ignored) {
                        return false;
                    }
                });
            } catch (final NoSuchElementException e) {
                associatedRecording = EMPTY_RECORDING;
            }
            schema = new Schema(
                    scenarioName,
                    description,
                    content,
                    associatedRecording);
        } else {
            schema = new Schema(scenarioName, description, content);
        }
        return schema;
    }

    public static void saveTextualScenario(final Schema schema, final File rootFile) throws Exception {
        final Properties scenarioProperties = new Properties();
        scenarioProperties.setProperty(DESCRIPTION, schema.getDescription());
        final Recording recording = schema.getRecording();
        if (recording != EMPTY_RECORDING) {
            scenarioProperties.setProperty(BST_PATH,
                    separatorsToUnix(getRelativePath(schema, recording, rootFile).toString()));
        }
        scenarioProperties.setProperty(CONTENT, schema.getContent());
        try (final OutputStream scenarioOutputStream = new FileOutputStream(getFditElementFile(schema, rootFile))) {
            scenarioProperties.storeToXML(scenarioOutputStream, null, UTF_8);
        }
    }

    private static Properties loadScenarioProperties(final File scenarioFile) throws Exception {
        try (final InputStream scenarioInputStream = new FileInputStream(scenarioFile)) {
            final Properties scenarioProperties = new Properties();
            scenarioProperties.loadFromXML(scenarioInputStream);
            return scenarioProperties;
        }
    }
}