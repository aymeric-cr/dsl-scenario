package fdit.metamodel.aircraft;

import org.junit.Test;

import static fdit.metamodel.FditElementHelper.coordinates;
import static fdit.metamodel.aircraft.AircraftHelper.baseStationState;
import static fdit.metamodel.aircraft.AircraftHelper.loadedAircraft;
import static org.junit.Assert.assertEquals;

public class StaticPropertiesTest {

    @Test
    public void staticPropertiesMinMax() throws Exception {
        final StaticProperties properties1 = loadedAircraft(1, 111111,
                baseStationState(coordinates(1.1, 2.2), 1025, 101.1, 402.2, 300, 0),
                baseStationState(coordinates(1.2, 2.1), 1050, 101.1, 402.2, 300, 500),
                baseStationState(coordinates(1.3, 2.0), 1075, 101.1, 402.2, -300, 1000),
                baseStationState(coordinates(1.4, 1.9), 1050, 101.1, 402.2, -300, 1500),
                baseStationState(coordinates(1.5, 1.8), 1025, 101.1, 402.2, -300, 2000),
                baseStationState(coordinates(1.6, 1.7), 1000, 101.1, 402.2, -300, 2500),
                baseStationState(coordinates(1.7, 1.6), 975, 101.1, 402.2, -300, 3000)).getStaticProperties();
        assertEquals(1.1, properties1.getMinLatitude(), 0.1);
        assertEquals(1.7, properties1.getMaxLatitude(), 0.1);
        assertEquals(1.6, properties1.getMinLongitude(), 0.1);
        assertEquals(2.2, properties1.getMaxLongitude(), 0.1);
        assertEquals(975, properties1.getMinAltitude(), 0.1);
        assertEquals(1075, properties1.getMaxAltitude(), 0.1);
        assertEquals(707.7, properties1.getSumTracks(), 0.1);
        assertEquals(402.2, properties1.getMaxGroundSpeed(), 0.1);
        assertEquals(402.2, properties1.getMinGroundSpeed(), 0.1);
        assertEquals(-300, properties1.getMinVerticalRate(), 0.1);
        assertEquals(300, properties1.getMaxVerticalRate(), 0.1);
    }
}