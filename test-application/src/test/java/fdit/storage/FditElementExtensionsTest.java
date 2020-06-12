package fdit.storage;

import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.zone.Zone;
import fdit.testTools.rules.FileSystemPlugin;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.write;
import static fdit.storage.FditElementExtensions.getElementTypeFrom;
import static fdit.testTools.PredicateAssert.assertEqual;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FditElementExtensionsTest {

    @Rule
    public final FileSystemPlugin filesystem = new FileSystemPlugin();

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeEmptyAlterationFile() throws IOException {
        final File myalterations = new File(filesystem.getRoot(), "myalterations.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<specification />", myalterations, UTF_8);
        assertEqual(getElementTypeFrom(myalterations).get(), AlterationSpecification.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeAlterationFile_withTrigger() throws IOException {
        final File myalterations = new File(filesystem.getRoot(), "myalterations.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<specification>\n" +
                "  <scenario name=\"sc\">\n" +
                "    <description>my scenario</description>\n" +
                "    <action type=\"alteration\">\n" +
                "      <name>Altération</name>\n" +
                "      <description>Description</description>\n" +
                "      <sID>ALL</sID>\n" +
                "      <scope type=\"trigger\">\n" +
                "        <time>100</time>\n" +
                "      </scope>\n" +
                "      <parameters>\n" +
                "        <target>ALL</target>\n" +
                "        <value type=\"icao\" offset=\"false\" assertion=\"test assertion\">AF1234</value>\n" +
                "      </parameters>\n" +
                "    </action>\n" +
                "  </scenario>\n" +
                "</specification>", myalterations, UTF_8);
        assertEqual(getElementTypeFrom(myalterations).get(), AlterationSpecification.class);
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeAlterationFile_withTimeWindow() throws IOException {
        final File myalterations = new File(filesystem.getRoot(), "myalterations.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<specification>\n" +
                "  <scenario name=\"sc\">\n" +
                "    <description>my scenario</description>\n" +
                "    <action type=\"alteration\">\n" +
                "      <name>Altération</name>\n" +
                "      <description>Description</description>\n" +
                "      <sID>ALL</sID>\n" +
                "      <scope type=\"timeWindow\">\n" +
                "        <lowerBound>100</lowerBound>\n" +
                "        <upperBound>200</upperBound>\n" +
                "      </scope>\n" +
                "      <parameters>\n" +
                "        <target>ALL</target>\n" +
                "        <value type=\"icao\" offset=\"false\" assertion=\"test assertion\">AF1234</value>\n" +
                "      </parameters>\n" +
                "    </action>\n" +
                "  </scenario>\n" +
                "</specification>", myalterations, UTF_8);
        assertEqual(getElementTypeFrom(myalterations).get(), AlterationSpecification.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeAlterationFile_withGeoTimeWindow() throws IOException {
        final File myalterations = new File(filesystem.getRoot(), "myalterations.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<specification>\n" +
                "  <scenario name=\"sc\">\n" +
                "    <description>my scenario</description>\n" +
                "    <action type=\"alteration\">\n" +
                "      <name>Altération</name>\n" +
                "      <description>Description</description>\n" +
                "      <sID>ALL</sID>\n" +
                "      <scope type=\"geoTimeWindow\">\n" +
                "        <polygon>\n" +
                "          <id>00000001-0001-0001-0001-000000000001</id>\n" +
                "          <name>my zone</name>\n" +
                "          <vertices>\n" +
                "            <vertex>\n" +
                "              <lat>50.1</lat>\n" +
                "              <lon>2.5</lon>\n" +
                "            </vertex>\n" +
                "            <vertex>\n" +
                "              <lat>48.6</lat>\n" +
                "              <lon>0.1</lon>\n" +
                "            </vertex>\n" +
                "            <vertex>\n" +
                "              <lat>50.9</lat>\n" +
                "              <lon>3.4</lon>\n" +
                "            </vertex>\n" +
                "          </vertices>\n" +
                "          <lowerAlt>10000</lowerAlt>\n" +
                "          <upperAlt>20000</upperAlt>\n" +
                "        </polygon>\n" +
                "        <lowerBound>100</lowerBound>\n" +
                "        <upperBound>200</upperBound>\n" +
                "      </scope>\n" +
                "      <parameters>\n" +
                "        <target>ALL</target>\n" +
                "        <value type=\"icao\" offset=\"false\" assertion=\"test assertion\">AF1234</value>\n" +
                "      </parameters>\n" +
                "    </action>\n" +
                "  </scenario>\n" +
                "</specification>", myalterations, UTF_8);
        assertEqual(getElementTypeFrom(myalterations).get(), AlterationSpecification.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeAlterationFile_withMassification() throws IOException {
        final File myalterations = new File(filesystem.getRoot(), "myalterations.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<specification>\n" +
                "   <scenario name=\"scenario_AFR\">\n" +
                "      <description>scenario_AFR</description>\n" +
                "      <action type=\"alteration\">\n" +
                "         <name>emergency</name>\n" +
                "         <description>description</description>\n" +
                "         <sID>ALL</sID>\n" +
                "         <scope type=\"timeWindow\">\n" +
                "           <lowerBound>30000</lowerBound>\n" +
                "           <upperBound>1086372</upperBound>\n" +
                "         </scope>\n" +
                "         <parameters>\n" +
                "           <target>2433,98,1252,41,586,170,652,429,719,1231,144,1330,246,631,856,2173,2142</target>\n" +
                "           <value type=\"longitude\">11</value>\n" +
                "           <value type=\"latitude\">\n" +
                "               <min>11</min>\n" +
                "               <max>22</max>\n" +
                "           </value>\n" +
                "           <value type=\"callsign\">AFR011</value>\n" +
                "           <value type=\"icao\" offset=\"false\" assertion=\"test assertion\">\n" +
                "               <item>AFR234</item>\n" +
                "               <item>TFR774</item>\n" +
                "               <item>ZFR235</item>\n" +
                "               <item>ADR134</item>\n" +
                "           </value>\n" +
                "         </parameters>\n" +
                "      </action>\n" +
                "   </scenario>\n" +
                "</specification>", myalterations, UTF_8);
        assertEqual(getElementTypeFrom(myalterations).get(), AlterationSpecification.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void recognizeZoneFile() throws IOException {
        final File myzone = new File(filesystem.getRoot(), "myzone.xml");
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<zone>" +
                        "<id>15</id>\n" +
                        "<lowerAlt>0000</lowerAlt>\n" +
                        "<upperAlt>40000</upperAlt>\n" +
                        "<polygon>\n" +
                        "<vertices>\n" +
                        "<vertex>\n" +
                        "<lat>48.85</lat>\n" +
                        "<lon>2.37</lon>\n" +
                        "</vertex>\n" +
                        "<vertex>\n" +
                        "<lat>48.44</lat>\n" +
                        "<lon>1.49</lon>\n" +
                        "</vertex>\n" +
                        "<vertex>\n" +
                        "<lat>48.17</lat>\n" +
                        "<lon>2.25</lon>\n" +
                        "</vertex>\n" +
                        "<vertex>\n" +
                        "<lat>48.81</lat>\n" +
                        "<lon>3.03</lon>\n" +
                        "</vertex>\n" +
                        "<vertex>\n" +
                        "<lat>49.40</lat>\n" +
                        "<lon>2.82</lon>\n" +
                        "</vertex>\n" +
                        "</vertices>\n" +
                        "</polygon>\n" +
                        "</zone>",
                myzone, UTF_8);
        assertEqual(getElementTypeFrom(myzone).get(), Zone.class);
    }
}