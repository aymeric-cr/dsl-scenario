package fdit.storage;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.execution.Execution;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.metamodel.recording.SiteBaseStationRecording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static fdit.storage.XsdValidator.*;
import static fdit.tools.stream.StreamUtils.tryFind;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.FilenameUtils.getExtension;

public final class FditElementExtensions {

    private static final Map<Class<? extends FditElement>, String> EXTENSIONS = newHashMap();

    static {
        EXTENSIONS.put(Directory.class, "");
        EXTENSIONS.put(AlterationSpecification.class, "xml");
        EXTENSIONS.put(Schema.class, "scenario");
        EXTENSIONS.put(BaseStationRecording.class, "bst");
        EXTENSIONS.put(SiteBaseStationRecording.class, "sbs");
        EXTENSIONS.put(Zone.class, "xml");
        EXTENSIONS.put(Execution.class, "xml");
        EXTENSIONS.put(LTLFilter.class, "ltl");
        EXTENSIONS.put(ActionTrigger.class, "trg");
    }

    private FditElementExtensions() {

    }

    public static String getFileExtensionsOf(final Class<? extends FditElement> clazz) {
        final String extension = EXTENSIONS.get(clazz);
        if (extension == null) {
            return getFileExtensionsOf((Class<? extends FditElement>) clazz.getSuperclass());
        }
        return extension;
    }

    public static String suffixWithExtension(final String fileName, final Class<? extends FditElement> clazz) {
        return fileName + '.' + getFileExtensionsOf(clazz);
    }

    public static Optional<Class<? extends FditElement>> getElementTypeFrom(final File file) {
        if ("xml".equals(getExtension(file.getName()))) {
            if (isValidAlterationFile(file)) {
                return of(AlterationSpecification.class);
            }
            if (isValidZoneFile(file)) {
                return of(Zone.class);
            }
            if (isValidExecutionFile(file)) {
                return of(Execution.class);
            }
            return empty();
        }
        return getElementTypeFromFileExtension(file);
    }

    private static Optional<Class<? extends FditElement>> getElementTypeFromFileExtension(final File file) {
        return tryFind(EXTENSIONS.entrySet(), entry -> entry.getValue().equals(getExtension(file.getName())))
                .map(Entry::getKey);
    }
}