package fdit.tools.io;

import java.io.File;

import static java.lang.System.getProperty;
import static org.apache.commons.io.FilenameUtils.normalize;

public final class FileSystemUtils {
    private FileSystemUtils() {
    }

    public static File getUserDirectory() {
        return createFileReference(getProperty("user.home"));
    }

    public static File createFileReference(final String path, final String... elements) {
        return createFileReference(new File(path), elements);
    }

    public static File createFileReference(final File path, final String... elements) {
        File rawPath = path;
        for (final String element : elements) {
            rawPath = new File(rawPath, element);
        }
        final String normalizedPath = normalize(rawPath.getAbsolutePath());
        if (normalizedPath == null) {
            return rawPath.getAbsoluteFile();
        }
        return new File(normalizedPath);
    }

    private enum UnsupportedCharInFileName {
        DOT('.'),
        BACKSLASH('\\'),
        SLASH('/'),
        COLON(':'),
        STAR('*'),
        QUESTION('?'),
        QUOTE('"'),
        LESS('<'),
        GREAT('>'),
        PIPE('|');

        private final char unsupported;

        UnsupportedCharInFileName(final char unsupported) {
            this.unsupported = unsupported;
        }

        public char getUnsupported() {
            return unsupported;
        }
    }
}
