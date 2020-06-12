package fdit.tools.io;

import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DirectoryZipper {

    private static final Character SEPARATOR;

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            SEPARATOR = '\\';
        } else {
            SEPARATOR = '/';
        }
    }

    private DirectoryZipper() {

    }

    public static boolean zip(final File directory,
                              final String destinationPath) throws IOException {

        final FileOutputStream outputStream = new FileOutputStream(destinationPath);
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            addFolderToZip("", directory, zipOutputStream);
            zipOutputStream.flush();
            zipOutputStream.close();
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void addFileToZip(final String path,
                                     final File file,
                                     final ZipOutputStream zipOutputStream) throws Exception {
        if (file.isDirectory()) {
            addFolderToZip(path, file, zipOutputStream);
        } else {
            final FileInputStream in = new FileInputStream(file);
            zipOutputStream.putNextEntry(new ZipEntry(path + SEPARATOR + file.getName()));
            int len;
            final byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, len);
            }
            in.close();
        }
    }

    private static void addFolderToZip(final String path,
                                       final File directory,
                                       final ZipOutputStream zip) throws Exception {
        for (final String fileName : directory.list()) {
            if (path.isEmpty()) {
                addFileToZip(directory.getName(), new File(directory.getAbsolutePath() + SEPARATOR + fileName), zip);
            } else {
                addFileToZip(path + SEPARATOR + directory.getName(),
                        new File(directory.getAbsolutePath() + SEPARATOR + fileName),
                        zip);
            }
        }
    }
}