package fdit.ltlcondition

import com.google.inject.Inject
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.common.services.DefaultTerminalConverters

class LTLConditionTerminalConverters extends DefaultTerminalConverters {


    @Inject StringLiteralConverter stringLiteralConverter

    @ValueConverter(rule = "T_STRING_LITERAL")
    def IValueConverter<String> convertForStringLiteral() {
        stringLiteralConverter;
    }
}