package fdit.storage.nameChecker;

import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.metamodel.zone.Zone;
import fdit.tools.i18n.MessageTranslator;
import fdit.tools.io.FileExtensionFilter;
import fdit.tools.nativeutil.OperatingSystemUtils;

import java.io.File;
import java.util.Collection;

import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Files.getNameWithoutExtension;
import static fdit.storage.FditElementExtensions.getFileExtensionsOf;
import static fdit.storage.FditStorageUtils.getFditElementFile;
import static fdit.storage.nameChecker.CheckResult.fail;
import static fdit.storage.nameChecker.CheckResult.success;
import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.io.FileUtils.listFiles;
import static fdit.tools.stream.StreamUtils.filter;
import static fdit.tools.stream.StreamUtils.mapping;
import static fdit.tools.string.StringPredicates.equalsIgnoringCase;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.lang.StringUtils.contains;
import static org.apache.commons.lang.StringUtils.isBlank;

public final class FditElementNameChecker {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(FditElementNameChecker.class);
    private static final int NAME_MAX_LENGTH = 100;
    private static final char[] INVALID_CHARACTERS = {
            '*',
            '>',
            ':',
            '"',
            '/',
            '\\',
            '|',
            '?',
            '&',
    };

    private FditElementNameChecker() {
    }

    public static CheckResult checkRenameElementValidity(final FditElement element,
                                                         final File rootFile,
                                                         final String newName) {
        final String normalizedName = normalize(newName);
        if (normalizedName.equals(element.getName())) {
            return success();
        }
        try {
            final Collection<String> prohibitedNames;
            if (element instanceof Directory) {
                prohibitedNames = gatherAllDirectoryNamesInParentFolder((Directory) element, rootFile);
            } else {
                prohibitedNames = gatherFileNamesOfType(element.getFather(), rootFile, element.getClass());
            }
            return checkNameValidity(normalizedName, prohibitedNames);
        } catch (final Exception e) {
            return fail(TRANSLATOR.getMessage("error.unexpectedError"));
        }
    }

    public static CheckResult checkNewFditElementNameValidity(final String name,
                                                              final Directory father,
                                                              final File rootFile,
                                                              final Class<? extends FditElement> type) {
        try {
            return checkNameValidity(name, gatherFileNamesOfType(father, rootFile, type));
        } catch (final Exception e) {
            return fail(TRANSLATOR.getMessage("error.unexpectedError"));
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static Collection<String> gatherAllDirectoryNamesInParentFolder(final Directory directory,
                                                                            final File rootFile) {
        final File directoryFile = getFditElementFile(directory, rootFile);
        final File parentFile = new File(directoryFile.getParent());
        final Collection<String> prohibitedNames = newArrayList();
        for (final File child : listFiles(parentFile)) {
            if (child.isDirectory()) {
                prohibitedNames.add(child.getName());
            }
        }
        return prohibitedNames;
    }

    private static CheckResult checkNameValidity(final String name, final Collection<String> prohibitedNames) {
        final String normalized = normalize(name);
        if (isBlank(normalized)) {
            return fail(TRANSLATOR.getMessage("error_name_blank"));
        }
        if (normalized.length() > NAME_MAX_LENGTH) {
            return fail(TRANSLATOR.getMessage("error_name_too_long_2", normalized.length(), NAME_MAX_LENGTH));
        }
        if (contains(prohibitedNames, normalized)) {
            return fail(TRANSLATOR.getMessage("error_name_already_exist_1", normalized));
        }
        final boolean caseSensitive = OperatingSystemUtils.isFileSystemCaseSensitive();
        if (caseSensitive && prohibitedNames.stream().anyMatch(normalized::equals)) {
            return fail(TRANSLATOR.getMessage("error_name_already_exist_case_sensitive_1", normalized));
        }
        if (!caseSensitive && prohibitedNames.stream().anyMatch(equalsIgnoringCase(normalized))) {
            return fail(TRANSLATOR.getMessage("error_name_already_exist_case_insensitive_1", normalized));
        }
        if (!OperatingSystemUtils.isNameValid(normalized) || containsInvalidChar(normalized)) {
            return fail(TRANSLATOR.getMessage("error_name_bad_format"));
        }
        return success();
    }

    private static boolean containsInvalidChar(final String name) {
        for (final char invalidCharacter : INVALID_CHARACTERS) {
            if (contains(name, invalidCharacter)) {
                return true;
            }
        }
        return false;
    }

    private static Collection<String> gatherFileNamesOfType(final Directory father,
                                                            final File rootFile,
                                                            final Class<? extends FditElement> type) {
        final Collection<String> fileNames = newArrayList();
        if (Zone.class.isAssignableFrom(type) ||
                type.equals(Schema.class) ||
                type.equals(LTLFilter.class) ||
                type.equals(ActionTrigger.class)) {
            final Class<? extends FditElement> superType;
            if (Zone.class.isAssignableFrom(type)) {
                superType = Zone.class;
            } else {
                superType = type;
            }
            Directory root = (father instanceof Root) ? father : father.getFather();
            while (!(root instanceof Root)) {
                root = root.getFather();
            }
            fileNames.addAll(gatherFileNamesOfTypeFromDirectory(root, superType));
        }
        final Collection<File> files = asList(getFditElementFile(father, rootFile).listFiles(new FileExtensionFilter(
                getFileExtensionsOf(type))));
        fileNames.addAll(mapping(files, file -> getNameWithoutExtension(file.getName())));
        return fileNames;
    }

    private static Collection<String> gatherFileNamesOfTypeFromDirectory(final Directory directory,
                                                                         final Class<? extends FditElement> type) {
        final Collection<String> fileNames = newArrayList();
        for (final FditElement child : directory.getChildren()) {
            if (child instanceof Directory) {
                fileNames.addAll(gatherFileNamesOfTypeFromDirectory((Directory) child, type));
            }
        }
        for (final FditElement toAdd : filter(directory.getChildren(), type::isInstance)) {
            fileNames.add(toAdd.getName());
        }
        return fileNames;
    }
}