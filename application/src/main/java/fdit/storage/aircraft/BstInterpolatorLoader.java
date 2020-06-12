package fdit.storage.aircraft;

import fdit.metamodel.aircraft.AircraftCriterion;
import fdit.metamodel.aircraft.AircraftCriterion.AircraftCriterionSwitch;
import fdit.metamodel.aircraft.InterpolatorContentLoaderResult;

import java.io.File;

public class BstInterpolatorLoader extends RecordingInterpolatorLoader {

    public BstInterpolatorLoader(File recording, int aircraftId) {
        super(recording, aircraftId);
    }

    public InterpolatorContentLoaderResult loadInterpolatorContent() throws Exception {
        return loadFromInDatabase();
    }

    @Override
    public String getAircraftCriterionField(final AircraftCriterion criterion) {
        return new AircraftCriterionSwitch<String>() {
            @Override
            public String visitAltitude() {
                return "altitude";
            }

            @Override
            public String visitEmergency() {
                return "emergency";
            }

            @Override
            public String visitGroundSpeed() {
                return "ground_speed";
            }

            @Override
            public String visitLatitude() {
                return "latitude";
            }

            @Override
            public String visitLongitude() {
                return "longitude";
            }

            @Override
            public String visitSpi() {
                return "spi";
            }

            @Override
            public String visitSquawk() {
                return "squawk";
            }

            @Override
            public String visitTrack() {
                return "track";
            }

            @Override
            public String visitAlert() {
                return "alert";
            }

            @Override
            public String visitIsOnGround() {
                return "is_on_ground";
            }

            @Override
            public String visitVerticalRate() {
                return "vertical_rate";
            }

            @Override
            public String visitDefault() {
                return "";
            }
        }.doSwitch(criterion);
    }

    @Override
    public String getAircraftStatesTableName() {
        return "SBS_AIRCRAFT_STATES";
    }
}