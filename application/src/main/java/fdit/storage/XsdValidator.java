package fdit.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static fdit.tools.jdom.XmlValidationUtils.validateAgainstSchema;

public final class XsdValidator {

    private static final String ALTERATION_XSD_FILE_NAME = "alterationsXSD.xsd";
    private static final String EXECUTION_XSD_FILE_NAME = "executionXSD.xsd";
    private static final String ZONE_XSD_FILE_NAME = "zonesXSD.xsd";

    private XsdValidator() {
    }

    public static boolean isValidAlterationFile(final File alterationFile) {
        return validateXmlFile(alterationFile, ALTERATION_XSD_FILE_NAME);
    }

    public static boolean isValidZoneFile(final File zoneFile) {
        return validateXmlFile(zoneFile, ZONE_XSD_FILE_NAME);
    }

    public static boolean isValidExecutionFile(final File executionFile) {
        return validateXmlFile(executionFile, EXECUTION_XSD_FILE_NAME);
    }

    private static boolean validateXmlFile(final File xmlFile, final String xsdFileName) {
        try (final InputStream alterationXsdIS = getXsdInputStream(xsdFileName)) {
            try (final InputStream xmlContent = new FileInputStream(xmlFile)) {
                validateAgainstSchema(xsdFileName, xmlContent, alterationXsdIS);
                return true;
            }
        } catch (final Exception ignored) {
            return false;
        }
    }

    private static InputStream getXsdInputStream(final String xsdFileName) {
        return XsdValidator.class.getResourceAsStream("/xsd/" + xsdFileName);
    }
}