package fdit.tools.io;

import java.io.File;
import java.io.FilenameFilter;

import static com.google.common.io.Files.getFileExtension;

public class FileExtensionFilter implements FilenameFilter {

    private final String extension;

    public FileExtensionFilter(final String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(final File dir, final String name) {
        return getFileExtension(name).equals(extension);
    }
}
