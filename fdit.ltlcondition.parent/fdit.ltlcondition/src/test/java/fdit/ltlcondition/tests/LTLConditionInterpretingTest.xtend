package fdit.ltlcondition.tests

import com.google.inject.Inject
import fdit.ltlcondition.interpreter.LTLConditionInterpreter
import fdit.ltlcondition.lTLCondition.Model
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.aircraft.AircraftCriterion
import fdit.metamodel.aircraft.StaticProperties
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.recording.Recording
import java.util.List
import org.apache.commons.collections4.CollectionUtils
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static com.google.common.collect.Lists.newArrayList
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(XtextRunner)
@InjectWith(LTLConditionInjectorProvider)
class LTLConditionInterpretingTest {

    @Inject extension ParseHelper<Model>
    @Inject extension ValidationTestHelper
    @Inject extension LTLConditionInterpreter

    @Before
    def void setup() {
        TIME_INTERVAL = 1000
    }

    @Test
    def void F_altitude_minimal () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval F (ALTITUDE > 14000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_minimal () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval G (ALTITUDE > 14000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void static_max_altitude_minimal () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var sp1 = mock(StaticProperties)
        var sp2 = mock(StaticProperties)
        var sp3 = mock(StaticProperties)

        when(ac1.getStaticProperties).thenReturn(sp1)
        when(sp1.maxAltitude).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.getStaticProperties).thenReturn(sp2)
        when(sp2.maxAltitude).thenReturn(14000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.getStaticProperties).thenReturn(sp3)
        when(sp3.maxAltitude).thenReturn(14005D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval MAX_ALTITUDE > 14005".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_minimal_arithm () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval G (ALTITUDE > 1400 * (15 - 5))".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_minimal_arithm_RAP () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(rap.meanAltitude).thenReturn(13999D)

        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval G (ALTITUDE > 1 + (RAP.MEAN_ALTITUDE))".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_F_altitude_and () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15020D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.add(ac2)
        "eval G (ALTITUDE > 14000) and F(ALTITUDE > 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void F_altitude_Plus_arithm () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac1_st = mock(StaticProperties)
        var ac2 = mock(Aircraft)
        var ac2_st = mock(StaticProperties)
        var ac3 = mock(Aircraft)
        var ac3_st = mock(StaticProperties)
        var ac4 = mock(Aircraft)
        var ac4_st = mock(StaticProperties)

        when(ac1.staticProperties).thenReturn(ac1_st)
        when(ac2.staticProperties).thenReturn(ac2_st)
        when(ac3.staticProperties).thenReturn(ac3_st)
        when(ac4.staticProperties).thenReturn(ac4_st)

        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac1_st.meanAltitude).thenReturn(14009D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac2_st.meanAltitude).thenReturn(14335D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3_st.meanAltitude).thenReturn(14335D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13920D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac4_st.meanAltitude).thenReturn(13975D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac2,ac3)
        "eval F(ALTITUDE > AIRCRAFT.MEAN_ALTITUDE + 500)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_F_altitude_and_arithm () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac1_st = mock(StaticProperties)
        var ac2 = mock(Aircraft)
        var ac2_st = mock(StaticProperties)
        var ac3 = mock(Aircraft)
        var ac3_st = mock(StaticProperties)
        var ac4 = mock(Aircraft)
        var ac4_st = mock(StaticProperties)

        when(ac1.staticProperties).thenReturn(ac1_st)
        when(ac2.staticProperties).thenReturn(ac2_st)
        when(ac3.staticProperties).thenReturn(ac3_st)
        when(ac4.staticProperties).thenReturn(ac4_st)

        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac1_st.minAltitude).thenReturn(14001D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac2_st.minAltitude).thenReturn(14001D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3_st.minAltitude).thenReturn(14000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15020D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac4_st.minAltitude).thenReturn(13005D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.add(ac2)
        "eval G (ALTITUDE > 14000) and F(ALTITUDE > AIRCRAFT.MIN_ALTITUDE + 20)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_F_altitude_and_leftArithm () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15020D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.add(ac2)
        "eval G (ALTITUDE > (6 * 2 - 10) + 13998) and F(ALTITUDE > 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_F_altitude_or () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15020D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac1,ac2,ac4)
        "eval G (ALTITUDE > 14000) or F(ALTITUDE > 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void F_altitude_groundspeed_and () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac1.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac1.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac2.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac3.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(340D)
        when(ac3.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(341D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13020D)
        when(ac4.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac4.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(341D)
        when(ac4.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(345D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.add(ac1)
        "eval F(ALTITUDE > 14000  and GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
        expected.addAll(ac2,ac3)
        "eval F(ALTITUDE > 14000) and F(GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
    }

    @Test
    def void F_altitude_and () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15014D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13020D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac1,ac2)
        "eval F(ALTITUDE > 14000  and ALTITUDE < 15000)".assertFilter(rap,expected,recording)
        expected.add(ac3)
        "eval F(ALTITUDE > 14000) and F(ALTITUDE < 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_or () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac1,ac2,ac4)
        "eval G(ALTITUDE < 14000 or ALTITUDE > 15000)".assertFilter(rap,expected,recording)
        expected.remove(ac1)
        "eval G(ALTITUDE < 14000) or G(ALTITUDE > 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_and () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14900D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(15000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14978D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14023D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14999D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14500D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac1,ac4)
        "eval G(ALTITUDE > 14000  and ALTITUDE < 15000)".assertFilter(rap,expected,recording)
        "eval G(ALTITUDE > 14000) and G(ALTITUDE < 15000)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_groundspeed_or () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac1.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac1.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15002D)
        when(ac2.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac2.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac3.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(344D)
        when(ac3.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(341D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac4.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac4.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(340D)
        when(ac4.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac2,ac3)
        "eval G(ALTITUDE > 14000) or G(GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
        expected.add(ac1)
        "eval G(ALTITUDE > 14000  or GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
    }

    @Test
    def void G_altitude_groundspeed_or_not () {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13020D)
        when(ac1.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac1.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac1.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13002D)
        when(ac2.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac2.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(325D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(13005D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac3.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(344D)
        when(ac3.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(341D)
        when(ac3.timeOfLastAppearance).thenReturn(2000L)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13020D)
        when(ac4.query(0,AircraftCriterion.GROUNDSPEED)).thenReturn(350D)
        when(ac4.query(1000,AircraftCriterion.GROUNDSPEED)).thenReturn(340D)
        when(ac4.query(2000,AircraftCriterion.GROUNDSPEED)).thenReturn(339D)
        when(ac4.timeOfLastAppearance).thenReturn(2000L)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.GROUNDSPEED)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var List<Aircraft> expected = newArrayList
        expected.addAll(ac2,ac3)
        "eval G(not(ALTITUDE > 14000)) or G(GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
        expected.add(ac1)
        "eval G(not(ALTITUDE > 14000)  or GROUNDSPEED > 340)".assertFilter(rap,expected,recording)
        expected.remove(ac3)
        expected.add(ac4)
        "eval not(G(ALTITUDE > 14000  or GROUNDSPEED > 340))".assertFilter(rap,expected,recording)
    }

    def assertFilter(CharSequence input, RecognizedAirPicture rap, List<Aircraft> expected, Recording recording) {
        input.parse => [
            assertNoErrors
            var List<Aircraft> result = expression.interpret(rap, recording)
            assertTrue(CollectionUtils.isEqualCollection(result,expected))
            //assertThat(result,containsInAnyOrder(expected.toArray))
            //assertThat(expected,containsInAnyOrder(result.toArray))
        ]
    }

}