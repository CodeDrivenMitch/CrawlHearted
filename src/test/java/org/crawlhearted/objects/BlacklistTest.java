package org.crawlhearted.objects;

import org.crawlhearted.database.Database;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for the Blacklist class
 */
public class BlacklistTest {
    private String baseUrlToTest = "http://www.google.nl/";

    @Before
    public void prepareDbConnection() throws IOException {
        Database.loadSettings();
        Database.openDatabaseConnection();
    }

    @Test
    public void testBlacklistValidation() {
        Blacklist blacklist = new Blacklist(baseUrlToTest);

        blacklist.addEntry("mail", false);

        assertTrue(blacklist.urlAllowed("http://www.google.nl/web"));
        assertFalse(blacklist.urlAllowed("http://www.google.nl/mail"));
        assertFalse(blacklist.urlAllowed("http://www.yahoo.nl/"));
    }
}
