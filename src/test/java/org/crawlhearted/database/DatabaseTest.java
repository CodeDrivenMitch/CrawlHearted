package org.crawlhearted.database;

import java.io.IOException;
import java.sql.SQLException;

import static junit.framework.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/8/13
 * Time: 11:49 AM
 */
public class DatabaseTest {

    @org.junit.Test
    public void testLoad() throws IOException {
        Database.loadSettings();
    }

    @org.junit.Test
    public void testSaving() throws IOException {
        Database.loadSettings();
        Database.saveSettings();
    }

    @org.junit.Test
    public void testOpeningConnection() throws SQLException, IOException {
        Database.loadSettings();
        Database.openDatabaseConnection();

        assertNotNull(Database.getCONNECTION());
    }
}
