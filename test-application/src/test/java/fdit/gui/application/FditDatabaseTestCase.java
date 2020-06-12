package fdit.gui.application;

import org.junit.After;

import static fdit.database.FditDatabase.FDIT_DATABASE;

public abstract class FditDatabaseTestCase extends FditTestCase {

    private static final String TEST_DATABASE_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FDIT_DATABASE.open(TEST_DATABASE_URL);
    }

    @After
    public void tearDown() throws Exception {
        FDIT_DATABASE.empty();
        FDIT_DATABASE.close();
    }
}