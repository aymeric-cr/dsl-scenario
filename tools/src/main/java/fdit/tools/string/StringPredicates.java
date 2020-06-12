package fdit.tools.string;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.google.common.base.Ascii.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

public final class StringPredicates {

    private StringPredicates() {
    }

    public static Predicate<String> equalsIgnoringCase(final CharSequence reference) {
        return element -> equalsIgnoreCase(element, reference);
    }

    public static Predicate<String> startingWith(final String prefix) {
        return text -> text.startsWith(prefix);
    }

    public static Predicate<String> containing(final CharSequence content) {
        return text -> text.contains(content);
    }

    public static Predicate<String> containingIgnoringCase(final String content) {
        return text -> containsIgnoreCase(text, content);
    }

    public static Predicate<String> matches(final String regexp) {
        return input -> Pattern.compile(regexp).matcher(input).matches();
    }
}
