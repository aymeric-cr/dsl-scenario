package fdit.tools.i18n;

import fdit.tools.i18n.Message.Key;

import java.util.Locale;

import static fdit.tools.i18n.LanguagesManager.LANGUAGES_MANAGER;

public class MessageTranslator {
    private final Class<?> klass;
    private CharsetResourceBundle bundle;

    private MessageTranslator(final Class<?> klass) {
        this.klass = klass;
        LANGUAGES_MANAGER.addListener(observable -> initBundle());
        initBundle();
    }

    public static MessageTranslator createMessageTranslator(final Class<?> klass) {
        return new MessageTranslator(klass);
    }

    private void initBundle() {
        bundle = TranslatorUtils.getResourceBundleForClass(klass, Locale.getDefault());
    }

    public final String getMessage(final String key) {
        return bundle.getString(key);
    }

    public final String getMessage(final String key, final Object... parameters) {
        return formatMessage(Message.key(key), parameters);
    }

    private String formatMessage(final Key key, final Object... parameters) {
        return TranslatorUtils.formatMessage(bundle, key, parameters);
    }

}