package fdit.storage.recording.baseStation;

import fdit.database.DatabaseTestCase;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.storage.recording.TestRecordingInDatabaseLoadingCallback;
import fdit.testTools.rules.FileSystemPlugin;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.io.Files.write;
import static fdit.database.FditDatabaseHelper.*;
import static fdit.metamodel.aircraft.AircraftHelper.*;
import static fdit.metamodel.recording.RecordingHelper.*;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.testTools.PredicateAssert.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SbsContentLoaderTest extends DatabaseTestCase {

    @Rule
    public final FileSystemPlugin fileSystem = new FileSystemPlugin();

    private static BaseStationRecording loadRecording(final File file) {
        final BaseStationRecording recording = new BaseStationRecording(buildFditElementName(file),
                new SbsContentLoader(file, new TestRecordingInDatabaseLoadingCallback()));
        recording.load();
        return recording;
    }

    @Test
    public void testLoadSbs() throws IOException {
        final File sbsFile = new File(fileSystem.getRoot(), "messages.sbs");
        write("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161,RJA1118,,,,,,,,,,,\n" +
                        "MSG,1,496,603,400CB6,11267,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,CSA1118,,,,,,,,,,,\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,0,76.4,258.3,54.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,10,76.4,258.3,90,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,10,76.4,258.3,90,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,10,76.4,258.3,90,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.414,2008/10/13,12:28:52.074,,20,76.4,258.3,90,-4.38826,,,,,,0\n" +
                        "MSG,4,496,469,4CA767,27854,2010/02/19,17:58:13.039,2010/02/19,17:58:13.368,,,288.6,103.2,,,-832,,,,,\n" +
                        "MSG,6,496,237,4CA215,27864,2010/02/19,17:58:12.846,2010/02/19,17:58:13.368,,33325,,,,,,0271,0,0,0,0",
                sbsFile,
                UTF_8);
        assertThat(loadRecording(sbsFile),
                aRecording(
                        withAircrafts(
                                anAircraft(id(603),
                                        icao(0x400CB6),
                                        callsign("CSA1118"),
                                        firstAppearance(0),
                                        lastAppearance(0),
                                        withMinMaxAltitude(0, 20))),
                        withMaxRelativeDate(42701620625L)));

        checkDatabase(
                recordingTable(
                        row(cell("file_name", "messages.sbs"), cell("max_relative_date", 42701620625L))),

                aircraftTable(
                        row(cell("fdit_id", 603), cell("icao", 0x400CB6), cell("call_sign", "CSA1118"))),

                sbsStateTable(
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", 54.05735),
                                cell("longitude", -4.38826),
                                cell("altitude", 0d),
                                cell("ground_speed", 76.4),
                                cell("track", 258.3),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", null),
                                cell("emergency", null),
                                cell("spi", null),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", 90d),
                                cell("longitude", -4.38826),
                                cell("altitude", 10d),
                                cell("ground_speed", 76.4),
                                cell("track", 258.3),
                                cell("vertical_rate", null),
                                cell("alert", null),
                                cell("emergency", null),
                                cell("spi", null),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", 90d),
                                cell("longitude", -4.38826),
                                cell("altitude", 10d),
                                cell("ground_speed", 76.4),
                                cell("track", 258.3),
                                cell("vertical_rate", null),
                                cell("alert", null),
                                cell("emergency", null),
                                cell("spi", null),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", 90d),
                                cell("longitude", -4.38826),
                                cell("altitude", 10d),
                                cell("ground_speed", 76.4),
                                cell("track", 258.3),
                                cell("vertical_rate", null),
                                cell("alert", null),
                                cell("emergency", null),
                                cell("spi", null),
                                cell("is_on_ground", "0")),
                        row(
                                cell("aircraft_table_id", 1L),
                                cell("relative_date", 0L),
                                cell("latitude", 90d),
                                cell("longitude", -4.38826),
                                cell("altitude", 20d),
                                cell("ground_speed", 76.4),
                                cell("track", 258.3),
                                cell("vertical_rate", null),
                                cell("squawk", null),
                                cell("alert", null),
                                cell("emergency", null),
                                cell("spi", null),
                                cell("is_on_ground", "0"))));
    }

    @Test
    public void loadSbsFile_missingUselessFieldsAtEnd() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.sbs");
        write("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161,RJA1118,,", bstFile, UTF_8);
        assertThat(loadRecording(bstFile), aRecording(withAircrafts()));
    }

    @Test
    public void loadSbsFile_missingUsefulFields() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.sbs");
        write("MSG,1,145,256,7404F2,11267,2008/11/28,23:48:18.611,2008/11/28,23:53:19.161", bstFile, UTF_8);
        assertThat(loadRecording(bstFile), aRecording(withAircrafts()));
    }

    @Test
    public void loadSbsFile_malformedField() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.sbs");
        write("MSG,1,145,256,7404F2,11267,NOT_A_DATE,23:48:18.611,NOT_A_DATE,23:53:19.161,RJA1118,,\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.050,2008/10/13,12:28:52.074,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.060,2008/10/13,12:28:52.074,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.070,2008/10/13,12:28:52.074,,0,76.4,258.3,44.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.071,2008/10/13,12:28:52.074,,0,76.4,258.3,44.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.072,2008/10/13,12:28:52.074,,0,76.4,258.3,44.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.073,2008/10/13,12:28:52.074,,0,76.4,258.3,44.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.074,2008/10/13,12:28:52.074,,0,76.4,258.3,44.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.080,2008/10/13,12:28:52.074,,0,76.4,258.3,94.05735,-4.38826,,,,,,0\n" +
                        "MSG,2,496,603,400CB6,13168,2008/10/13,12:24:32.100,2008/10/13,12:28:52.074,,0,76.4,258.3,54.05735,-184.38826,,,,,,0\n",
                bstFile,
                UTF_8);
        assertThat(loadRecording(bstFile), aRecording(
                withAircrafts(
                        anAircraft(id(603),
                                icao(0x400CB6),
                                firstAppearance(20),
                                lastAppearance(24),
                                withMinMaxAltitude(0, 0)))));
    }
}