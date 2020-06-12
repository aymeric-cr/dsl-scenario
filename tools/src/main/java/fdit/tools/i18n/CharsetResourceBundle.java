package fdit.tools.i18n;

import java.nio.charset.Charset;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CharsetResourceBundle {

    private final ResourceBundle bundle;
    private final Charset charset;

    public CharsetResourceBundle(final ResourceBundle bundle, final Charset charset) {
        this.bundle = bundle;
        this.charset = charset;
    }

    public CharsetResourceBundle(final ResourceBundle bundle) {
        this(bundle, UTF_8);
    }

    public String getString(final String key) {
        final String value = bundle.getString(key);
        return new String(value.getBytes(ISO_8859_1), charset);
    }
}