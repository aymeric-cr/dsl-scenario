package fdit.dsl.ide;

import java.util.Collection;

public class StylesPosition {

    private final int offset;
    private final int length;
    private final Collection<String> styles;

    StylesPosition(final int offset, final int length, final Collection<String> styles) {
        this.offset = offset;
        this.length = length;
        this.styles = styles;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public Collection<String> getStyles() {
        return styles;
    }
}
