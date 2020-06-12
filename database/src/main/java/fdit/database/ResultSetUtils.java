package fdit.database;

import org.h2.jdbc.JdbcSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ResultSetUtils {

    private ResultSetUtils() {
    }

    public static Double getDoubleOrNull(final ResultSet resultSet, final int columnIndex) throws SQLException {
        try {
            final double result = resultSet.getDouble(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Double getDoubleOrNull(final ResultSet resultSet, final String columnLabel) throws SQLException {
        try {
            final double result = resultSet.getDouble(columnLabel);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Integer getIntOrNull(final ResultSet resultSet, final int columnIndex) throws SQLException {
        try {
            final int result = resultSet.getInt(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Integer getIntOrNull(final ResultSet resultSet, final String columnLabel) throws SQLException {
        try {
            final int result = resultSet.getInt(columnLabel);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Short getShortOrNull(final ResultSet resultSet, final int columnIndex) throws SQLException {
        try {
            final short result = resultSet.getShort(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Short getShortOrNull(final ResultSet resultSet, final String columnLabel) throws SQLException {
        try {
            final short result = resultSet.getShort(columnLabel);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Long getLongOrNull(final ResultSet resultSet, final int columnIndex) throws SQLException {
        try {
            final long result = resultSet.getLong(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Long getLongOrNull(final ResultSet resultSet, final String columnLabel) throws SQLException {
        try {
            final long result = resultSet.getLong(columnLabel);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Boolean getBooleanOrNull(final ResultSet resultSet, final int columnIndex) throws SQLException {
        try {
            final boolean result = resultSet.getBoolean(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }

    public static Boolean getBooleanOrNull(final ResultSet resultSet, final String columnLabel) throws SQLException {
        try {
            final boolean result = resultSet.getBoolean(columnLabel);
            if (resultSet.wasNull()) {
                return null;
            }
            return result;
        } catch (final JdbcSQLException e) {
            return null;
        }
    }
}
