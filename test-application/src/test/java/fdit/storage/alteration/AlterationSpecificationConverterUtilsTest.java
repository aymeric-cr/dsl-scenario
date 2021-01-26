package fdit.storage.alteration;

import fdit.gui.application.FditTestCase;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.AlterationSpecificationHelper;
import org.junit.Test;

import static com.google.inject.internal.util.$Lists.newArrayList;
import static fdit.metamodel.alteration.AlterationSpecificationHelper.*;
import static fdit.metamodel.alteration.action.Action.ActionType.ALTERATION;
import static fdit.metamodel.alteration.action.Action.ActionType.SATURATION;
import static fdit.metamodel.alteration.parameters.Characteristic.ICAO;
import static fdit.metamodel.alteration.parameters.Characteristic.SQUAWK;
import static fdit.storage.alteration.AlterationSpecificationConverterUtils.mergeAlterationSpecification;
import static fdit.testTools.PredicateAssert.assertThat;

public class AlterationSpecificationConverterUtilsTest extends FditTestCase {

    @Test
    public void mergeAlterationSpecificationTest() {
        final AlterationSpecification specification1 = createAlterationSpecification("specification1",
                actions(
                        createAction(type(ALTERATION),
                                name("alteration"),
                                description("Description"),
                                target("1111,3333"),
                                timeWindow(100, 200),
                                parameters(value(ICAO, "AF1234")))));
        final AlterationSpecification specification2 = createAlterationSpecification("specification2",
                actions(
                        createAction(type(SATURATION),
                                name("saturation"),
                                description("Description"),
                                target("1111,2222"),
                                timeWindow(200, 300),
                                aircraftNumber(10),
                                icaoParameter("RANDOM"))));
        final AlterationSpecification specification3 = createAlterationSpecification("specification3",
                actions(
                        createAction(type(ALTERATION),
                                name("alteration"),
                                description("Description"),
                                target("2222,3333"),
                                timeWindow(100, 400),
                                parameters(value(SQUAWK, "7700")))));
        assertThat(mergeAlterationSpecification(newArrayList(specification1, specification2, specification3)),
                anAlterationSpecification(anAlterationScenario("", "",
                        actions(
                                anAction(
                                        withActionName("alteration"),
                                        withType(ALTERATION),
                                        onTarget("1111,3333"),
                                        onTimeWindow(100, 200),
                                        withParameters(withValue(ICAO, "AF1234", false))),
                                anAction(
                                        withActionName("saturation"),
                                        withType(SATURATION),
                                        onTarget("1111,2222"),
                                        onTimeWindow(200, 300),
                                        withParameters(
                                                withValue(ICAO, "RANDOM", false),
                                                withAircraftNumber(10))),
                                anAction(
                                        withActionName("alteration"),
                                        withType(ALTERATION),
                                        onTarget("2222,3333"),
                                        onTimeWindow(100, 400),
                                        withParameters(withValue(SQUAWK, "7700", false)))))));
    }
}