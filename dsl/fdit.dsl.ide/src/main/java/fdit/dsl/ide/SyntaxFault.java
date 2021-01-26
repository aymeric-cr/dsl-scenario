package fdit.dsl.ide;

public class SyntaxFault {

    private final int offset;
    private final int line;
    private final int column;
    private final int length;
    private final String message;

    public SyntaxFault(final int offset, final int line, final int column, final int length, final String message) {
        this.offset = offset;
        this.line = line;
        this.column = column;
        this.length = length;
        this.message = message;
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getLength() {
        return length;
    }

    public String getMessage() {
        return message;
    }
}
