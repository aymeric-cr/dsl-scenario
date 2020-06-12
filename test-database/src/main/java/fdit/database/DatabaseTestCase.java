package fdit.database;

import org.junit.After;
import org.junit.Before;

import static fdit.database.FditDatabase.FDIT_DATABASE;

public abstract class DatabaseTestCase {

    static final String TEST_DATABASE_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    @Before
    public void setUp() throws Exception {
        FDIT_DATABASE.open(TEST_DATABASE_URL);
    }

    @After
    public void tearDown() throws Exception {
        FDIT_DATABASE.empty();
        FDIT_DATABASE.close();
    }
}