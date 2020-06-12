package fdit.storage.recording;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static com.google.common.io.Files.createTempDir;
import static com.google.common.io.Files.write;
import static fdit.storage.recording.RecordingStorage.createSubRecordingFromAircrafts;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.containsAll;
import static fdit.tools.stream.StreamUtils.filter;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RecordingStorageTest extends FditTestCase {

    @Test
    public void test_createSubSbsFromSelectedAircrafts() throws Exception {
        final Saver<Recording> recordingSaver = create();
        final File sbsFile = new File(createTempDir(), "messages.sbs");
        write("MSG,2,1,1000,0A0075,18009,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,,0,27.0,264.4,48.72313,2.37821,,,,,,-1\n" +
                        "MSG,1,1,2000,4CA8F0,17987,2016/12/09,09:14:03.050,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,1000,4CA8F6,17987,2016/12/09,09:14:03.100,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,1,1,3000,0A0075,18009,2016/12/09,09:14:03.200,2016/12/09,09:14:05.486,RJA1118,,,,,,,,,,,\n" +
                        "MSG,3,1,1000,4CA8F6,17987,2016/12/09,09:14:03.300,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,3000,4CA8F1,17988,2016/12/09,09:14:03.400,2016/12/09,09:14:09.554,,2800,,,,,,5750,0,0,0,0\n" +
                        "MSG,7,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,32000,,,,,,,,,,0\n" +
                        "MSG,2,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,0,27.0,,48.76740,2.62024,,,,,,,0\n" +
                        "MSG,1,1,1000,4CA8F6,17987,2016/12/09,09:14:04.600,2016/12/09,09:14:11.568,RJA0000,,,,,,,,,,,\n" +
                        "MSG,4,1,5000,AE11D3,18018,2016/12/09,09:14:04.700,2016/12/09,09:14:12.568,,,288.6,103.2,,,-832,,,,,\n" +
                        "MSG,6,1,1000,4CA8F6,17987,2016/12/09,09:14:04.800,2016/12/09,09:14:13.568,,33325,,,,,,0271,0,0,0,0\n" +
                        "MSG,8,1,5000,AE11D3,18018,2016/12/09,09:14:04.900,2016/12/09,09:14:14.568,,,,,,,,,,,,0\n" +
                        "MSG,2,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:14.568,,0,27.0,,48.76740,2.62024,,,,,,,0",
                sbsFile,
                UTF_8);
        root(sbsRecording(recordingSaver, sbsFile,
                new Aircraft(1000, 655477),
                new Aircraft(2000, 5023984),
                new Aircraft(3000, 655477),
                new Aircraft(5000, 11407827)));
        final Collection<Aircraft> selectedAircrafts = filter(recordingSaver.get().getAircrafts(), aircraft ->
                aircraft.getAircraftId() == 1000 || aircraft.getAircraftId() == 5000);

        createSubRecordingFromAircrafts(selectedAircrafts, recordingSaver.get(), getRootFile(), recordingLoadingCallback());
        assertThat(findFile("messages_extract0.sbs"),
                aFile("messages_extract0.sbs",
                        containsAll(
                                "MSG,2,1,1000,0A0075,18009,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,,0,27.0,264.4,48.72313,2.37821,,,,,,-1\n",
                                "MSG,5,1,1000,4CA8F6,17987,2016/12/09,09:14:03.100,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n",
                                "MSG,3,1,1000,4CA8F6,17987,2016/12/09,09:14:03.300,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n",
                                "MSG,7,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,32000,,,,,,,,,,0\n",
                                "MSG,1,1,1000,4CA8F6,17987,2016/12/09,09:14:04.600,2016/12/09,09:14:11.568,RJA0000,,,,,,,,,,,\n",
                                "MSG,4,1,5000,AE11D3,18018,2016/12/09,09:14:04.700,2016/12/09,09:14:12.568,,,288.6,103.2,,,-832,,,,,\n",
                                "MSG,6,1,1000,4CA8F6,17987,2016/12/09,09:14:04.800,2016/12/09,09:14:13.568,,33325,,,,,,0271,0,0,0,0\n",
                                "MSG,8,1,5000,AE11D3,18018,2016/12/09,09:14:04.900,2016/12/09,09:14:14.568,,,,,,,,,,,,0")));
    }

    @Test
    public void test_createSubBstFromSelectedAircrafts() throws Exception {
        final Saver<Recording> recordingSaver = create();
        final File bstFile = new File(createTempDir(), "messages.bst");
        write("MSG,2,1,1000,0A0075,18009,2016/12/09,09:14:03.000,2016/12/09,09:14:04.484,,0,27.0,264.4,48.72313,2.37821,,,,,,-1\n" +
                        "MSG,1,1,2000,4CA8F0,17987,2016/12/09,09:14:03.050,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                        "MSG,5,1,1000,4CA8F6,17987,2016/12/09,09:14:03.100,2016/12/09,09:14:04.486,,3375,,,,,,,0,,0,0\n" +
                        "MSG,1,1,3000,0A0075,18009,2016/12/09,09:14:03.200,2016/12/09,09:14:05.486,RJA1118,,,,,,,,,,,\n" +
                        "MSG,3,1,1000,4CA8F6,17987,2016/12/09,09:14:03.300,2016/12/09,09:14:06.484,,2900,,,48.76740,2.62024,,,0,0,0,0\n" +
                        "MSG,6,1,3000,4CA8F1,17988,2016/12/09,09:14:03.400,2016/12/09,09:14:09.554,,2800,,,,,,5750,0,0,0,0\n" +
                        "MSG,7,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,32000,,,,,,,,,,0\n" +
                        "MSG,2,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:10.568,,0,27.0,,48.76740,2.62024,,,,,,,0\n" +
                        "MSG,1,1,1000,4CA8F6,17987,2016/12/09,09:14:04.600,2016/12/09,09:14:11.568,RJA0000,,,,,,,,,,,\n" +
                        "MSG,4,1,5000,AE11D3,18018,2016/12/09,09:14:04.700,2016/12/09,09:14:12.568,,,288.6,103.2,,,-832,,,,,\n" +
                        "MSG,6,1,1000,4CA8F6,17987,2016/12/09,09:14:04.800,2016/12/09,09:14:13.568,,33325,,,,,,0271,0,0,0,0\n" +
                        "MSG,8,1,5000,AE11D3,18018,2016/12/09,09:14:04.900,2016/12/09,09:14:14.568,,,,,,,,,,,,0\n" +
                        "MSG,2,1,5000,AE11D3,18018,2016/12/09,09:14:04.500,2016/12/09,09:14:14.568,,0,27.0,,48.76740,2.62024,,,,,,,0",
                bstFile,
                UTF_8);
        root(bstRecording(recordingSaver, bstFile,
                new Aircraft(1000, 655477),
                new Aircraft(2000, 5023984),
                new Aircraft(3000, 655477),
                new Aircraft(5000, 11407827)));

        final Collection<Aircraft> selectedAircrafts = filter(recordingSaver.get().getAircrafts(), aircraft ->
                aircraft.getAircraftId() == 3000 || aircraft.getAircraftId() == 2000);

        createSubRecordingFromAircrafts(selectedAircrafts, recordingSaver.get(), getRootFile(), recordingLoadingCallback());
        assertThat(findFile("messages_extract0.bst"),
                aFile("messages_extract0.bst",
                        containsAll("MSG,1,1,2000,4CA8F0,17987,2016/12/09,09:14:03.050,2016/12/09,09:14:04.484,CS1501,,,,,,,,,,,\n" +
                                "MSG,1,1,3000,0A0075,18009,2016/12/09,09:14:03.200,2016/12/09,09:14:05.486,RJA1118,,,,,,,,,,,\n" +
                                "MSG,6,1,3000,4CA8F1,17988,2016/12/09,09:14:03.400,2016/12/09,09:14:09.554,,2800,,,,,,5750,0,0,0,0")));
    }
}