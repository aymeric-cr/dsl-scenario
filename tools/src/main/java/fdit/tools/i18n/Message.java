package fdit.tools.i18n;

final class Message {
    private Message() {
    }

    static Key key(final String key) {
        return new Key(key);
    }

    static final class Key {
        private final String key;

        private Key(final String key) {
            this.key = key;
        }

        String getValue() {
            return key;
        }
    }

}
