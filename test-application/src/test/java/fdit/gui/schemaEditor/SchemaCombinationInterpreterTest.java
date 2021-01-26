package fdit.gui.schemaEditor;

import fdit.dsl.ide.AttackScenarioFacade;
import fdit.gui.application.FditTestCase;
import fdit.gui.schemaEditor.schemaInterpretation.SchemaInterpreter;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.testTools.Saver;
import org.junit.Test;

import java.util.List;

import static fdit.metamodel.FditElementHelper.aSchema;
import static fdit.metamodel.element.DirectoryUtils.findSchema;
import static fdit.metamodel.schema.SchemaHelper.content;
import static fdit.metamodel.schema.SchemaHelper.description;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.Saver.create;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;

public class SchemaCombinationInterpreterTest extends FditTestCase {

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
                schema("scenario",
                        recording,
                        content(scenarioContent),
                        description("")));
        final SchemaInterpreter interpreter = new SchemaInterpreter(new AttackScenarioFacade());
        final Schema schema = findSchema("scenario", getRoot()).get();
        final List<Schema> scenarios = interpreter.interpreteAbstractScenario(schema);
        assertThat(scenarios, containsOnly(
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU25\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU70\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 100.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 102.2", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 202.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"39AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 204.0", ""),
                aSchema("scenario",
                        "alter all_planes at 0 seconds\n" +
                                "with_values ICAO = \"38AC47\"\n" +
                                "and CALLSIGN = \"SAMU90\"\n" +
                                "and GROUNDSPEED = 204.0", "")));
    }
}