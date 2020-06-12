package fdit.storage.alteration;

import fdit.metamodel.alteration.action.Action;
import fdit.metamodel.alteration.action.Action.ActionTypeSwitch;
import fdit.metamodel.alteration.parameters.Characteristic;
import fdit.metamodel.alteration.parameters.Characteristic.CharacteristicSwitch;

@SuppressWarnings("ConstantNamingConvention")
public final class AlterationSpecificationStorage {

    public static final String SPECIFICATION = "specification";
    public static final String SCENARIO = "scenario";
    public static final String SCENARIO_NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ACTION = "action";
    public static final String ACTION_TYPE = "type";
    public static final String ACTION_NAME = "name";
    public static final String ACTION_TYPE_ALTERATION = "alteration";
    public static final String ACTION_TYPE_REPLAY = "replay";
    public static final String ACTION_TYPE_CREATION = "creation";
    public static final String ACTION_TYPE_DELETION = "deletion";
    public static final String ACTION_TYPE_SATURATION = "saturation";
    public static final String ACTION_TYPE_TIMESTAMP = "timestamp";
    public static final String ACTION_TYPE_TRAJECTORY_MODIFICATION = "trajectory";
    public static final String SCOPE = "scope";
    public static final String SCOPE_TYPE = "type";
    public static final String SCOPE_TYPE_TIME_WINDOW = "timeWindow";
    public static final String SCOPE_TYPE_GEO_AREA = "geoArea";
    public static final String SCOPE_TYPE_GEO_THRESHOLD = "geoThreshold";
    public static final String SCOPE_TYPE_GEO_TIME = "geoTime";
    public static final String SCOPE_TYPE_GEO_TIME_WINDOW = "geoTimeWindow";
    public static final String SCOPE_TYPE_TRIGGER = "trigger";
    public static final String LOWER_BOUND = "lowerBound";
    public static final String UPPER_BOUND = "upperBound";
    public static final String TIME = "time";
    public static final String ZONE_NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String TARGET = "target";
    public static final String TRAJECTORY = "trajectory";
    public static final String PARAMETER_NUMBER = "number";
    public static final String PARAMETER_FREQUENCY = "frequency";
    public static final String RECORD_PATH = "recordPath";
    public static final String PARAMETER_WAYPOINT = "waypoint";
    public static final String PARAMETER_TIMESTAMP = "offset";
    public static final String PARAMETER_RECORD_NAME = "recordName";
    public static final String VALUE = "value";
    public static final String VALUE_TYPE = "type";
    public static final String ITEM = "item";
    public static final String VALUE_TYPE_ALTITUDE = "altitude";
    public static final String VALUE_TYPE_TIME = "time";
    public static final String VALUE_TYPE_CALL_SIGN = "callsign";
    public static final String VALUE_TYPE_EMERGENCY = "emergency";
    public static final String VALUE_TYPE_GROUND_SPEED = "groundSpeed";
    public static final String VALUE_TYPE_ICAO = "icao";
    public static final String VALUE_TYPE_IS_ON_GROUND = "isOnGround";
    public static final String VALUE_TYPE_ALERT = "alert";
    public static final String VALUE_TYPE_LATITUDE = "latitude";
    public static final String VALUE_TYPE_LONGITUDE = "longitude";
    public static final String VALUE_TYPE_SPI = "spi";
    public static final String VALUE_TYPE_SQUAWK = "squawk";
    public static final String VALUE_TYPE_TRACK = "track";
    public static final String VALUE_TYPE_TIMESTAMP_NANO = "timestampNano";
    public static final String VALUE_TYPE_EAST_WEST_VELOCITY = "east_west_velocity";
    public static final String VALUE_TYPE_NORTH_SOUTH_VELOCITY = "north_south_velocity";
    public static final String VALUE_TYPE_VERTICAL_RATE = "verticalRate";
    public static final String OFFSET_VALUE_TRUE = "true";
    public static final String OFFSET_VALUE_FALSE = "false";
    public static final String ATTRIBUT_OFFSET = "offset";
    public static final String KEY = "key";
    public static final String MIN = "min";
    public static final String MAX = "max";

    private AlterationSpecificationStorage() {
    }

    static String renderActionType(final Action action) {
        return new ActionTypeSwitch<String>() {

            @Override
            public String visitAlteration() {
                return ACTION_TYPE_ALTERATION;
            }

            public String visitDeletion() {
                return ACTION_TYPE_DELETION;
            }

            @Override
            public String visitSaturation() {
                return ACTION_TYPE_SATURATION;
            }

            @Override
            public String visitTimestamp() {
                return ACTION_TYPE_TIMESTAMP;
            }

            @Override
            public String visitReplay() {
                return ACTION_TYPE_REPLAY;
            }

            @Override
            public String visitTrajectoryModification() {
                return ACTION_TYPE_TRAJECTORY_MODIFICATION;
            }

            @Override
            public String visitCreation() {
                return ACTION_TYPE_CREATION;
            }
        }.doSwitch(action.getActionType());
    }

    static String renderCharacteristic(final Characteristic characteristic) {
        return new CharacteristicSwitch<String>() {
            @Override
            public String visitAltitude() {
                return VALUE_TYPE_ALTITUDE;
            }

            @Override
            public String visitCallSign() {
                return VALUE_TYPE_CALL_SIGN;
            }

            @Override
            public String visitEmergency() {
                return VALUE_TYPE_EMERGENCY;
            }

            @Override
            public String visitIcao() {
                return VALUE_TYPE_ICAO;
            }

            @Override
            public String visitGroundSpeed() {
                return VALUE_TYPE_GROUND_SPEED;
            }

            @Override
            public String visitLatitude() {
                return VALUE_TYPE_LATITUDE;
            }

            @Override
            public String visitLongitude() {
                return VALUE_TYPE_LONGITUDE;
            }

            @Override
            public String visitSpi() {
                return VALUE_TYPE_SPI;
            }

            @Override
            public String visitSquawk() {
                return VALUE_TYPE_SQUAWK;
            }

            @Override
            public String visitTrack() {
                return VALUE_TYPE_TRACK;
            }

            @Override
            public String visitTimestampNano() {
                return VALUE_TYPE_TIMESTAMP_NANO;
            }

            @Override
            public String visitEastWestVelocity() {
                return VALUE_TYPE_EAST_WEST_VELOCITY;
            }

            @Override
            public String visitNorthSouthVelocity() {
                return VALUE_TYPE_NORTH_SOUTH_VELOCITY;
            }

            @Override
            public String visitVerticalRate() {
                return VALUE_TYPE_VERTICAL_RATE;
            }

            @Override
            public String visitIsOnGround() {
                return VALUE_TYPE_IS_ON_GROUND;
            }

            @Override
            public String visitAlert() {
                return VALUE_TYPE_ALERT;
            }

        }.doSwitch(characteristic);
    }
}