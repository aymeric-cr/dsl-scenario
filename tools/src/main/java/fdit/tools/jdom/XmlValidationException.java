package fdit.tools.jdom;

import org.xml.sax.SAXParseException;

public class XmlValidationException extends RuntimeException {
    public XmlValidationException(final String location, final SAXParseException e) {
        super(
                "File "
                        + location
                        + ", line "
                        + e.getLineNumber()
                        + ", column "
                        + e.getColumnNumber()
                        + ": "
                        + e.getMessage(), e);
    }
}
