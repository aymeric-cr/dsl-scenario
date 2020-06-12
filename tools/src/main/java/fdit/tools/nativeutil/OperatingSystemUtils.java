package fdit.tools.nativeutil;


import java.io.File;
import java.util.Arrays;

import static java.lang.System.getProperty;
import static java.util.Arrays.binarySearch;

@SuppressWarnings("ConstantNamingConvention")
public final class OperatingSystemUtils {
    public static final int GNU = 9;
    public static final int UNSPECIFIED = -1;
    public static final int MAC = 0;
    public static final int LINUX = 1;
    public static final int WINDOWS = 2;
    public static final int SOLARIS = 3;
    public static final int FREEBSD = 4;
    public static final int OPENBSD = 5;
    public static final int WINDOWSCE = 6;
    public static final int AIX = 7;
    public static final int ANDROID = 8;
    public static final int KFREEBSD = 10;
    public static final int NETBSD = 11;
    public static final String[] INVALID_RESOURCE_BASENAMES;
    public static final String[] INVALID_RESOURCE_FULLNAMES;
    private static final int OS_TYPE;
    private static final String ARCH = getProperty("os.arch").toLowerCase().trim();

    static {
        final String osName = getProperty("os.name");
        //noinspection IfStatementWithTooManyBranches
        if (osName.startsWith("Linux")) {
            if ("dalvik".equals(getProperty("java.vm.name").toLowerCase())) {
                OS_TYPE = ANDROID;
                // Native libraries on android must be bundled with the APK
                System.setProperty("jna.nounpack", "true");
            } else {
                OS_TYPE = LINUX;
            }
        } else if (osName.startsWith("AIX")) {
            OS_TYPE = AIX;
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            OS_TYPE = MAC;
        } else if (osName.startsWith("Windows CE")) {
            OS_TYPE = WINDOWSCE;
        } else if (osName.startsWith("Windows")) {
            OS_TYPE = WINDOWS;
        } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            OS_TYPE = SOLARIS;
        } else if (osName.startsWith("FreeBSD")) {
            OS_TYPE = FREEBSD;
        } else if (osName.startsWith("OpenBSD")) {
            OS_TYPE = OPENBSD;
        } else if ("gnu".equalsIgnoreCase(osName)) {
            OS_TYPE = GNU;
        } else if ("gnu/kfreebsd".equalsIgnoreCase(osName)) {
            OS_TYPE = KFREEBSD;
        } else if ("netbsd".equalsIgnoreCase(osName)) {
            OS_TYPE = NETBSD;
        } else {
            OS_TYPE = UNSPECIFIED;
        }

        if (isWindows()) {
            INVALID_RESOURCE_BASENAMES = new String[]{"aux", "com1", "com2", "com3", "com4",
                    "com5", "com6", "com7", "com8", "com9", "con", "lpt1", "lpt2",
                    "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "nul", "prn"};
            Arrays.sort(INVALID_RESOURCE_BASENAMES);
            INVALID_RESOURCE_FULLNAMES = new String[]{"clock$"};
        } else {
            INVALID_RESOURCE_BASENAMES = null;
            INVALID_RESOURCE_FULLNAMES = null;
        }
    }

    private OperatingSystemUtils() {
    }

    public static boolean isWindows() {
        return OS_TYPE == WINDOWS || OS_TYPE == WINDOWSCE;
    }

    public static boolean isLinux() {
        return OS_TYPE == LINUX || OS_TYPE == GNU;
    }

    public static boolean isMac() {
        return OS_TYPE == MAC;
    }


    /**
     * @returns - win-x86-32
     * - win-x86-64
     * - linux-x86-32
     * - linux-x86-64
     * - linux-ppc-32
     * - linux-ppc-64
     * - darwin-64
     */
    public static String getOsAndArchitectureInformation() {
        final StringBuilder builder = new StringBuilder(20);
        switch (OS_TYPE) {
            case MAC:
                builder.append("darwin");
                break;
            case LINUX:
                builder.append("linux");
                break;
            case WINDOWS:
                builder.append("windows");
                break;
            case SOLARIS:
                builder.append("solaris");
                break;
            case FREEBSD:
                builder.append("freebsd");
                break;
            case OPENBSD:
                builder.append("openbsd");
                break;
            case WINDOWSCE:
                builder.append("wince");
                break;
            default:
                builder.append("unspecified");
        }
        final String arch = getProperty("os.arch").toLowerCase();
        if (arch.contains("x86") || arch.contains("i386") || arch.contains("amd")) {
            builder.append("-x86");
        } else if (arch.contains("ppc")) {
            builder.append("-ppc");
        } else {
            builder.append("-unknown");
        }
        builder.append('-');
        builder.append(getArchitectureBits());
        return builder.toString();
    }

    public static int getArchitectureBits() {
        if (is64Bit()) {
            return 64;
        } else {
            return 32;
        }
    }


    public static boolean is64Bit() {
        final String model = getProperty("sun.arch.data.model",
                getProperty("com.ibm.vm.bitmode"));
        if (model != null) {
            return "64".equals(model);
        }
        return "x86_64".equals(ARCH)
                || "ia64".equals(ARCH)
                || "ppc64".equals(ARCH)
                || "sparcv9".equals(ARCH)
                || "amd64".equals(ARCH);
    }

    public static boolean isNameValid(final String name) {
        if (".".equals(name) || "..".equals(name)) {
            return false;
        }
        if (isWindows()) {
            final int length = name.length();
            if (length == 0) {
                return false;
            }
            final char lastChar = name.charAt(length - 1);
            if (lastChar == '.') {
                return false;
            }
            if (Character.isWhitespace(lastChar)) {
                return false;
            }
            final int dot = name.indexOf('.');
            final String basename = dot == -1 ? name : name.substring(0, dot);
            return binarySearch(INVALID_RESOURCE_BASENAMES, basename.toLowerCase()) < 0
                    && binarySearch(INVALID_RESOURCE_FULLNAMES, name.toLowerCase()) < 0;
        }
        return true;
    }

    public static boolean isFileSystemCaseSensitive() {
        return !isMac() && new File("a").compareTo(new File("A")) != 0;
    }
}

