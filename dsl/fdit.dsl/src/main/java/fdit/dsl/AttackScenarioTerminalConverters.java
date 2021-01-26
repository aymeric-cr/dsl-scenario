package fdit.dsl;

import com.google.inject.Inject;
import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

public class AttackScenarioTerminalConverters extends DefaultTerminalConverters {

    @Inject
    private StringLiteralConverter stringLiteralConverter;

    @ValueConverter(rule = "T_STRING_LITERAL")
    public IValueConverter<String> convertForStringLiteral() {
        return stringLiteralConverter;
    }

}
