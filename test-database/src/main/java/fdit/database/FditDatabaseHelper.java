package fdit.database;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Predicate;

import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.tools.predicate.PredicateUtils.and;

public final class FditDatabaseHelper {

    private FditDatabaseHelper() {
    }

    public static void checkDatabase(final Predicate<FditDatabase>... tables) {
        assertThat(FDIT_DATABASE, and(tables));
    }

    public static Predicate<FditDatabase> recordingTable(final Predicate<ResultSet>... rows) {
        return table("RECORDINGS", rows);
    }

    public static Predicate<FditDatabase> aircraftTable(final Predicate<ResultSet>... rows) {
        return table("AIRCRAFTS", rows);
    }

    public static Predicate<FditDatabase> sbsStateTable(final Predicate<ResultSet>... rows) {
        return table("SBS_AIRCRAFT_STATES", rows);
    }

    private static Predicate<FditDatabase> table(final String tableName, final Predicate<ResultSet>... rows) {
        return database -> {
            try {
                return database.executeQuery("select * from " + tableName, resultSet -> {
                    int i = 0;
                    while (resultSet.next()) {
                        if (!rows[i].test(resultSet)) {
                            return false;
                        }
                        i++;
                    }
                    return rows.length == i;
                });
            } catch (final Exception e) {
                throw new AssertionError(e);
            }
        };
    }

    public static Predicate<ResultSet> row(final Predicate<ResultSet>... columns) {
        return and(columns);
    }

    public static Predicate<ResultSet> cell(final String column, @Nullable final Object value) {
        return resultSet -> {
            try {
                final Object object = resultSet.getObject(column);
                if (Objects.equals(object, value)) {
                    return true;
                }
                throw new AssertionError("row " + resultSet.getRow() + " , column " + column + " :\n " +
                        "expected : " + value + "\n got : " + object);
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        };
    }
}
