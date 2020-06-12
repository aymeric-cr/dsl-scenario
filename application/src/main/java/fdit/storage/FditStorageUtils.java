package fdit.storage;

import fdit.metamodel.element.Directory;
import fdit.metamodel.element.FditElement;
import fdit.metamodel.element.Root;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fdit.storage.FditElementExtensions.getFileExtensionsOf;
import static java.io.File.separatorChar;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public final class FditStorageUtils {
    private FditStorageUtils() {
    }

    public static String buildFditElementName(final File file) {
        if (file.isFile()) {
            return removeExtension(file.getName());
        } else {
            return file.getName();
        }
    }

    public static File getFditElementFile(final FditElement element, final File rootFile) {
        if (element instanceof Root) {
            return rootFile;
        }
        Directory father = element.getFather();
        final StringBuilder filePathBuilder = new StringBuilder(element.getName());
        if (!(element instanceof Directory)) {
            filePathBuilder.append('.').append(getFileExtensionsOf(element.getClass()));
        }
        while (!(father instanceof Root)) {
            filePathBuilder.insert(0, father.getName() + separatorChar);
            father = father.getFather();
        }
        return new File(rootFile, filePathBuilder.toString());
    }

    public static Path getRelativePath(final FditElement from,
                                       final FditElement to,
                                       final File rootFile) {
        final Path fromPath = getFditElementFile(from, rootFile).getParentFile().toPath();
        final Path toPath = getFditElementFile(to, rootFile).toPath();
        return fromPath.relativize(toPath);
    }

    public static Path getRelativePath(final FditElement element,
                                       final File rootFile) {
        final Path toPath = getFditElementFile(element, rootFile).toPath();
        final Path fromPath = Paths.get(System.getProperty("user.dir"));
        return fromPath.relativize(toPath);
    }
}