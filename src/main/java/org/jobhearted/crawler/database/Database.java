package org.jobhearted.crawler.database;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The Database class is used for opening a database connection and maintaining it.
 * Upon making a connection it will first verify the integrity of the database.
 * When the database structure is correct, according to databasestructure.cfg, it will initalize
 * the ActiveRecord connection.
 * If not, an exception will be thrown and the application will exit.
 */
public class Database {
    private static Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private static String USER;
    private static String PASS;
    private static int PORT;
    private static String HOST;
    private static String NAME;

    // Constant for config file use
    private static final String CONN_PREFIX = "jdbc:mysql://";
    private static final String CFG_PORT = "port";
    private static final String CFG_USER = "user";
    private static final String CFG_HOST = "host";
    private static final String CFG_NAME = "name";
    private static final String CFG_PWD = "password";
    private static final String CFG_FILE = "database.cfg";

    /**
     * Opens a JDBC database connection as well as active JDBC connection if the database structure is correct
     */
    public static void openDatabaseConnection() {
        if (!Base.hasConnection()) {
            Base.open("com.mysql.jdbc.Driver", CONN_PREFIX + HOST + ":" + PORT + "/" + NAME, USER, PASS);
            LOGGER.info("ActiveJDBC connection initialized");
        }
    }

    /**
     * Loads the database.cfg file
     */
    public static boolean loadSettings() {
        // load database settings.config
        Properties configFile = new java.util.Properties();

        try {
            configFile.load(new FileInputStream(CFG_FILE));
            HOST = configFile.getProperty(CFG_HOST);
            PORT = Integer.parseInt(configFile.getProperty(CFG_PORT));
            NAME = configFile.getProperty(CFG_NAME);
            USER = configFile.getProperty(CFG_USER);
            PASS = configFile.getProperty(CFG_PWD);
            return true;
        } catch (Exception e) {
            LOGGER.warn("Could not load the {} database config file! Creating the default one.", CFG_FILE);
            createDefaultConfigFile();
            return false;
        }
    }

    /**
     * Saves the current database settings to the disk
     */
    public static void saveSettings() {
        Properties configFile = new java.util.Properties();

        configFile.put(CFG_HOST, HOST);
        configFile.put(CFG_PORT, Integer.toString(PORT));
        configFile.put(CFG_NAME, NAME);
        configFile.put(CFG_USER, USER);
        configFile.put(CFG_PWD, PASS);

        try {
            configFile.store(new FileOutputStream(CFG_FILE), "This is the Database settings file for the JobHearted Crawl application \r\n Last saved:");
        } catch (IOException e) {
            LOGGER.warn("Couldn't store configuration!", e);
        }
    }

    /**
     * Creates a default database config file
     */
    private static void createDefaultConfigFile() {
        HOST = "mysql.insidion.com";
        PORT = 3306;
        NAME = "jobhearted";
        USER = "jobhearted";
        PASS = "RAM2675132";

        saveSettings();
    }

    /**
     * Private constructor to hide the public one
     */
    private Database() {
    }
}
