package org.crawlhearted.database;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * The Database class is used for opening a database connection and maintaining it.
 * Upon making a connection it will first verify the integrity of the database.
 * When the database structure is correct, according to databasestructure.cfg, it will initalize
 * the ActiveRecord connection.
 * If not, an exception will be thrown and the application will exit.
 */
public class Database {
    private static Logger logger = LoggerFactory.getLogger(Database.class);
    private static String USER;
    private static String PASS;
    private static int PORT;
    private static String HOST;
    private static String NAME;

    // Constant for config file use
    private static final String CONN_PREFIX = "jdbc:mysql://";
    private static final String CFG_PORT = "DbPort";
    private static final String CFG_USER = "DbUser";
    private static final String CFG_HOST = "DbHost";
    private static final String CFG_NAME = "DbName";
    private static final String CFG_PWD = "DbPwd";
    private static final String CFG_FILE = "database.cfg";

    /**
     * Opens a JDBC database connection as well as active JDBC connection if the database structure is correct
     *
     * @throws SQLException Thrown when connection failed or database structure is incorrect
     */
    public static void openDatabaseConnection() throws SQLException {

        // prepare for making the connection
        Properties connectionProps = new Properties();
        connectionProps.put("user", USER);
        connectionProps.put("password", PASS);

        Base.open("com.mysql.jdbc.Driver", CONN_PREFIX + HOST + ":" + PORT + "/" + NAME, USER, PASS);
        logger.info("Database integrity verified and ActiveJDBC connection initialized");
    }

    /**
     * Loads the database.cfg file
     *
     * @throws IOException When config file can't be found
     */
    public static void loadSettings() throws IOException {
        // load database settings.config
        Properties configFile = new java.util.Properties();

        configFile.load(new FileInputStream(CFG_FILE));
        HOST = configFile.getProperty(CFG_HOST);
        PORT = Integer.parseInt(configFile.getProperty(CFG_PORT));
        NAME = configFile.getProperty(CFG_NAME);
        USER = configFile.getProperty(CFG_USER);
        PASS = configFile.getProperty(CFG_PWD);


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
            logger.warn("Couldn't store configuration!", e);
        }
    }

    /**
     * Private constructor to hide the public one
     */
    private Database() {
    }
}
