package fdit.tools.io;

import com.google.common.collect.Iterables;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.io.FileUtils.deleteDirectory;

public final class FileUtils {

    public static final char[] INVALID_CHARACTERS = {'.', '%', /*'&', '#'*/};
    public static final File USER_HOME_DIRECTORY = new File(System.getProperty("user.home"));
    public static final File TMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    public static final File USER_DESKTOP_DIRECTORY = new File(USER_HOME_DIRECTORY, "Desktop");
    public static final int FULL_PATH_MAX_LENGTH = 250;

    private FileUtils() {
    }


    public static String toAbsoluteString(final Path path) {
        return path.toAbsolutePath().toString();
    }

    public static boolean areSameFiles(final File file1, final File file2) {
        return file1.toPath().normalize().equals(file2.toPath().normalize());
    }

    public static Collection<Path> listPaths(final Path directory) {
        try (final Stream<Path> stream = Files.list(directory)) {
            return stream.collect(Collectors.toList());
        } catch (final IOException e) {
            return EMPTY_LIST;
        }
    }

    public static boolean hasFile(final Path directory, final String name) {
        if (Files.notExists(directory) || !isDirectory(directory)) {
            return false;
        }
        final Path file = directory.resolve(name);
        return Files.exists(file) && Files.isRegularFile(file);
    }

    public static Iterable<Path> getFileSystemReadableRoots() {
        return Iterables.filter(getDefault().getRootDirectories(), Files::isReadable);
    }

    public static boolean hasExtension(final Path path, final String extension) {
        return path.getFileSystem().getPathMatcher("glob:*." + extension).matches(path.getFileName());
    }

    public static void deletePath(final Path path) {
        if (isDirectory(path)) {
            try (final Stream<Path> stream = Files.list(path)) {
                stream.forEach(FileUtils::deletePath);
                Files.deleteIfExists(path);
            } catch (final IOException ignored) {
            }
        } else {
            try {
                Files.deleteIfExists(path);
            } catch (final IOException ignored) {
            }
        }
    }

    public static Path moveDirectoryToDirectory(final Path directory, final Path targetRoot) throws IOException {
        Files.createDirectories(targetRoot);
        final Path newPath = targetRoot.resolve(directory.getFileName());
        Files.move(directory, newPath);
        return newPath;
    }

    public static void openFiles(final Iterable<Path> paths) {
        paths.forEach(path -> {
            try {
                Desktop.getDesktop().open(path.toFile());
            } catch (final Exception e) {
                // do nothing
            }
        });
    }

    public static void deleteSubDirStartWith(final File root, final String regex) throws IOException {
        for (final File child : listFiles(root)) {
            if (child.isDirectory()) {
                if (child.getName().startsWith(regex)) {
                    deleteDirectory(child);
                } else {
                    deleteSubDirStartWith(child, regex);
                }
            }
        }
    }

    public static Collection<File> listFiles(final File directory) {
        final Collection<File> files = newArrayList();
        final File[] arrayFiles = directory.listFiles();
        if (arrayFiles != null) {
            files.addAll(asList(arrayFiles));
        }
        return files;
    }

    public static Collection<File> listFiles(final File directory, final FileFilter filter) {
        final Collection<File> files = newArrayList();
        final File[] arrayFiles = directory.listFiles(filter);
        if (arrayFiles != null) {
            files.addAll(asList(arrayFiles));
        }
        return files;
    }
}