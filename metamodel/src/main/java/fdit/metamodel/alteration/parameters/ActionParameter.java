package fdit.metamodel.alteration.parameters;

public interface ActionParameter {

    interface ActionParameterVisitor<T> {

        T visitAircraftNumber(final AircraftNumber aircraftNumber);

        T visitValue(final Value value);

        T visitRangeValue(final RangeValue rangeValue);

        T visitListValue(final ListValue listValue);

        T visitTimestamp(final Timestamp timestamp);

        T visitRecordName(final RecordName recordName);

        T visitTrajectory(final Trajectory trajectory);

        T visitFrequency(final Frequency frequency);

        default T accept(final ActionParameter actionParameter) {
            if (actionParameter instanceof AircraftNumber) {
                return visitAircraftNumber((AircraftNumber) actionParameter);
            }
            if (actionParameter instanceof Value) {
                return visitValue((Value) actionParameter);
            }
            if (actionParameter instanceof RangeValue) {
                return visitRangeValue((RangeValue) actionParameter);
            }
            if (actionParameter instanceof ListValue) {
                return visitListValue((ListValue) actionParameter);
            }
            if (actionParameter instanceof Timestamp) {
                return visitTimestamp((Timestamp) actionParameter);
            }
            if (actionParameter instanceof RecordName) {
                return visitRecordName((RecordName) actionParameter);
            }
            if (actionParameter instanceof Trajectory) {
                return visitTrajectory((Trajectory) actionParameter);
            }
            if (actionParameter instanceof Frequency) {
                return visitFrequency((Frequency) actionParameter);
            }
            throw new IllegalArgumentException("Unknown action parameter");
        }
    }
}