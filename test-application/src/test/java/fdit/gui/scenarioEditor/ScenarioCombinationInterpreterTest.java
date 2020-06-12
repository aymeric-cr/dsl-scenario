package fdit.gui.scenarioEditor;

import fdit.dsl.xtext.standalone.AttackScenarioDslFacade;
import fdit.gui.application.FditTestCase;
import fdit.gui.scenarioEditor.scenarioInterpretation.ScenarioInterpreter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.scenario.ScenarioHelper;
import fdit.metamodel.scenario.Scenario;
import fdit.testTools.Saver;
import org.junit.Test;

import java.util.List;

import static fdit.metamodel.FditElementHelper.aScenario;
import static fdit.metamodel.element.DirectoryUtils.findScenario;
import static fdit.metamodel.scenario.ScenarioHelper.content;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;

public class ScenarioCombinationInterpreterTest extends FditTestCase {

    @Test
    public void testCombination() {
        final Saver<Recording> recording = create();
        final String scenarioContent =
                "let $icaos = {\"39AC47\", \"38AC47\" },\n" +
                        "let $callsigns = { \"SAMU25\", \"SAMU70\", \"SAMU90\" },\n" +
                        "let $groundspeeds = [ 101.2, 203.0 ],\n" +
                        "alter all_planes at 0 seconds\n" +
                        "with_values ICAO = $icaos\n" +
                        "and CALLSIGN = $callsigns\n" +
                        "and GROUNDSPEED = $groundspeeds";
        root(
                bstRecording("recording", recording, 79600),
                scenario("scenario",
                        recording,
                        content(scenarioContent),
                        ScenarioHelper.description("")));
        final ScenarioInterpreter interpreter = new ScenarioInterpreter(new AttackScenarioDslFacade());
        final Scenario scenario = findScenario("scenario", getRoot()).get();
        final List<Scenario> scenarios = interpreter.interpreteAbstractScenario(scenario);
        assertThat(scenarios, containsOnly(
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aScenario("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 204.0", "")));
    }
}