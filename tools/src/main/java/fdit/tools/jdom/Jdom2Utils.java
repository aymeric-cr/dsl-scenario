package fdit.tools.jdom;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static fdit.tools.collection.ConsumerUtils.acceptAll;
import static org.jdom2.output.Format.getPrettyFormat;

public final class Jdom2Utils {
    private Jdom2Utils() {
    }

    public static void writeDocument(final Document document, final OutputStream outputStream) throws IOException {
        final XMLOutputter outputter = new XMLOutputter(getPrettyFormat());
        outputter.output(document, outputStream);
    }

    public static Document createFromString(final String xml) {
        try {
            return new SAXBuilder().build(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (final JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document createFromInputStream(final InputStream inputStream) {
        try {
            return new SAXBuilder().build(inputStream);
        } catch (final JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String renderDocument(final Document description, final Format format) {
        return new XMLOutputter(format).outputString(description);
    }

    public static String renderElement(final Element element) {
        return new XMLOutputter().outputString(element);
    }

    public static String renderContent(final List<? extends Content> content) {
        return new XMLOutputter().outputString(content);
    }

    public static Element createElement(final String name, final Consumer<Element>... processors) {
        return acceptAll(new Element(name), processors);
    }

    public static Element createElement(
            final String name, final Namespace nameSpace, final Consumer<Element>... processors) {
        return acceptAll(new Element(name, nameSpace), processors);
    }

    public static Consumer<Element> composite(final Consumer<Element>... processors) {

        return element -> acceptAll(element, processors);
    }

    public static Consumer<Element> attribute(final String name, final String value) {
        return element -> element.setAttribute(name, value);
    }

    public static Consumer<Element> attribute(final Namespace namespace, final String name, final String value) {
        return element -> element.setAttribute(name, value, namespace);
    }

    public static Consumer<Element> child(final Content child) {
        return element -> element.addContent(child);
    }

    public static Consumer<Element> child(final String name, final Consumer<Element>... processors) {
        return element -> element.addContent(createElement(name, processors));
    }

    public static Consumer<Element> child(
            final String name, final Namespace namespace, final Consumer<Element>... processors) {
        return element -> element.addContent(createElement(name, namespace, processors));
    }

    public static Consumer<Element> children(final Collection<Element> elements) {
        return element -> element.addContent(elements);
    }

    public static Consumer<Element> children(final Element... elements) {
        return children(Arrays.asList(elements));
    }

    public static Consumer<Element> text(final String text) {
        return element -> element.addContent(new Text(text));
    }
}
