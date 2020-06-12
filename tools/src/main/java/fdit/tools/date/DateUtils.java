package fdit.tools.date;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public final class DateUtils {
    private DateUtils() {
    }

    public static long computeRelativeDate(final long reference,
                                           final long toRelativize) {
        return toRelativize - reference;
    }

    public static long secondsToMillis(final double dateSeconds) {
        return (long) (dateSeconds * 1000);
    }

    public static double millisToSeconds(final long dateMillis) {
        return (double) dateMillis / 1000;
    }

    public static String dateMillisToHourMinSec(long dateMillis) {
        final long hours = TimeUnit.MILLISECONDS.toHours(dateMillis);
        dateMillis -= TimeUnit.HOURS.toMillis(hours);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(dateMillis);
        dateMillis -= TimeUnit.MINUTES.toMillis(minutes);
        final long secondes = TimeUnit.MILLISECONDS.toSeconds(dateMillis);
        return format("%02d h %02d min %02d sec", hours, minutes, secondes);
    }
}