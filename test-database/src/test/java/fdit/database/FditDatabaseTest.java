package fdit.database;

import org.junit.Test;

import static fdit.database.DatabaseTestCase.TEST_DATABASE_URL;
import static fdit.database.FditDatabase.FDIT_DATABASE;
import static fdit.database.FditDatabaseHelper.*;

public class FditDatabaseTest {

    private static void insertToRecordingTable(final String fileName) throws Exception {
        FDIT_DATABASE.executeUpdateRequests("insert into RECORDINGS(file_name) VALUES ('" + fileName + "')");
    }

    @Test
    public void testDatabaseLifecycle() throws Exception {
        FDIT_DATABASE.open(TEST_DATABASE_URL);
        checkDatabase(
                recordingTable());

        insertToRecordingTable("record1.sbs");
        checkDatabase(
                recordingTable(
                        row(cell("file_name", "record1.sbs"))));

        insertToRecordingTable("record2.sbs");
        checkDatabase(
                recordingTable(
                        row(cell("file_name", "record1.sbs")),
                        row(cell("file_name", "record2.sbs"))));

        FDIT_DATABASE.empty();
        checkDatabase(
                recordingTable());

        insertToRecordingTable("record3.sbs");
        checkDatabase(
                recordingTable(
                        row(cell("file_name", "record3.sbs"))));

        FDIT_DATABASE.close();

        FDIT_DATABASE.open(TEST_DATABASE_URL);
        checkDatabase(
                recordingTable(
                        row(cell("file_name", "record3.sbs"))));

        FDIT_DATABASE.close();
    }
}
