package fdit.ltlcondition.ide

class SyntaxFault {

    var int offset
    var int line
    var int column
    var int length
    var String message

    new(int offset, int line, int column, int length, String message) {
        this.offset = offset
        this.line = line
        this.column = column
        this.length = length
        this.message = message
    }

    def int getOffset() {
        offset
    }

    def int getLine() {
        line
    }

    def int getColumn() {
        column
    }

    def int getLength() {
        length
    }

    def String getMessage() {
        message
    }

}