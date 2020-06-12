package fdit.storage.recording.baseStation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static java.lang.Integer.parseInt;
import static java.time.temporal.ChronoField.*;

public final class BaseStationParserUtils {

    private static final String BST_DATE_PATTERN = "yyyy/MM/dd,HH:mm:ss.SSS";

    private BaseStationParserUtils() {
    }

    public static List<String> splitMessage(final String message) {
        return Arrays.asList(message.replaceAll("\"", "").split(",", -1));
    }

    static String parseTransmissionType(final List<String> message) {
        return message.get(1);
    }

    public static int parseAircraftId(final List<String> message) {
        return parseInt(message.get(3));
    }

    static int parseIcao(final List<String> message) {
        return parseInt(message.get(4), 16);
    }

    static long parseDateMessageGenerated(final String dateStr) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(BST_DATE_PATTERN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(dateStr).toInstant().toEpochMilli();
    }

    static LocalDateTime baseStationFormatToDate(final CharSequence messageDate) {
        return LocalDateTime.parse(messageDate, baseStationDateFormatter());
    }

    static DateTimeFormatter baseStationDateFormatter() {
        return new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4)
                .appendLiteral('/')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('/')
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(',')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .appendLiteral('.')
                .appendValue(MILLI_OF_SECOND, 3)
                .toFormatter();
    }
}
