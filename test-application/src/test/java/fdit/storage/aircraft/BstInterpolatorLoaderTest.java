package fdit.storage.aircraft;

import fdit.database.DatabaseTestCase;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.NullLoaderException;
import fdit.metamodel.aircraft.OutOfDateException;
import fdit.metamodel.recording.BaseStationRecording;
import fdit.storage.recording.TestRecordingInDatabaseLoadingCallback;
import fdit.storage.recording.baseStation.SbsContentLoader;
import fdit.testTools.rules.FileSystemPlugin;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.io.Files.asCharSink;
import static com.google.common.io.Files.write;
import static com.google.inject.internal.util.$Lists.newArrayList;
import static fdit.metamodel.aircraft.AircraftCriterion.*;
import static fdit.metamodel.aircraft.AircraftHelper.*;
import static fdit.metamodel.recording.RecordingHelper.aRecording;
import static fdit.metamodel.recording.RecordingHelper.withAircrafts;
import static fdit.storage.FditStorageUtils.buildFditElementName;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.tools.stream.StreamUtils.filter;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BstInterpolatorLoaderTest extends DatabaseTestCase {

    @Rule
    public final FileSystemPlugin fileSystem = new FileSystemPlugin();

    private static BaseStationRecording loadBaseStationRecording(final File file) {
        final BaseStationRecording recording = new BaseStationRecording(buildFditElementName(file),
                new SbsContentLoader(file, new TestRecordingInDatabaseLoadingCallback()));
        recording.load();
        return recording;
    }

    @Test
    public void testLoadInterpolator() throws IOException {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write(
                "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.001,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.002,2016/12/09,09:14:06.584,,3220,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.003,2016/12/09,09:14:06.584,,3240,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.004,2016/12/09,09:14:06.584,,3280,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.005,2016/12/09,09:14:06.584,,3320,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.006,2016/12/09,09:14:06.584,,3340,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.007,2016/12/09,09:14:06.584,,3400,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.008,2016/12/09,09:14:09.554,,3300,,,,,,5750,0,0,0,0");
        assertThat(loadBaseStationRecording(bstFile),
                aRecording(
                        withAircrafts(
                                anAircraft(id(4959),
                                        icao(0x4CA8F6),
                                        callsign("CS1501"),
                                        withFunctions(IS_ON_GROUND,
                                                aFunction(from(0), to(7), withKnots(8))),
                                        withFunctions(ALERT,
                                                aFunction(from(0), to(7), withKnots(8))),
                                        withFunctions(ALTITUDE,
                                                aFunction(from(1), to(6), withKnots(6))),
                                        withFunctions(SPI,
                                                aFunction(from(0), to(7), withKnots(8))),
                                        withFunctions(LATITUDE,
                                                aFunction(from(1), to(6), withKnots(6))),
                                        withFunctions(LONGITUDE,
                                                aFunction(from(1), to(6), withKnots(6))),
                                        withFunctions(EMERGENCY,
                                                aFunction(from(1), to(7), withKnots(7))),
                                        withoutFunction(GROUNDSPEED),
                                        withoutFunction(TRACK),
                                        withoutFunction(VERTICAL_RATE),
                                        withoutFunction(SQUAWK)))));
    }

    @Test
    public void testQueryInterpolator() throws Exception {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write(
                "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.001,2016/12/09,09:14:06.584,,3375,,,48.76740,2.62048,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.002,2016/12/09,09:14:06.584,,3240,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.003,2016/12/09,09:14:06.584,,3280,,,48.76740,2.62020,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.004,2016/12/09,09:14:06.584,,3320,,,48.76740,2.62016,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.005,2016/12/09,09:14:06.584,,3340,,,48.76740,2.62058,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.006,2016/12/09,09:14:06.584,,3150,,,48.76765,2.62020,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.007,2016/12/09,09:14:09.554,,3300,,,,,,5750,0,0,0,0");
        final Aircraft aircraft = filter(loadBaseStationRecording(bstFile).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 4959).get(0);
        // Good queries - LATITUDE
        assertThat(aircraft.query(1, LATITUDE) == 48.76740);
        assertThat(aircraft.query(4, LATITUDE) != 0);
        assertThat(aircraft.query(6, LATITUDE) == 48.76765);

        // Good queries - LONGITUDE
        assertThat(aircraft.query(2, LONGITUDE) == 2.62024);
        assertThat(aircraft.query(4, LONGITUDE) != 0);
        assertThat(aircraft.query(6, LONGITUDE) == 2.62020);

        // Good queries - ALTITUDE
        assertThat(aircraft.query(1, ALTITUDE) == 3375);
        assertThat(aircraft.query(4, ALTITUDE) != 0);
        assertThat(aircraft.query(6, ALTITUDE) == 3150);

        // query unexisting function
        assertThat(aircraft.query(200, GROUNDSPEED) == 0.0);
    }

    @Test(expected = NullLoaderException.class)
    public void testQueryExceptions_UninstanciedException() throws Exception {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write(
                "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.001,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.002,2016/12/09,09:14:06.584,,3220,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.003,2016/12/09,09:14:06.584,,3240,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.005,2016/12/09,09:14:06.584,,3280,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.006,2016/12/09,09:14:06.584,,3320,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.007,2016/12/09,09:14:06.584,,3340,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.008,2016/12/09,09:14:06.584,,3400,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.009,2016/12/09,09:14:09.554,,3300,,,,,,5750,0,0,0,0");
        final Aircraft aircraft = new Aircraft(1, 1);
        aircraft.query(1500, SPI);
    }

    @Test(expected = OutOfDateException.class)
    public void testQueryExceptions_OutOfDateException_early() throws Exception {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write(
                "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.001,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.002,2016/12/09,09:14:06.584,,3220,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.003,2016/12/09,09:14:06.584,,3240,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.004,2016/12/09,09:14:06.584,,3280,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.005,2016/12/09,09:14:06.584,,3320,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.006,2016/12/09,09:14:06.584,,3340,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.007,2016/12/09,09:14:06.584,,3400,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.008,2016/12/09,09:14:09.554,,3300,,,,,,5750,0,0,0,0");
        final Aircraft aircraft = filter(loadBaseStationRecording(bstFile).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 4959).get(0);
        // query unexisting date
        aircraft.query(0, LATITUDE);
    }

    @Test(expected = OutOfDateException.class)
    public void testQueryExceptions_OutOfDateException_late() throws Exception {
        final File bstFile = new File(fileSystem.getRoot(), "messages.bst");
        asCharSink(bstFile, UTF_8).write(
                "MSG,1,1,4959,4CA8F6,17987,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,4959,4CA8F6,17987,2016/12/09,09:14:03.001,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.002,2016/12/09,09:14:06.584,,3220,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.003,2016/12/09,09:14:06.584,,3240,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.004,2016/12/09,09:14:06.584,,3280,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.005,2016/12/09,09:14:06.584,,3320,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.006,2016/12/09,09:14:06.584,,3340,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,3,1,4959,4CA8F6,17987,2016/12/09,09:14:03.007,2016/12/09,09:14:06.584,,3400,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,4959,4CA8F6,17987,2016/12/09,09:14:03.008,2016/12/09,09:14:09.554,,3300,,,,,,5750,0,0,0,0");
        final Aircraft aircraft = filter(loadBaseStationRecording(bstFile).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 4959).get(0);
        // query unexisting date
        aircraft.query(501, LATITUDE);
    }

    @Ignore
    @Test
    public void testFunctions() throws Exception {
        final File fullFile = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe.sbs").getFile());
        final File partial_0m30s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-30s.sbs").getFile());
        final File partial_1m0s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-1m.sbs").getFile());
        final File partial_1m30s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-1m30s.sbs").getFile());
        final File partial_2m0s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-2m.sbs").getFile());
        final File partial_2m30s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-2m30s.sbs").getFile());
        final File partial_3m0s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-3m.sbs").getFile());
        final File partial_3m30s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-3m30s.sbs").getFile());
        final File partial_4m0s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-4m.sbs").getFile());
        final File partial_4m30s = new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-4m30s.sbs").getFile());
        final File jsFile = new File(fileSystem.getRoot(), "push.js");
        final Collection<File> files = newArrayList(
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-1m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-1m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-2m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-2m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-3m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-3m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-4m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-4m30s.sbs").getFile()));/*,
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-5m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-5m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-6m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-6m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-7m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-7m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-8m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-8m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-9m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-9m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-10m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-10m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-11m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-11m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-12m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-12m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-13m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-13m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-14m.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-14m30s.sbs").getFile()),
                new File(BstInterpolatorLoaderTest.class.getClassLoader().getResource("fdit/storage/recording/expe-15m.sbs").getFile()));*/

        final Aircraft fullAircraft = filter(loadBaseStationRecording(fullFile).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_0m30s = filter(loadBaseStationRecording(partial_0m30s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_1m0s = filter(loadBaseStationRecording(partial_1m0s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_1m30s = filter(loadBaseStationRecording(partial_1m30s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_2m0s = filter(loadBaseStationRecording(partial_2m0s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_2m30s = filter(loadBaseStationRecording(partial_2m30s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_3m0s = filter(loadBaseStationRecording(partial_3m0s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_3m30s = filter(loadBaseStationRecording(partial_3m30s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_4m0s = filter(loadBaseStationRecording(partial_4m0s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);
        final Aircraft partialAicraft_4m30s = filter(loadBaseStationRecording(partial_4m30s).getAircrafts(), filteredAircraft -> filteredAircraft.getAircraftId() == 2173).get(0);

        final StringBuilder builder = new StringBuilder();
        for (int i = (int) fullAircraft.getFirstCriterionAppearance(ALTITUDE); i < fullAircraft.getLastCriterionAppearance(ALTITUDE); i += 1000) {
            builder.append("data.push({ date: ")
                    .append(i)
                    .append(", value_full: ")
                    .append(fullAircraft.query(i, ALTITUDE))
                    .append(", value_0m30s: ")
                    .append(partialAicraft_0m30s.query(i, ALTITUDE))
                    .append(", value_1m0s: ")
                    .append(partialAicraft_1m0s.query(i, ALTITUDE))
                    .append(", value_1m30s: ")
                    .append(partialAicraft_1m30s.query(i, ALTITUDE))
                    .append(", value_2m0s: ")
                    .append(partialAicraft_2m0s.query(i, ALTITUDE))
                    .append(", value_2m30s: ")
                    .append(partialAicraft_2m30s.query(i, ALTITUDE))
                    .append(", value_3m0s: ")
                    .append(partialAicraft_3m0s.query(i, ALTITUDE))
                    .append(", value_3m30s: ")
                    .append(partialAicraft_3m30s.query(i, ALTITUDE))
                    .append(", value_4m0s: ")
                    .append(partialAicraft_4m0s.query(i, ALTITUDE))
                    .append(", value_4m30s: ")
                    .append(partialAicraft_4m30s.query(i, ALTITUDE))
                    .append(" });")
                    .append('\n');
        }
        write(builder.toString(), jsFile, UTF_8);
    }
}