package fdit.storage.filter;

import fdit.metamodel.filter.LTLFilter;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static java.util.UUID.fromString;
import static org.apache.commons.lang.CharEncoding.UTF_8;

public final class LTLFilterStorage {

    private static final String DESCRIPTION = "description";
    private static final String CONTENT = "content";
    private static final String ID = "id";

    private LTLFilterStorage() {
    }

    public static LTLFilter loadLTLFilter(final File filterFile) throws Exception {
        final Properties filterProperties = loadFilterProperties(filterFile);
        final String name = buildFditElementName(filterFile);
        final String description = filterProperties.getProperty(DESCRIPTION);
        final String content = filterProperties.getProperty(CONTENT);
        final UUID id = fromString(filterProperties.getProperty(ID));
        return new LTLFilter(name, id, description, content);
    }

    public static void saveFilter(final LTLFilter filter, final File rootFile) throws Exception {
        final Properties filterProperties = new Properties();
        filterProperties.setProperty(DESCRIPTION, filter.getDescription());
        filterProperties.setProperty(CONTENT, filter.getContent());
        filterProperties.setProperty(ID, filter.getId().toString());
        try (final OutputStream scenarioOutputStream = new FileOutputStream(getFditElementFile(filter, rootFile))) {
            filterProperties.storeToXML(scenarioOutputStream, null, UTF_8);
        }
    }

    private static Properties loadFilterProperties(final File filterFile) throws Exception {
        try (final InputStream FilterInputStream = new FileInputStream(filterFile)) {
            final Properties filterProperties = new Properties();
            filterProperties.loadFromXML(FilterInputStream);
            return filterProperties;
        }
    }
}