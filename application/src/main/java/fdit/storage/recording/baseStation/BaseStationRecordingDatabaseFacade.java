package fdit.storage.recording.baseStation;

import com.google.common.collect.Multimap;
import fdit.metamodel.aircraft.BaseStationAircraftState;
import fdit.metamodel.coordinates.Coordinates;
import fdit.storage.recording.RecordingDatabaseFacade;

import java.io.File;
import java.util.Map.Entry;

import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.database.PreparedStatementUtils.*;

public class BaseStationRecordingDatabaseFacade extends RecordingDatabaseFacade<BaseStationAircraftState> {

    BaseStationRecordingDatabaseFacade(final File recordingFile) {
        super(recordingFile);
    }

    @Override
    protected void sendPendingAircraftStatesToDatabase(final Multimap<Integer, BaseStationAircraftState> aircraftStates) throws Exception {
        FDIT_DATABASE.executeInPreparedStatement(
                "insert into SBS_AIRCRAFT_STATES(aircraft_table_id, relative_date, latitude, longitude, altitude, " +
                        "ground_speed, track, vertical_rate, squawk, alert, emergency, spi, is_on_ground) " +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?)",

                statement -> {
                    for (final Entry<Integer, BaseStationAircraftState> entry : aircraftStates.entries()) {
                        final BaseStationAircraftState aircraftState = entry.getValue();
                        statement.setLong(1, aircraftIdToTableId.get(entry.getKey()));
                        statement.setLong(2, aircraftState.getRelativeDate());
                        setOptionalDouble(statement, 3, aircraftState.getPosition().map(Coordinates::getLatitude));
                        setOptionalDouble(statement, 4, aircraftState.getPosition().map(Coordinates::getLongitude));
                        setOptionalDouble(statement, 5, aircraftState.getAltitude());
                        setOptionalDouble(statement, 6, aircraftState.getGroundSpeed());
                        setOptionalDouble(statement, 7, aircraftState.getTrack());
                        setOptionalDouble(statement, 8, aircraftState.getVerticalRate());
                        setOptionalShort(statement, 9, aircraftState.getSquawk());
                        setOptionalString(statement, 10, aircraftState.getAlert());
                        setOptionalString(statement, 11, aircraftState.getEmergency());
                        setOptionalString(statement, 12, aircraftState.getSpi());
                        setOptionalString(statement, 13, aircraftState.getIsOnGround());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                });
    }

    @Override
    protected long getFirstDateTimestamp() {
        return firstAbsoluteDate;
    }
}
