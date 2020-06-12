package fdit.tools.jdom;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.SlimJDOMFactory;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.validation.SchemaFactory.newInstance;

public final class XmlValidationUtils {
    private XmlValidationUtils() {
    }

    public static Document validateAgainstSchema(
            final String location, final InputStream xmlContent, final InputStream xsdSchema) throws RuntimeException {
        try {
            final SAXBuilder builder = new SAXBuilder(
                    new XMLReaderSchemaFactory(
                            newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(new SAXSource(new InputSource(xsdSchema)))),
                    null,
                    new SlimJDOMFactory(true));
            builder.setErrorHandler(createErrorHandler(location));
            return builder.build(new InputSource(xmlContent));
        } catch (final IOException | SAXException | JDOMException e) {
            throw new RuntimeException(e);
        }
    }

    private static ErrorHandler createErrorHandler(final String location) throws XmlValidationException {
        return new ErrorHandler() {
            public void warning(final SAXParseException e) {
                throw new XmlValidationException(location, e);
            }

            public void error(final SAXParseException e) {
                throw new XmlValidationException(location, e);
            }

            public void fatalError(final SAXParseException e) {
                throw new XmlValidationException(location, e);
            }
        };
    }
}
