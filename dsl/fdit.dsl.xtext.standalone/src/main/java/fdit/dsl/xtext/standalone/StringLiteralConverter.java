package fdit.dsl.xtext.standalone;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;

class StringLiteralConverter extends AbstractLexerBasedConverter<String> {

    private static String stripQuotes(final String s) {
        return s.substring(0, s.length() - 1).substring(1);
    }

    @Override
    protected String toEscapedString(final String value) {
        return stripQuotes(value);
    }

    @Override
    public String toValue(final String s, final INode node) throws ValueConverterException {
        return stripQuotes(s);
    }
}
