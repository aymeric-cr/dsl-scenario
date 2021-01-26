package fdit.triggcondition.tests

import com.google.inject.Inject
import fdit.metamodel.aircraft.Aircraft
import fdit.metamodel.aircraft.AircraftCriterion
import fdit.metamodel.aircraft.TimeInterval
import fdit.metamodel.element.Directory
import fdit.metamodel.rap.RecognizedAirPicture
import fdit.metamodel.recording.Recording
import fdit.triggcondition.triggeringCondition.Model
import fdit.triggcondition.interpreter.TriggeringConditionInterpreter
import java.util.HashMap
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import fdit.metamodel.aircraft.TimeInterval.IntervalType

import static com.google.common.collect.Lists.newArrayList
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import fdit.metamodel.aircraft.StaticProperties

@RunWith(XtextRunner)
@InjectWith(TriggeringConditionInjectorProvider)
class TriggeringConditionInterpretingTest {

    @Inject extension ParseHelper<Model>
    @Inject extension ValidationTestHelper
    @Inject extension TriggeringConditionInterpreter

    @Test
    def void when_rap_minimal() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(ac2.query(0,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(34.8967)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(2000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L, IntervalType.FALSE), new TimeInterval(1000L,2000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L, IntervalType.FALSE), new TimeInterval(1000L,2000L,IntervalType.TRUE)))
        "eval when (RAP.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void when_aircraft_minimal() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,2000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L)))
        "eval when (AIRCRAFT.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void when_aircraft_minimal_arithm() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(2000L)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(2000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(2000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,2000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L,IntervalType.TRUE)))
        "eval when (AIRCRAFT.ALTITUDE > (30000 - 8000 * 2))".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_aircraft_minimal() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14030D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13998D)
        when(ac3.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(13991D)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        expected.put(ac3,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        "eval as_soon_as (AIRCRAFT.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_aircraft_minimal_2() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14020D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14030D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14002D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(13998D)
        when(ac3.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(13991D)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        expected.put(ac3,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        "eval as_soon_as (AIRCRAFT.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_aircraft_minimal_3() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(13999D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14030D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        "eval as_soon_as (AIRCRAFT.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_rap_minimal() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval as_soon_as (RAP.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)

    }

    @Test
    def void asap_rap_minimal_2() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        "eval as_soon_as (RAP.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_rap_minimal_3() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(20000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        "eval as_soon_as (RAP.ALTITUDE > 14000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_aircraft_minimal_static() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.icao).thenReturn(4321) // in Hex string == 10e1
        when(ac2.icao).thenReturn(1234)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        "eval as_soon_as (AIRCRAFT.ICAO == \"10e1\")".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_aircraft_minimal_static_2() {
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.icao).thenReturn(4321)
        var ac2 = mock(Aircraft)
        when(ac2.icao).thenReturn(1235)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        "eval as_soon_as (AIRCRAFT.ICAO == \"10e2\")".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void asap_rap_minimal_static() { // TEST CHELOU
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.icao).thenReturn(4321)
        var ac2 = mock(Aircraft)
        when(ac2.icao).thenReturn(4321)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        "eval as_soon_as (RAP.ICAO == \"10e1\")".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void notwhen_aircraft_minimal() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.LATITUDE)).thenReturn(42.1657)
        when(ac1.query(1000,AircraftCriterion.LATITUDE)).thenReturn(42.1696)
        when(ac1.query(2000,AircraftCriterion.LATITUDE)).thenReturn(42.0696)
        when(ac1.query(3000,AircraftCriterion.LATITUDE)).thenReturn(42.1658)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.LATITUDE)).thenReturn(42.2657)
        when(ac2.query(1000,AircraftCriterion.LATITUDE)).thenReturn(41.1987)
        when(ac2.query(2000,AircraftCriterion.LATITUDE)).thenReturn(43.1093)
        when(ac2.query(3000,AircraftCriterion.LATITUDE)).thenReturn(42.1)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L,IntervalType.TRUE),new TimeInterval(2000L,3000L,IntervalType.FALSE)))
        "eval not_when (AIRCRAFT.LATITUDE > 42.1657)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void notwhen_aircraft_minimal_2() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(6.15942)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(4D)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac1.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(7.1)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(12D)
        when(ac2.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval not_when (AIRCRAFT.LONGITUDE <= 6.85942)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void notwhen_aircraft_medium() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(6.15942)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34D)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac1.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(7.1)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(12D)
        when(ac2.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval not_when (AIRCRAFT.LONGITUDE <= 6.85942)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void notwhen_aircraft_medium_arithm() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(6.15942)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34D)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac1.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(7.1)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(12D)
        when(ac2.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval not_when (AIRCRAFT.LONGITUDE <= 5.85942 + 1)".assertAlterationIntervals(rap,expected,recording)
    }


    @Test
    def void notwhen_aircraft_medium_arithm2() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        when(rap.maxLongitude).thenReturn(5.85942)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(6.15942)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34D)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac1.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)

        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(7.1)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(12D)
        when(ac2.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval not_when (AIRCRAFT.LONGITUDE <= RAP.MAX_LONGITUDE + 1)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void notwhen_aircraft_medium_arithm3() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac1_st = mock(StaticProperties)
        when(ac1.staticProperties).thenReturn(ac1_st)
        var ac2 = mock(Aircraft)
        var ac2_st = mock(StaticProperties)
        when(ac2.staticProperties).thenReturn(ac2_st)
        when(rap.maxLongitude).thenReturn(5.85942)
        when(ac1.query(0,AircraftCriterion.LONGITUDE)).thenReturn(6.15942)
        when(ac1.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(34D)
        when(ac1.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac1.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1_st.minLongitude).thenReturn(6.15942)

        when(ac2.query(0,AircraftCriterion.LONGITUDE)).thenReturn(7.1)
        when(ac2.query(1000,AircraftCriterion.LONGITUDE)).thenReturn(6.85942)
        when(ac2.query(2000,AircraftCriterion.LONGITUDE)).thenReturn(12D)
        when(ac2.query(3000,AircraftCriterion.LONGITUDE)).thenReturn(6.85943)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)
        when(ac2_st.minLongitude).thenReturn(6.85942)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,3000L,IntervalType.TRUE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,2000L,IntervalType.FALSE),new TimeInterval(2000L,3000L,IntervalType.TRUE)))
        "eval when (AIRCRAFT.LONGITUDE > AIRCRAFT.MIN_LONGITUDE)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void until_aircraft_simple() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(11999D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(5000D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(3000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(20000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(3000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(2000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(11000D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.timeOfLastAppearance).thenReturn(3000L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(3000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(3000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.TRUE),new TimeInterval(1000L,3000L,IntervalType.FALSE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.FALSE)))
        expected.put(ac3,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE)))
        "eval until (AIRCRAFT.ALTITUDE > 12000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void until_rap_simple() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(5000D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(8000D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(4000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(14005D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(13000D)
        when(ac2.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(4000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(4000D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac3.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac3.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.timeOfLastAppearance).thenReturn(4000L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        when(rap.relativeDuration).thenReturn(4000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE),new TimeInterval(3000L,4000L,IntervalType.FALSE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE),new TimeInterval(3000L,4000L,IntervalType.FALSE)))
        expected.put(ac3,newArrayList(new TimeInterval(0L,3000L,IntervalType.TRUE),new TimeInterval(3000L,4000L,IntervalType.FALSE)))
        "eval until (RAP.ALTITUDE > 12000)".assertAlterationIntervals(rap,expected,recording)
    }

    @Test
    def void when_rap_aircraft_double() {
        setBeaconInterval(1000)
        var recording = mock(Recording)
        var rap = mock(RecognizedAirPicture)
        var ac1 = mock(Aircraft)
        var ac2 = mock(Aircraft)
        var ac3 = mock(Aircraft)
        var ac4 = mock(Aircraft)
        when(ac1.query(0,AircraftCriterion.ALTITUDE)).thenReturn(5000D)
        when(ac1.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(16001D)
        when(ac1.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac1.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac1.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(14001D)
        when(ac1.timeOfFirstAppearance).thenReturn(0L)
        when(ac1.timeOfLastAppearance).thenReturn(4000L)
        when(ac1.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac1.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(ac2.query(0,AircraftCriterion.ALTITUDE)).thenReturn(3000D)
        when(ac2.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(15000D)
        when(ac2.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15005D)
        when(ac2.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(13000D)
        when(ac2.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac2.timeOfFirstAppearance).thenReturn(0L)
        when(ac2.timeOfLastAppearance).thenReturn(4000L)
        when(ac2.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac2.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(ac3.query(0,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(16000D)
        when(ac3.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac3.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(12000D)
        when(ac3.timeOfFirstAppearance).thenReturn(0L)
        when(ac3.timeOfLastAppearance).thenReturn(4000L)
        when(ac3.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac3.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(ac4.query(0,AircraftCriterion.ALTITUDE)).thenReturn(12001D)
        when(ac4.query(1000,AircraftCriterion.ALTITUDE)).thenReturn(16000D)
        when(ac4.query(2000,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac4.query(3000,AircraftCriterion.ALTITUDE)).thenReturn(15001D)
        when(ac4.query(4000,AircraftCriterion.ALTITUDE)).thenReturn(13000D)
        when(ac4.timeOfFirstAppearance).thenReturn(0L)
        when(ac4.timeOfLastAppearance).thenReturn(4000L)
        when(ac4.getFirstCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(0D)
        when(ac4.getLastCriterionAppearance(AircraftCriterion.ALTITUDE)).thenReturn(4000D)

        when(rap.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        when(rap.relativeDuration).thenReturn(4000L)
        when(recording.aircrafts).thenReturn(newArrayList(ac1,ac2,ac3,ac4))
        var HashMap<Aircraft, List<TimeInterval>> expected = new HashMap<Aircraft,List<TimeInterval>>()
        expected.put(ac1,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L,IntervalType.TRUE),new TimeInterval(2000L,4000L,IntervalType.FALSE)))
        expected.put(ac2,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L,IntervalType.TRUE),new TimeInterval(2000L,3000L,IntervalType.FALSE),new TimeInterval(3000L,4000L,IntervalType.TRUE)))
        expected.put(ac3,newArrayList(new TimeInterval(0L,1000L,IntervalType.FALSE),new TimeInterval(1000L,2000L,IntervalType.TRUE),new TimeInterval(2000L,4000L,IntervalType.FALSE)))
        expected.put(ac4,newArrayList(new TimeInterval(0L,2000L,IntervalType.TRUE),new TimeInterval(2000L,3000L,IntervalType.FALSE),new TimeInterval(3000L,4000L,IntervalType.TRUE)))
        "eval when (AIRCRAFT.ALTITUDE > 12000) and not_when (RAP.ALTITUDE > 15000)".assertAlterationIntervals(rap,expected,recording)
    }

    def assertAlterationIntervals(CharSequence input, RecognizedAirPicture rap, HashMap<Aircraft, List<TimeInterval>> expected, Recording recording) {
        input.parse => [
            assertNoErrors
            var HashMap<Aircraft, List<TimeInterval>> result =expression.getAlterationIntervals(rap, new Directory("root"), recording)
            assertEquals(expected,result)
        ]
    }
}