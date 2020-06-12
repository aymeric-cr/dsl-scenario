package fdit.metamodel.rap;

import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.FditElementHelper.coordinates;
import static fdit.metamodel.aircraft.AircraftHelper.baseStationState;
import static fdit.metamodel.aircraft.AircraftHelper.loadedAircraft;
import static org.junit.Assert.assertEquals;

public class RecognizedAirPictureTest {

    @Test
    public void wholeRAP() throws Exception {
        final RecognizedAirPicture rap = new RecognizedAirPicture();
        rap.addAircrafts(newArrayList(
                loadedAircraft(1, 111111,
                        baseStationState(coordinates(1.1, 2.2), 1025, 101.1, 402.2, 300, 0),
                        baseStationState(coordinates(1.2, 2.1), 1050, 101.1, 402.2, 300, 500),
                        baseStationState(coordinates(1.3, 2.0), 1075, 101.1, 402.2, -300, 1000),
                        baseStationState(coordinates(1.4, 1.9), 1050, 101.1, 402.2, -300, 1500),
                        baseStationState(coordinates(1.5, 1.8), 1025, 101.1, 402.2, -300, 2000),
                        baseStationState(coordinates(1.6, 1.7), 1000, 101.1, 402.2, -300, 2500),
                        baseStationState(coordinates(1.7, 1.6), 975, 101.1, 402.2, -300, 3000)),
                loadedAircraft(2, 222222,
                        baseStationState(coordinates(24.1, -12.2), 10000, 50, 387.4, 0, 1000),
                        baseStationState(coordinates(24.2, -12.1), 10000, 52, 387.4, 0, 1500),
                        baseStationState(coordinates(24.3, -12.0), 10000, 54, 387.4, 0, 2000),
                        baseStationState(coordinates(24.4, -11.9), 10000, 54, 387.4, 0, 2500),
                        baseStationState(coordinates(24.5, -11.8), 10000, 54, 387.4, 0, 3000),
                        baseStationState(coordinates(24.6, -11.7), 10000, 52, 387.4, 0, 3500),
                        baseStationState(coordinates(24.7, -11.6), 10000, 50, 387.4, 0, 4000)),
                loadedAircraft(3, 333333,
                        baseStationState(coordinates(-11.1, 52.2), 35000, 252.3, 402.2, 1000, 0),
                        baseStationState(coordinates(-11.2, 52.1), 35500, 252.4, 390.1, 1050, 600),
                        baseStationState(coordinates(-11.3, 52.0), 36000, 252.5, 380.2, 1100, 1100),
                        baseStationState(coordinates(-11.4, 51.9), 36500, 252.6, 370.5, 1150, 1600),
                        baseStationState(coordinates(-11.5, 51.8), 37000, 252.7, 360.4, 1200, 2100),
                        baseStationState(coordinates(-11.6, 51.7), 37500, 252.8, 350.9, 1250, 2600),
                        baseStationState(coordinates(-11.7, 51.6), 38000, 252.9, 340.7, 1300, 3100))));
        assertEquals(3, rap.getAircrafts().size());
        assertEquals(4.8, rap.getMeanLatitude(), 0.001);
        assertEquals(13.96, rap.getMeanLongitude(), 0.01);
        assertEquals(15842.857, rap.getMeanAltitude(), 0.001);
        assertEquals(135.328, rap.getMeanTrack(), 0.001);
        assertEquals(386.771, rap.getMeanGroundSpeed(), 0.001);
        assertEquals(340.476, rap.getMeanVerticalRate(), 0.001);
    }
}