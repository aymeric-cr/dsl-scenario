package fdit.tools.i18n;

import fdit.tools.i18n.Message.Key;

import java.util.Locale;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public final class TranslatorUtils {
    private static final Pattern COMPILED_APOSTROPHE = Pattern.compile("'");

    private TranslatorUtils() {
    }

    static String formatMessage(final CharsetResourceBundle bundle, final Key key, final Object... parameters) {
        return format(COMPILED_APOSTROPHE.matcher(bundle.getString(key.getValue())).replaceAll("''"), parameters);
    }

    public static CharsetResourceBundle getResourceBundleForClass(final Class<?> klass) {
        return getResourceBundleForClass(klass, Locale.getDefault());
    }

    static CharsetResourceBundle getResourceBundleForClass(final Class<?> klass, final Locale locale) {
        return new CharsetResourceBundle(getBundle(klass.getName() + "Messages", locale, klass.getClassLoader()));
    }
}