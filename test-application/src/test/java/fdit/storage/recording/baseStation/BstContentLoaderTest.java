package fdit.storage.recording.baseStation;

import fdit.database.DatabaseTestCase;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.storage.recording.TestRecordingInDatabaseLoadingCallback;
import fdit.testTools.rules.FileSystemPlugin;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.asCharSink;
import static com.google.common.io.Files.write;
import static fdit.database.FditDatabaseHelper.*;
import static fdit.metamodel.aircraft.AircraftHelper.*;
import static fdit.metamodel.recording.RecordingHelper.*;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.testTools.PredicateAssert.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;


public class BstContentLoaderTest extends DatabaseTestCase {

    @Rule
    public final FileSystemPlugin fileSystem = new FileSystemPlugin();

    private static BaseStationRecording loadRecording(final File file) {
        final BaseStationRecording recording = new BaseStationRecording(buildFditElementName(file),
                new BstContentLoader(file, new TestRecordingInDatabaseLoadingCallback()));
        recording.load();
        return recording;
    }

    @Test
    public void testLoadBst() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        write("MSG,2,1,368,0A0075,18009,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,,0,27.0,264.4,48.72313,2.37821,,,,,,-1\n" +
                        "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.050,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.100,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,1,1,368,0A0075,18009,2016/12/09,09:14:03.200,2016/12/09,09:14:05.486,RJA1118,,,,,,,,,,,\n" +
                        "MSG,2,1,4959,4CA8F6,17987,2016/12/09,09:14:03.300,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,4959,4CA8F6,17987,2016/12/09,09:14:03.305,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,4959,4CA8F6,17987,2016/12/09,09:14:03.315,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,4959,4CA8F6,17987,2016/12/09,09:14:03.320,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.350,2016/12/09,09:14:06.584,,3200,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.400,2016/12/09,09:14:09.554,,2800,,,,,,5750,0,0,0,0\n" +
                        "MSG,7,1,4962,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,32000,,,,,,,,,,0\n" +
                        "MSG,1,1,3007,AF3007,93007,2016/12/09,09:14:05.500,2016/12/09,09:14:12.568,CS3007,,,,,,,,,,,\n" +
                        "MSG,2,1,3007,AF3007,93007,2016/12/09,09:14:05.520,2016/12/09,09:14:12.568,,4000,,,45.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,3007,AF3007,93007,2016/12/09,09:14:05.540,2016/12/09,09:14:12.568,,4000,,,45.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,3007,AF3007,93007,2016/12/09,09:14:05.560,2016/12/09,09:14:12.568,,4000,,,45.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,3007,AF3007,93007,2016/12/09,09:14:05.580,2016/12/09,09:14:12.568,,4000,,,45.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,2,1,3007,AF3007,93007,2016/12/09,09:14:05.600,2016/12/09,09:14:12.568,,4200,,,46.76740,2.62024,,,0,0,0,0",
                bstFile,
                UTF_8);
        assertThat(loadRecording(bstFile),
                aRecording(
                        withAircrafts(
                                anAircraft(id(4959),
                                        icao(0x4CA8F6),
                                        callsign("CS1501"),
                                        firstAppearance(200),
                                        lastAppearance(250),
                                        withMinMaxAltitude(2900, 3200)),
                                anAircraft(id(3007),
                                        icao(0xAF3007),
                                        callsign("CS3007"),
                                        firstAppearance(2420),
                                        lastAppearance(2500),
                                        withMinMaxAltitude(4000, 4200))),
                        withMaxRelativeDate(2600)));
        checkDatabase(
                recordingTable(
                        row(cell("file_name", "messages.bst"), cell("max_relative_date", 2600L))),
                aircraftTable(
                        row(cell("fdit_id", 4959), cell("icao", 0x4CA8F6), cell("call_sign", "CS1501")),
                        row(cell("fdit_id", 3007), cell("icao", 0xAF3007), cell("call_sign", "CS3007"))),

                sbsStateTable(
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", null),
                                cell("longitude", null),
                                cell("altitude", 3375d),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", null),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 200L),
                                cell("latitude", 48.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 2900.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 205L),
                                cell("latitude", 48.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 2900.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 215L),
                                cell("latitude", 48.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 2900.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 220L),
                                cell("latitude", 48.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 2900.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 250L),
                                cell("latitude", 48.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 3200.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 300L),
                                cell("latitude", null),
                                cell("longitude", null),
                                cell("altitude", 2800.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", (short) 5750),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 2L),
                                cell("relative_date", 2420L),
                                cell("latitude", 45.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 4000.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 2L),
                                cell("relative_date", 2440L),
                                cell("latitude", 45.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 4000.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 2L),
                                cell("relative_date", 2460L),
                                cell("latitude", 45.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 4000.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 2L),
                                cell("relative_date", 2480L),
                                cell("latitude", 45.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 4000.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 2L),
                                cell("relative_date", 2500L),
                                cell("latitude", 46.76740),
                                cell("longitude", 2.62024),
                                cell("altitude", 4200.0),
                                cell("ground_speed", null),
                                cell("track", null),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", "0"),
                                cell("emergency", "0"),
                                cell("spi", "0"),
                                cell("is_on_ground", "0"))));
    }

    @Test
    public void loadBstFile_missingUselessFieldsAtEnd() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161,RJA1118,,");
        assertThat(loadRecording(bstFile), aRecording(withAircrafts()));
    }

    @Test
    public void loadBstFile_missingUsefulFields() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161");
        assertThat(loadRecording(bstFile), aRecording(withAircrafts()));
    }

    @Test
    public void loadBstFile_malformedField() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write("MSG,1,145,256,7404F2,11267,NOT_A_DATE,23:48:18.000,NOT_A_DATE,23:53:19.161,RJA1118,,,,,,,,,,,\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.050,2008/10/13,12:48:18.050,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.100,2008/10/13,12:48:18.100,,0,76.4,259.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.150,2008/10/13,12:48:18.150,,0,76.4,257.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.200,2008/10/13,12:48:18.200,,0,76.4,256.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.250,2008/10/13,12:48:18.250,,0,76.4,255.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.300,2008/10/13,12:48:18.300,,0,76.4,255.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.350,2008/10/13,12:48:18.350,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.400,2008/10/13,12:48:18.400,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.450,2008/10/13,12:48:18.450,,0,76.4,258.3,54.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.500,2008/10/13,12:48:18.500,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                "MSG,2,496,603,400CB6,13168,2008/10/13,12:48:18.550,2008/10/13,12:48:18.550,,0,76.4,258.3,54.05735,-184.38826,,,,,,0");
        assertThat(loadRecording(bstFile), aRecording(withAircrafts(
                anAircraft(id(603),
                        icao(0x400CB6),
                        firstAppearance(50),
                        lastAppearance(400),
                        withMinMaxAltitude(0, 0))
        )));
    }
}