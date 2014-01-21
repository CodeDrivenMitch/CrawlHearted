package org.jobhearted.crawler;

import org.jobhearted.crawler.database.Database;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 1/21/14
 * Time: 11:02 PM
 */
public class TestDatabase {

    @Test
    public void testConnection() {
        Database.loadSettings();
        Database.openDatabaseConnection();
    }

    @Test
    public void testConfigRestore() throws IOException {
        Properties configFile = new java.util.Properties();
        configFile.put("damn", "damn");
        configFile.store(new FileOutputStream("database.cfg"), "");

        Database.loadSettings();

        configFile.load(new FileInputStream("database.cfg"));
        assertTrue(configFile.getProperty("user") != null);
    }
}
