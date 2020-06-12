package fdit.storage;

import java.io.*;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static fdit.gui.application.FditApplication.USER_FDIT_PATH;
import static java.io.File.separatorChar;

public class LocalFditPropertiesFileStorage {

    public static final String DEFAULT_LOCATION_PROPERTY = "defaultLocation";
    public static final String LANG_PROPERTY = "lang";
    private final static String splitSeparator = "=";

    private static File propertiesFile;

    private LocalFditPropertiesFileStorage() {

    }

    private static void replaceLineToPropertiesStorage(String key, String newValue) {
        try {
            final BufferedReader file = new BufferedReader(new FileReader(getPropertiesFile()));
            final StringBuilder inputBuffer = new StringBuilder();
            String line;
            while ((line = file.readLine()) != null) {
                if (line.split(splitSeparator).length == 2) {
                    if (line.split(splitSeparator)[0].equals(key)) {
                        line = key + splitSeparator + newValue;
                    }
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();
            FileOutputStream fileOut = new FileOutputStream(getPropertiesFile());
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    public static void writeToLocalPropertiesStorage(final String key, final String value) {
        final String currentValue = readFromLocalPropertiesStorage(key);
        if (!currentValue.equals("")) {
            if (!currentValue.equals(value)) {
                replaceLineToPropertiesStorage(key, value);
            }
        } else {
            try {
                final BufferedWriter output = new BufferedWriter(new FileWriter(getPropertiesFile(), true));
                output.write(key + splitSeparator + value + "\n");
                output.flush();
            } catch (IOException e) {
                throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }
    }

    public static String readFromLocalPropertiesStorage(final String key) {
        final BufferedReader reader;
        String value = "";
        try {
            reader = new BufferedReader(new FileReader(getPropertiesFile()));
            String line = reader.readLine();
            while (line != null) {
                if (line.split(splitSeparator).length == 2) {
                    if (line.split(splitSeparator)[0].equals(key)) {
                        value = line.split(splitSeparator)[1];
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        return value;
    }

    private static File getPropertiesFile() {
        if (propertiesFile != null) {
            return propertiesFile;
        } else {
            propertiesFile = new File(USER_FDIT_PATH + separatorChar + ".fditLocalPropertiesATC.ini");
            if (!propertiesFile.exists()) {
                try {
                    propertiesFile.createNewFile();
                } catch (IOException e) {
                    throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            }
        }
        return propertiesFile;
    }
}