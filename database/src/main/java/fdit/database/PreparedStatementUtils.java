package fdit.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

public final class PreparedStatementUtils {

    private PreparedStatementUtils() {
    }

    public static void setOptionalShort(final PreparedStatement statement,
                                        final int parameterIndex,
                                        final Optional<Short> value) throws SQLException {
        if (value.isPresent()) {
            statement.setShort(parameterIndex, value.get());
            return;
        }
        statement.setNull(parameterIndex, Types.SMALLINT);
    }

    public static void setOptionalInt(final PreparedStatement statement,
                                      final int parameterIndex,
                                      final Optional<Integer> value) throws SQLException {
        if (value.isPresent()) {
            statement.setInt(parameterIndex, value.get());
            return;
        }
        statement.setNull(parameterIndex, Types.INTEGER);
    }

    public static void setOptionalDouble(final PreparedStatement statement,
                                         final int parameterIndex,
                                         final Optional<Double> value) throws SQLException {
        if (value.isPresent()) {
            statement.setDouble(parameterIndex, value.get());
            return;
        }
        statement.setNull(parameterIndex, Types.DOUBLE);
    }

    public static void setOptionalBoolean(final PreparedStatement statement,
                                          final int parameterIndex,
                                          final Optional<Boolean> value) throws SQLException {
        if (value.isPresent()) {
            statement.setBoolean(parameterIndex, value.get());
            return;
        }
        statement.setNull(parameterIndex, Types.BOOLEAN);
    }

    public static void setOptionalString(final PreparedStatement statement,
                                         final int parameterIndex,
                                         final Optional<String> value) throws SQLException {
        if (value.isPresent()) {
            statement.setString(parameterIndex, value.get());
            return;
        }
        statement.setNull(parameterIndex, Types.VARCHAR);
    }
}
