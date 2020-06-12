package fdit.storage.scenario;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.recording.Recording;
import fdit.testTools.Saver;
import org.junit.Test;

import java.io.File;

import static fdit.metamodel.FditElementHelper.*;
import static fdit.metamodel.aircraft.AircraftHelper.baseStationState;
import static fdit.metamodel.aircraft.AircraftHelper.loadedAircraft;
import static fdit.metamodel.element.DirectoryUtils.gatherAllRecordings;
import static fdit.metamodel.scenario.ScenarioHelper.*;
import static fdit.storage.scenario.ScenarioStorage.loadScenario;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.FilePredicate.aFile;
import static fdit.testTools.predicate.FilePredicate.containsAllXML;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public class ScenarioStorageTest extends FditTestCase {

    @Test
    public void testScenarioStorage_withoutBstAssociated() throws Exception {
        root(scenario("scenario", content("alter all\nplanes"), description("")));
        final File scenarioFile = findFile("scenario.scenario");
        assertThat(scenarioFile,
                aFile("scenario.scenario",
                        containsAllXML("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
                                "<properties>\n" +
                                "<entry key=\"content\">alter all planes</entry>\n" +
                                "<entry key=\"description\"/>\n" +
                                "</properties>")));
        assertThat(loadScenario(findFile("scenario.scenario"), getRootFile(), EMPTY_LIST),
                aScenario("scenario", "alter all\nplanes", ""));
    }

    @Test
    public void testScenarioStorage_withBstAssociated() throws Exception {
        final Saver<Recording> recordingSaver = Saver.create();
        root(
                folder("folder",
                        bstRecording("recording", recordingSaver, 60000,
                                loadedAircraft(145, 0x7404F2,
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 0),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 10000),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 20000),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 30000),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 40000),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 50000),
                                        baseStationState(coordinates(48.914902, 1.432612), 0, 60000)))),
                scenario("scenario",
                        content("alter all\nplanes"),
                        description(""),
                        withRecording(recordingSaver)));
        final File scenarioFile = findFile("scenario.scenario");
        assertThat(scenarioFile,
                aFile("scenario.scenario",
                        containsAllXML("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
                                "<properties>\n" +
                                "<entry key=\"content\">alter all planes</entry>\n" +
                                "<entry key=\"bstPath\">" + separatorsToUnix("folder/recording.bst") + "</entry>\n" +
                                "<entry key=\"description\"/>\n" +
                                "</properties>")));

        assertThat(loadScenario(findFile("scenario.scenario"),
                getRootFile(),
                gatherAllRecordings(getRoot())),
                aScenario("scenario", "alter all\nplanes", "", withRecording(aRecording(recordingSaver))));
    }
}