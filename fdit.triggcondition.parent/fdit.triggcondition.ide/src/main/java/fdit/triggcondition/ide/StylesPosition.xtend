package fdit.triggcondition.ide

import java.util.Collection

class StylesPosition {

    val int offset
    val int length
    val Collection<String> styles

    new(int offset, int length, Collection<String> styles) {
        this.offset = offset
        this.length = length
        this.styles = styles
    }

    def getOffset() {
        offset
    }

    def getLength() {
        length
    }

    def getStyles() {
        styles
    }
}