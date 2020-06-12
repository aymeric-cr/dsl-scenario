package fdit.storage.trigger;

import fdit.metamodel.trigger.ActionTrigger;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static java.util.UUID.fromString;
import static org.apache.commons.lang.CharEncoding.UTF_8;

public class ActionTriggerStorage {
    private static final String DESCRIPTION = "description";
    private static final String CONTENT = "content";
    private static final String ID = "id";

    private ActionTriggerStorage() {
    }

    public static ActionTrigger loadAlterationTrigger(final File triggerFile) throws Exception {
        final Properties filterProperties = loadTriggerProperties(triggerFile);
        final String name = buildFditElementName(triggerFile);
        final String description = filterProperties.getProperty(DESCRIPTION);
        final String content = filterProperties.getProperty(CONTENT);
        final UUID id = fromString(filterProperties.getProperty(ID));
        return new ActionTrigger(name, id, description, content);
    }

    public static void saveAlterationTrigger(final ActionTrigger trigger, final File rootFile) throws Exception {
        final Properties filterProperties = new Properties();
        filterProperties.setProperty(DESCRIPTION, trigger.getDescription());
        filterProperties.setProperty(CONTENT, trigger.getContent());
        filterProperties.setProperty(ID, trigger.getId().toString());
        try (final OutputStream scenarioOutputStream = new FileOutputStream(getFditElementFile(trigger, rootFile))) {
            filterProperties.storeToXML(scenarioOutputStream, null, UTF_8);
        }
    }

    private static Properties loadTriggerProperties(final File filterFile) throws Exception {
        try (final InputStream scenarioInputStream = new FileInputStream(filterFile)) {
            final Properties triggerProperties = new Properties();
            triggerProperties.loadFromXML(scenarioInputStream);
            return triggerProperties;
        }
    }
}
