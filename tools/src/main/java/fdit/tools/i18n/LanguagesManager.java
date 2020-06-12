package fdit.tools.i18n;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;


@SuppressWarnings("Singleton")
public final class LanguagesManager implements Observable {

    public static final LanguagesManager LANGUAGES_MANAGER = new LanguagesManager();
    private final List<InvalidationListener> invalidationListeners = newArrayList();

    private LanguagesManager() {
    }

    public void setLocale(final Locale locale) {
        Locale.setDefault(locale);
        notifyListeners();
    }

    private void notifyListeners() {
        for (final InvalidationListener listener : invalidationListeners) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(final InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }
}
