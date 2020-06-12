package fdit.ltlcondition

import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.conversion.ValueConverterException

class StringLiteralConverter extends AbstractLexerBasedConverter<String> {

    override toEscapedString(String value) {
        stripQuotes(value)
    }

    private static def String stripQuotes(String s) {
        return s.substring(0, s.length() - 1).substring(1);
    }

    override toValue(String string, INode node) throws ValueConverterException {
        stripQuotes(string)
    }

}